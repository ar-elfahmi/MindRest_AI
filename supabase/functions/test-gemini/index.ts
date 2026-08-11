// Smoke-test Edge Function — TASK 1.4.
// Tujuan: validasi pipeline AI (call Gemini API) sebelum bangun pipeline
// Ikigai kompleks di TASK 3.1. JANGAN pakai untuk logic produksi.
//
// BACA DULU:
//   - ROADMAP.md section "TASK 1.4"
//   - supabase/functions/README.md (struktur & cara deploy)
//
// ENV yang dipakai:
//   - GEMINI_API_KEY  (WAJIB, di-set via `supabase secrets set`)

import { corsHeaders } from '../_shared/cors.ts'

// Lock ke major version yang sama dengan ROADMAP. Jangan pakai "latest".
// ^0.21 akan resolve ke 0.24.x (terbaru di major 0.x).
import { GoogleGenerativeAI } from 'npm:@google/generative-ai@^0.21'

// Model default untuk smoke test.
//   gemini-3.5-flash-lite → sub-detik, free tier-friendly, kualitas cukup
//     untuk validasi pipeline. Dipakai sebagai default karena:
//     - gemini-1.5-flash → sudah deprecated (404)
//     - gemini-2.0-flash & gemini-2.0-flash-lite → quota free tier habis
//     - gemini-2.5-* → tidak tersedia untuk user baru (404)
//     - gemini-3.5-flash → works tapi ~8s cold start (lebih mahal)
//   Untuk TASK 3.1 (Ikigai), pertimbangkan `gemini-3.5-flash` (kualitas lebih).
const DEFAULT_MODEL = 'gemini-3.5-flash-lite'

// Prompt default persis dari acceptance criteria ROADMAP supaya test cepat.
const DEFAULT_PROMPT = 'Sebut 3 warna primer dalam 1 kalimat.'

// Batas karakter untuk prompt. Gemini API bebas, tapi untuk smoke test
// kita cegah prompt gila yang bisa boros quota saat iterasi dev.
const MAX_PROMPT_LENGTH = 1000

/**
 * Shape response terstruktur. Selalu return {ok, data?, error?} supaya
 * client (curl/Postman/test e2e) bisa branch berdasarkan `ok`.
 */
type OkResponse = {
  ok: true
  data: {
    text: string
    model: string
    prompt: string
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
  error: {
    code: string
    message: string
  }
}

type FunctionResponse = OkResponse | ErrResponse

Deno.serve(async (req: Request) => {
  const startedAt = performance.now()

  // CORS preflight WAJIB ditangani sebelum apa-apa (Chrome, browser tools).
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  // Pakai GET juga untuk smoke test paling sederhana (no body).
  // POST boleh override prompt & model via body JSON.
  let prompt = DEFAULT_PROMPT
  let modelName = DEFAULT_MODEL

  if (req.method === 'POST') {
    // Parse body SEKALI. Kalau bukan JSON → anggap kosong, pakai default.
    const body = await req.json().catch(() => null)
    if (body && typeof body === 'object') {
      if (typeof body.prompt === 'string' && body.prompt.trim()) {
        prompt = body.prompt.trim()
      }
      if (typeof body.model === 'string' && body.model.trim()) {
        modelName = body.model.trim()
      }
    }
  }

  if (prompt.length > MAX_PROMPT_LENGTH) {
    return jsonResponse({
      ok: false,
      error: {
        code: 'prompt_too_long',
        message: `Prompt exceeds ${MAX_PROMPT_LENGTH} characters (got ${prompt.length}).`,
      },
    }, 400)
  }

  // STEP 1: Cek secret. Kalau missing → return error TERANG, bukan crash 500.
  const apiKey = Deno.env.get('GEMINI_API_KEY')
  if (!apiKey || apiKey.trim() === '') {
    return jsonResponse({
      ok: false,
      error: {
        code: 'missing_api_key',
        message:
          'GEMINI_API_KEY tidak ditemukan di Supabase secrets. Jalankan: supabase secrets set GEMINI_API_KEY=<key>',
      },
    }, 500)
  }

  // STEP 2: Init SDK.
  let genAI: GoogleGenerativeAI
  try {
    genAI = new GoogleGenerativeAI(apiKey)
  } catch (err) {
    return jsonResponse({
      ok: false,
      error: {
        code: 'sdk_init_failed',
        message: errorMessage(err),
      },
    }, 500)
  }

  const model = genAI.getGenerativeModel({ model: modelName })

  // STEP 3: Call Gemini. Tangani error kategori: safety/rate-limit/network.
  try {
    const result = await model.generateContent(prompt)
    const text = result.response.text()

    const usage = result.response.usageMetadata
      ? {
          promptTokenCount: result.response.usageMetadata.promptTokenCount ?? 0,
          candidatesTokenCount:
            result.response.usageMetadata.candidatesTokenCount ?? 0,
          totalTokenCount: result.response.usageMetadata.totalTokenCount ?? 0,
        }
      : undefined

    return jsonResponse(
      {
        ok: true,
        data: {
          text,
          model: modelName,
          prompt,
          latency_ms: Math.round(performance.now() - startedAt),
          ...(usage ? { usage } : {}),
        },
      },
      200,
    )
  } catch (err) {
    const code = classifyError(err)
    return jsonResponse(
      {
        ok: false,
        error: {
          code,
          message: errorMessage(err),
        },
      },
      statusForError(code),
    )
  }
})

// ──────────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────────

/** Bungkus Response JSON + tambah header CORS + Content-Type. */
function jsonResponse(body: FunctionResponse, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, 'Content-Type': 'application/json' },
  })
}

/** Pesan error yang aman: dipotong agar tidak bocor credential panjang. */
function errorMessage(err: unknown): string {
  if (err instanceof Error) {
    let msg = err.message || 'Unknown error'
    if (msg.length > 300) msg = msg.slice(0, 300) + '…'
    return msg
  }
  return String(err)
}

/**
 * Klasifikasi kasar error Gemini supaya client bisa branch tanpa parse pesan.
 * Pakai regex word-boundary supaya substring tidak salah-match
 * (mis. "rate" di dalam "generateContent").
 * - rate_limited    → 429 / RESOURCE_EXHAUSTED
 * - safety_block    → prompt ditolak filter keamanan
 * - invalid_key     → API key ditolak (401/403 / UNAUTHENTICATED)
 * - model_not_found → 404 / model id salah / deprecated
 * - upstream_error  → 5xx / network / timeout
 * - unknown         → fallback
 */
function classifyError(err: unknown): string {
  const msg = err instanceof Error ? err.message : String(err)
  const lower = msg.toLowerCase()

  // 429 / quota / rate limit — word boundary penting agar "rate" tidak
  // cocok di dalam "generateContent".
  if (/\b429\b/.test(lower) || /\bquota\b/.test(lower) || /\brate[- ]?limit/.test(lower)) {
    return 'rate_limited'
  }
  if (
    /\bsafety\b/.test(lower) ||
    /\bblocked\b/.test(lower) ||
    /\brecitation\b/.test(lower)
  ) {
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

/** Map error code → HTTP status yang masuk akal untuk client. */
function statusForError(code: string): number {
  switch (code) {
    case 'rate_limited':
      return 429
    case 'safety_block':
      return 400
    case 'model_not_found':
      return 400 // model id salah → caller bisa fix
    case 'invalid_key':
      return 500 // server-side config issue, bukan client error
    case 'upstream_error':
      return 502
    default:
      return 500
  }
}