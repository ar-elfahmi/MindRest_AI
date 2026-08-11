// =============================================================================
// Edge Function: chat-gemini
// =============================================================================
// T-003 (FR-009 — Journaling via AI Chatbot, FR-011 — Olah data jurnal).
//
// Tujuan:
//   1. Verify JWT user → dapat user_id.
//   2. Ambil conversation history dari `journal_entries` WHERE
//      user_id = ? AND session_id = ? AND role IS NOT NULL, urut created_at ASC.
//   3. Bangun array pesan [{role: 'user'|'model', parts: [{text: ...}]}, ...]
//      untuk dikirim ke Gemini.
//   4. Call Gemini (mode chat, multi-turn). Pakai system instruction CBT-style
//      untuk respons empatik & reflektif.
//   5. Return AI response text ke client. CLIENT yang INSERT row (user +
//      assistant) ke journal_entries — Edge Function tidak insert sendiri
//      supaya conversation real-time dapat ditulis atomik dari client (lihat
//      catatan di bawah).
//
// KENAPA CLIENT YANG INSERT (BEDA DENGAN generate-ikigai-report)?
//   - Chat adalah interaksi real-time, satu per satu. User butuh lihat
//     pesan user-nya dulu, baru AI reply. Kalau server INSERT, UI harus
//     poll atau optimistic update.
//   - Journal entries chat tidak butuh service-role bypass (RLS user sudah
//     INSERT-able dengan policy existing).
//   - Bandingkan Ikigai report: itu GENERATE sekali per 24 jam, server
//     INSERT masuk akal karena output adalah dokumen terstruktur.
//
// ENV yang dipakai:
//   - SUPABASE_URL       (otomatis ada di runtime)
//   - SUPABASE_ANON_KEY  (otomatis ada di runtime)
//   - GEMINI_API_KEY     (WAJIB, di-set via `supabase secrets set`)
//
// BACA DULU:
//   - supabase/functions/test-gemini/index.ts (pola error classifier)
//   - supabase/functions/generate-ikigai-report/index.ts (pola JWT verify
//     + service-role fetch — tapi di sini TANPA service-role)
//   - supabase/functions/_shared/cors.ts
// =============================================================================

import { corsHeaders } from '../_shared/cors.ts'
import { GoogleGenerativeAI } from 'npm:@google/generative-ai@^0.21'
import { createClient } from 'npm:@supabase/supabase-js@^2.45'


// ---------------------------------------------------------------------------
// Konfigurasi
// ---------------------------------------------------------------------------

// Model: gemini-3.5-flash-lite — cukup untuk chat reflektif (balasan 2-4
// paragraf). Cold start jauh lebih cepat dari `flash` (~600ms warm). Untuk
// kualitas lebih bisa override via body.model.
const DEFAULT_MODEL = 'gemini-3.5-flash-lite'

// Batas context window (jumlah pesan history yang dikirim ke Gemini).
// Gemini flash-lite punya ~1M token context, tapi untuk hemat biaya &
// latency kita batasi 20 pesan terakhir (= 10 turn bolak-balik).
const MAX_HISTORY_MESSAGES = 20

// Batas karakter per pesan user (defensive). Gemini API bebas, tapi ini
// mencegah prompt injection / spam yang bisa boros quota.
const MAX_USER_MESSAGE_CHARS = 2000


// ---------------------------------------------------------------------------
// System instruction (CBT-style)
// ---------------------------------------------------------------------------

/**
 * Persona: "Ruang Refleksi" — asisten empatik ala CBT (Cognitive Behavioral
 * Therapy). Berkomunikasi dalam Bahasa Indonesia (sesuai konteks app).
 *
 * Aturan:
 *  - Validasi perasaan tanpa menghakimi.
 *  - Dorong eksplorasi (open-ended question).
 *  - TIDAK memberi diagnosis medis / meresepkan treatment.
 *  - Kalau user tampak dalam krisis, sarankan profesional + hotline.
 */
const SYSTEM_INSTRUCTION = `Kamu adalah asisten reflektif di aplikasi "MindRest_AI" yang membantu pengguna journaling tentang perasaan dan pikiran mereka.

GAYA BICARA:
- Bahasa Indonesia yang hangat, lembut, dan empatik.
- Validasi perasaan user terlebih dahulu sebelum eksplorasi.
- Gunakan kalimat pendek dan mudah dicerna (maks 3-4 kalimat per respons).
- Akhiri respons dengan satu pertanyaan terbuka untuk mendorong user bercerita lebih lanjut (kecuali user sudah tampak selesai).

BATASAN KETAT (JANGAN DILANGGAR):
- Jangan pernah memberikan diagnosis medis (depresi, anxiety disorder, dll).
- Jangan meresepkan obat atau terapi spesifik.
- Jangan berpura-pura menjadi psikolog/psikiater profesional.
- Jika user menyebutkan pikiran untuk menyakiti diri sendiri atau orang lain, sarankan dengan lembut untuk menghubungi profesional (psikolog/psikiater) atau hotline krisis seperti Into The Light (119 ext 8) atau LSM Jangan Bunuh Diri (021-9696 9293 / 0858-9150-0029).

TUJUAN:
- Membantu user mengenali pola pikiran dan emosi mereka sendiri.
- Mendorong self-reflection yang sehat.
- Menjadi "teman bicara" yang aman dan suportif.`


// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface ChatRequestBody {
  /** Pesan user terbaru. WAJIB. */
  message: string
  /** Session ID untuk conversation history. Opsional — kalau null,
   *  server treat sebagai "first turn" (no history). Client generate
   *  UUID saat mulai sesi baru. */
  session_id?: string | null
  /** Override model (opsional). Default = DEFAULT_MODEL. */
  model?: string | null
}

interface HistoryMessage {
  role: 'user' | 'assistant'
  content: string
}

/** Response contract — selalu {ok, data?, error?} untuk konsistensi
 *  dengan function Edge Function lain (test-gemini, generate-ikigai). */
type OkResponse = {
  ok: true
  data: {
    /** Teks balasan AI. */
    reply: string
    /** Model yang dipakai. */
    model: string
    /** Session ID yang dipakai (echo dari request, atau null kalau first turn). */
    session_id: string | null
    /** Jumlah pesan history yang dipakai untuk context. */
    history_used: number
    latency_ms: number
    usage?: {
      promptTokenCount: number
      candidatesTokenCount: number
      totalTokenCount: number
    }
  }
}

type ErrResponse = {
  ok: false
  error: { code: string; message: string; [extra: string]: unknown }
}

type FunctionResponse = OkResponse | ErrResponse


// ---------------------------------------------------------------------------
// Handler
// ---------------------------------------------------------------------------

Deno.serve(async (req: Request) => {
  const startedAt = performance.now()

  // CORS preflight WAJIB sebelum apa-apa.
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  if (req.method !== 'POST') {
    return jsonResponse(
      { ok: false, error: { code: 'method_not_allowed', message: 'Gunakan POST.' } },
      405,
    )
  }

  // ─── STEP 1: Verifikasi secret ──────────────────────────────────────────
  const supabaseUrl = Deno.env.get('SUPABASE_URL')
  const geminiKey = Deno.env.get('GEMINI_API_KEY')

  if (!supabaseUrl) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'missing_runtime_config',
          message: 'SUPABASE_URL tidak ada di runtime.',
        },
      },
      500,
    )
  }

  if (!geminiKey || geminiKey.trim() === '') {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'missing_api_key',
          message:
            'GEMINI_API_KEY tidak ditemukan di Supabase secrets. Jalankan: supabase secrets set GEMINI_API_KEY=<key>',
        },
      },
      500,
    )
  }

  // ─── STEP 2: Verifikasi JWT → user_id ────────────────────────────────────
  const authHeader = req.headers.get('Authorization')
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'missing_authorization',
          message: 'Header Authorization: Bearer <jwt> wajib ada.',
        },
      },
      401,
    )
  }

  const jwt = authHeader.slice('Bearer '.length).trim()
  if (!jwt) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'invalid_authorization', message: 'Token kosong.' },
      },
      401,
    )
  }

  // ANON-key client dengan user JWT untuk verifikasi.
  const authClient = createClient(supabaseUrl, Deno.env.get('SUPABASE_ANON_KEY') ?? '', {
    auth: { persistSession: false, autoRefreshToken: false },
    global: { headers: { Authorization: `Bearer ${jwt}` } },
  })

  let userId: string
  try {
    const { data, error } = await authClient.auth.getUser(jwt)
    if (error || !data?.user) {
      return jsonResponse(
        {
          ok: false,
          error: {
            code: 'invalid_jwt',
            message: 'JWT tidak valid atau kadaluarsa.',
          },
        },
        401,
      )
    }
    userId = data.user.id
  } catch (err) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'auth_verification_failed',
          message: errorMessage(err),
        },
      },
      401,
    )
  }

  // ─── STEP 3: Parse + validate body ─────────────────────────────────────
  let body: ChatRequestBody
  try {
    const raw = await req.json()
    body = parseBody(raw)
  } catch (err) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'invalid_body',
          message: 'Body harus JSON valid dengan field `message`.',
        },
      },
      400,
    )
  }

  if (!body.message.trim()) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'empty_message', message: 'Field `message` kosong.' },
      },
      400,
    )
  }

  if (body.message.length > MAX_USER_MESSAGE_CHARS) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'message_too_long',
          message: `Message exceeds ${MAX_USER_MESSAGE_CHARS} chars (got ${body.message.length}).`,
        },
      },
      400,
    )
  }

  // ─── STEP 4: Fetch conversation history (kalau ada session_id) ─────────
  // Pakai ANON client dengan user JWT → RLS otomatis enforce user_id.
  // Tidak perlu service-role karena user SELECT own rows sudah di-policy.
  let history: HistoryMessage[] = []
  if (body.session_id) {
    const { data, error } = await authClient
      .from('journal_entries')
      .select('role, content')
      .eq('user_id', userId)
      .eq('session_id', body.session_id)
      .not('role', 'is', null) // exclude legacy "full entry" rows
      .order('created_at', { ascending: true })
      .limit(MAX_HISTORY_MESSAGES)

    if (error) {
      return jsonResponse(
        {
          ok: false,
          error: {
            code: 'history_fetch_failed',
            message: error.message,
          },
        },
        500,
      )
    }

    history = (data ?? [])
      .map((row: { role: string; content: string }) => ({
        role: row.role === 'user' ? 'user' : 'assistant',
        content: row.content,
      }))
      // Map role 'assistant' (DB) → 'model' (Gemini API).
      .map((m) => ({ ...m }))
  }

  // ─── STEP 5: Bangun chat history untuk Gemini ──────────────────────────
  // Gemini API expects: [{role: 'user'|'model', parts: [{text: ...}]}, ...]
  const chatHistory = history.map((m) => ({
    role: m.role === 'user' ? 'user' : 'model',
    parts: [{ text: m.content }],
  }))

  // ─── STEP 6: Call Gemini (chat mode) ───────────────────────────────────
  const modelName = (body.model && body.model.trim()) || DEFAULT_MODEL

  let genAI: GoogleGenerativeAI
  try {
    genAI = new GoogleGenerativeAI(geminiKey)
  } catch (err) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'sdk_init_failed', message: errorMessage(err) },
      },
      500,
    )
  }

  const model = genAI.getGenerativeModel({
    model: modelName,
    systemInstruction: SYSTEM_INSTRUCTION,
    generationConfig: {
      // Suhu moderate-tinggi untuk variasi respons empatik. Jangan terlalu
      // rendah (kaku) atau terlalu tinggi (chaotic).
      temperature: 0.8,
      // Output chat pendek (2-4 paragraf), 600 token cukup.
      maxOutputTokens: 600,
    },
  })

  let reply: string
  let usage: OkResponse['data']['usage'] | undefined
  try {
    const chat = model.startChat({ history: chatHistory })
    const result = await chat.sendMessage(body.message)
    reply = result.response.text()
    const u = result.response.usageMetadata
    if (u) {
      usage = {
        promptTokenCount: u.promptTokenCount ?? 0,
        candidatesTokenCount: u.candidatesTokenCount ?? 0,
        totalTokenCount: u.totalTokenCount ?? 0,
      }
    }
  } catch (err) {
    const code = classifyError(err)
    return jsonResponse(
      {
        ok: false,
        error: { code, message: errorMessage(err) },
      },
      statusForError(code),
    )
  }

  if (!reply || reply.trim() === '') {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'empty_reply',
          message: 'Gemini mengembalikan respons kosong. Coba lagi.',
        },
      },
      502,
    )
  }

  return jsonResponse(
    {
      ok: true,
      data: {
        reply: reply.trim(),
        model: modelName,
        session_id: body.session_id ?? null,
        history_used: history.length,
        latency_ms: Math.round(performance.now() - startedAt),
        ...(usage ? { usage } : {}),
      },
    },
    200,
  )
})


// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function jsonResponse(body: FunctionResponse, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, 'Content-Type': 'application/json' },
  })
}

function errorMessage(err: unknown): string {
  if (err instanceof Error) {
    let msg = err.message || 'Unknown error'
    if (msg.length > 300) msg = msg.slice(0, 300) + '…'
    return msg
  }
  return String(err)
}

/**
 * Klasifikasi kasar error Gemini. POLA PERSIS dengan test-gemini/index.ts
 * dan generate-ikigai-report/index.ts (regex word-boundary).
 */
function classifyError(err: unknown): string {
  const msg = err instanceof Error ? err.message : String(err)
  const lower = msg.toLowerCase()

  if (/\b429\b/.test(lower) || /\bquota\b/.test(lower) || /\brate[- ]?limit/.test(lower)) {
    return 'rate_limited'
  }
  if (/\bsafety\b/.test(lower) || /\bblocked\b/.test(lower) || /\brecitation\b/.test(lower)) {
    return 'safety_block'
  }
  if (
    /\bapi[- ]?key\b/.test(lower) ||
    /\bunauthenticated\b/.test(lower) ||
    /\bpermission[- ]?denied\b/.test(lower) ||
    /\b401\b/.test(lower) ||
    /\b403\b/.test(lower)
  ) {
    return 'invalid_key'
  }
  if (/\b404\b/.test(lower) || /\bnot[- ]?found\b/.test(lower)) {
    return 'model_not_found'
  }
  if (
    /\bnetwork\b/.test(lower) ||
    /\btimeout\b/.test(lower) ||
    /\beconn/.test(lower) ||
    /\b500\b/.test(lower) ||
    /\b502\b/.test(lower) ||
    /\b503\b/.test(lower)
  ) {
    return 'upstream_error'
  }
  return 'unknown'
}

function statusForError(code: string): number {
  switch (code) {
    case 'rate_limited':
      return 429
    case 'safety_block':
      return 400
    case 'model_not_found':
      return 400
    case 'invalid_key':
      return 500
    case 'upstream_error':
      return 502
    default:
      return 500
  }
}

/**
 * Parse + sanitize body. Toleran terhadap field opsional (session_id, model).
 * Throw Error kalau shape terlalu invalid (ditangkap caller).
 */
function parseBody(raw: unknown): ChatRequestBody {
  if (!raw || typeof raw !== 'object') {
    throw new Error('Body bukan object.')
  }
  const obj = raw as Record<string, unknown>
  const message = typeof obj.message === 'string' ? obj.message : ''
  const session_id =
    typeof obj.session_id === 'string' && obj.session_id.trim()
      ? obj.session_id.trim()
      : null
  const model =
    typeof obj.model === 'string' && obj.model.trim() ? obj.model.trim() : null
  return { message, session_id, model }
}
