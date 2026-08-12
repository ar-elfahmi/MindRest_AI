// =============================================================================
// Edge Function: generate-ikigai-report
// =============================================================================
// TASK 3.1 + 3.2 dari ROADMAP.md.
//
// Tujuan:
//   1. Verify JWT user → dapat user_id.
//   2. Rate limit 1 report / 24 jam (return 429 kalau sudah ada).
//   3. Fetch assessment terbaru + mood/sleep/journal 7 hari terakhir.
//   4. Bangun prompt (dari _shared/prompts/ikigai.ts).
//   5. Call Gemini dengan JSON mode + response_schema.
//   6. Parse + enrich (tambah id + done:false per rekomendasi).
//   7. INSERT ke ikigai_reports via SERVICE ROLE (bypass RLS).
//   8. Return report JSON ke client (sesuai output contract).
//
// ENV yang dipakai:
//   - SUPABASE_URL        (otomatis ada di runtime)
//   - SUPABASE_ANON_KEY   (otomatis ada di runtime)
//   - SUPABASE_SERVICE_ROLE_KEY  (WAJIB, di-set via `supabase secrets set`)
//   - GEMINI_API_KEY      (WAJIB, di-set via `supabase secrets set`)
//
// BACA DULU:
//   - ROADMAP.md section "TASK 3.1 + 3.2"
//   - AUDIT.md section 0
//   - supabase/functions/test-gemini/index.ts  (pola error classifier)
//   - supabase/functions/README.md  (latency + model notes)
//   - supabase/functions/_shared/prompts/ikigai.ts
// =============================================================================

import { corsHeaders } from '../_shared/cors.ts'
import {
  buildIkigaiPrompt,
  IKIGAI_RESPONSE_SCHEMA,
  type IkigaiAssessmentLike,
  type IkigaiPassiveDataLike,
} from '../_shared/prompts/ikigai.ts'

// Lock SDK ke major version yang sama dengan test-gemini (lihat README).
import { GoogleGenerativeAI } from 'npm:@google/generative-ai@^0.21'

// supabase-js untuk verify JWT + query DB dengan service role.
// Pakai createClient langsung (bukan @supabase/supabase-js SSR helper
// yang overkill untuk 1 function).
import { createClient } from 'npm:@supabase/supabase-js@^2.45'


// ---------------------------------------------------------------------------
// Konfigurasi
// ---------------------------------------------------------------------------

// Model: gemini-3.5-flash (BUKAN lite).
// Alasan (lihat README section "Pemilihan Model"):
//   - Laporan Ikigai = markdown panjang (350-600 kata) + JSON terstruktur.
//   - Lite kurang konsisten untuk output panjang.
//   - Cold start ~9.6s, warm ~600ms-1s — UI client TASK 3.3 wajib loading.
const GEMINI_MODEL = 'gemini-3.5-flash'

// Window rate limit (1 report / 24 jam).
const RATE_LIMIT_HOURS = 24

// Batas payload output Gemini (sanity check). 50 KB cukup untuk laporan
// 600 kata + 5 rekomendasi. Kalau lebih = indikasi output rusak.
const MAX_REPORT_BYTES = 50_000


// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

/** Output contract (HARUS match dengan prompt schema & UI TASK 3.3). */
interface IkigaiReportOutput {
  report_markdown: string
  ikigai_circles: {
    passion: string
    skill: string
    profession: string
    mission: string
  }
  recommendations: Array<{
    id: string
    text: string
    done: false
  }>
}

/** Bentuk response terstruktur (sama dengan test-gemini). */
type OkResponse = {
  ok: true
  data: {
    report: IkigaiReportOutput
    report_id: string        // UUID row ikigai_reports
    assessment_id: string    // UUID row ikigai_assessments yang dipakai
    version: number
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

  // ─── STEP 1: Verifikasi secret server-side ────────────────────────────
  const supabaseUrl = Deno.env.get('SUPABASE_URL')
  const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')
  const geminiKey = Deno.env.get('GEMINI_API_KEY')

  if (!supabaseUrl || !serviceRoleKey) {
    // Environment runtime Supabase selalu isi SUPABASE_URL. Kalau tidak ada
    // → runtime config rusak.
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'missing_runtime_config',
          message:
            'SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY tidak ada di runtime.',
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

  // ─── STEP 2: Verifikasi JWT → user_id ─────────────────────────────────
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

  // Client ini pakai ANON key + user JWT → hanya untuk verifikasi token.
  // Pakai service role key terpisah untuk query/insert (lihat STEP 4).
  // Pola dari docs Supabase: auth.getUser(jwt) verifikasi signature + expiry
  // dan mengembalikan user row lengkap.
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
    // Edge cases (network error ke Supabase auth server, malformed, dll).
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

  // ─── STEP 3: Rate limit (1 report / 24 jam) ──────────────────────────
  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  })

  const sinceIso = new Date(Date.now() - RATE_LIMIT_HOURS * 3600 * 1000).toISOString()

  const { data: recentReports, error: rateErr } = await adminClient
    .from('ikigai_reports')
    .select('id, generated_at, version')
    .eq('user_id', userId)
    .gt('generated_at', sinceIso)
    .order('generated_at', { ascending: false })
    .limit(1)

  if (rateErr) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'rate_limit_check_failed',
          message: rateErr.message,
        },
      },
      500,
    )
  }

  if (recentReports && recentReports.length > 0) {
    const last = recentReports[0]
    const nextAvailableIso = new Date(
      new Date(last.generated_at).getTime() + RATE_LIMIT_HOURS * 3600 * 1000,
    ).toISOString()

    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'already_generated_today',
          message:
            `Laporan sudah di-generate hari ini (${last.generated_at}). Coba lagi setelah ${nextAvailableIso}.`,
        },
      },
      429,
    )
  }

  // ─── STEP 4: Fetch assessment terbaru + data pasif ───────────────────
  const { data: assessmentRow, error: aErr } = await adminClient
    .from('ikigai_assessments')
    .select(
      'id, q1_passion, q2_skill, q3_profession, q4_mission, q5_overthinking, q6_satisfaction, created_at',
    )
    .eq('user_id', userId)
    .order('created_at', { ascending: false })
    .limit(1)
    .maybeSingle()

  if (aErr) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'assessment_fetch_failed', message: aErr.message },
      },
      500,
    )
  }

  if (!assessmentRow) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'no_assessment',
          message:
            'Belum ada assessment. User harus isi 6 pertanyaan assessment dulu.',
        },
      },
      404,
    )
  }

  const assessment: IkigaiAssessmentLike = {
    q1_passion: assessmentRow.q1_passion,
    q2_skill: assessmentRow.q2_skill,
    q3_profession: assessmentRow.q3_profession,
    q4_mission: assessmentRow.q4_mission,
    q5_overthinking: assessmentRow.q5_overthinking,
    q6_satisfaction: assessmentRow.q6_satisfaction,
  }

  // Data pasif 7 hari — opsional, best-effort. Kalau gagal fetch, jangan
  // gagalkan seluruh pipeline; lanjut tanpa data pasif.
  const passiveData = await fetchPassiveData(adminClient, userId)

  // ─── STEP 5: Bangun prompt ────────────────────────────────────────────
  const prompt = buildIkigaiPrompt(assessment, passiveData)

  // ─── STEP 6: Call Gemini (JSON mode + schema) ────────────────────────
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
    model: GEMINI_MODEL,
    generationConfig: {
      responseMimeType: 'application/json',
      responseSchema: IKIGAI_RESPONSE_SCHEMA,
      // Suhu rendah → output lebih deterministik (Ikigai = konsistensi narasi).
      temperature: 0.7,
      // gemini-3.5-flash adalah thinking model — sebagian token budget
      // terpakai untuk internal reasoning. Naikkan maxOutputTokens supaya
      // output JSON tidak terpotong. 8192 cukup untuk 600 kata markdown +
      // JSON + buffer thinking.
      maxOutputTokens: 8192,
    },
  })

  let rawText: string
  let usage: OkResponse['data']['usage'] | undefined
  try {
    const result = await model.generateContent(prompt)
    rawText = result.response.text()
    const u = result.response.usageMetadata
    if (u) {
      usage = {
        promptTokenCount: u.promptTokenCount ?? 0,
        candidatesTokenCount: u.candidatesTokenCount ?? 0,
        totalTokenCount: u.totalTokenCount ?? 0,
      }
    }
    // Observability: log raw output untuk debugging Gemini quality issues.
    // Hanya ~500 char pertama & 200 terakhir supaya log tidak meledak.
    console.log('[generate-ikigai-report] raw length:', rawText.length)
    console.log('[generate-ikigai-report] raw head (500):', rawText.slice(0, 500))
    console.log('[generate-ikigai-report] raw tail (200):', rawText.slice(-200))
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

  if (rawText.length > MAX_REPORT_BYTES) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'output_too_large',
          message: `Output Gemini ${rawText.length} bytes > ${MAX_REPORT_BYTES}.`,
        },
      },
      502,
    )
  }

  // ─── STEP 7: Parse + enrich JSON dari Gemini ──────────────────────────
  let parsed: {
    report_markdown: string
    ikigai_circles: { passion: string; skill: string; profession: string; mission: string }
    recommendations: Array<{ text: string }>
  }

  try {
    parsed = JSON.parse(rawText)
  } catch (err) {
    // Sertakan excerpt raw text (output truncated) supaya user & support
    // bisa langsung lihat apa yang Gemini hasilkan saat ada parse failure.
    // Aman: hanya output AI, tidak ada data user.
    const excerpt = rawText.slice(0, 600).replace(/\n/g, ' ')
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'json_parse_failed',
          message: `Gemini output bukan JSON valid: ${errorMessage(err)}`,
          excerpt,
          raw_length: rawText.length,
        },
      },
      502,
    )
  }

  // Validasi ringan (schema Gemini sudah guard, tapi defense in depth).
  const validationErr = validateAiOutput(parsed)
  if (validationErr) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'schema_violation', message: validationErr },
      },
      502,
    )
  }

  // Enrich rekomendasi: tambah id (UUID) + done:false default.
  // (Output contract untuk client TASK 3.3.)
  const enrichedRecommendations: IkigaiReportOutput['recommendations'] =
    parsed.recommendations.map((r) => ({
      id: crypto.randomUUID(),
      text: r.text,
      done: false,
    }))

  const finalReport: IkigaiReportOutput = {
    report_markdown: parsed.report_markdown,
    ikigai_circles: {
      passion: parsed.ikigai_circles.passion,
      skill: parsed.ikigai_circles.skill,
      profession: parsed.ikigai_circles.profession,
      mission: parsed.ikigai_circles.mission,
    },
    recommendations: enrichedRecommendations,
  }

  // ─── STEP 8: INSERT ke ikigai_reports (service role, bypass RLS) ─────
  //
  // Pakai service role → INSERT lolos walau policy 'Users can read own
  // reports' (SELECT) tidak punya policy INSERT. Inilah cara user tidak
  // bisa inject report palsu dari client (lihat ROADMAP.md section 4.2).
  //
  // Version: 1 untuk laporan pertama. Increment kalau user refresh dengan
  // data pasif baru (TASK 4.1). Untuk TASK 3.1 kita selalu = 1.
  // ---------------------------------------------------------------------------
  const { data: insertedRow, error: insErr } = await adminClient
    .from('ikigai_reports')
    .insert({
      user_id: userId,
      assessment_id: assessmentRow.id,
      report_markdown: finalReport.report_markdown,
      ikigai_circles: finalReport.ikigai_circles,
      recommendations: finalReport.recommendations,
      version: 1,
    })
    .select('id, generated_at')
    .single()

  if (insErr || !insertedRow) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'insert_failed',
          message: insErr?.message ?? 'Insert ke ikigai_reports gagal.',
        },
      },
      500,
    )
  }

  return jsonResponse(
    {
      ok: true,
      data: {
        report: finalReport,
        report_id: insertedRow.id,
        assessment_id: assessmentRow.id,
        version: 1,
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
 * (regex word-boundary, JANGAN ubah jadi substring match — lihat komentar
 * di file itu).
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

/** Validasi ringan output AI sebelum di-INSERT ke DB. */
function validateAiOutput(o: unknown): string | null {
  if (!o || typeof o !== 'object') return 'Output bukan object.'

  const obj = o as Record<string, unknown>

  if (typeof obj.report_markdown !== 'string' || obj.report_markdown.trim().length < 50) {
    return 'report_markdown kosong atau terlalu pendek.'
  }

  const c = obj.ikigai_circles as Record<string, unknown> | undefined
  if (!c || typeof c !== 'object') return 'ikigai_circles bukan object.'
  for (const k of ['passion', 'skill', 'profession', 'mission']) {
    if (typeof c[k] !== 'string' || (c[k] as string).trim().length === 0) {
      return `ikigai_circles.${k} kosong.`
    }
  }

  if (!Array.isArray(obj.recommendations)) return 'recommendations bukan array.'
  if (obj.recommendations.length < 3 || obj.recommendations.length > 5) {
    return `recommendations harus 3-5 item, dapat ${obj.recommendations.length}.`
  }
  for (let i = 0; i < obj.recommendations.length; i++) {
    const r = obj.recommendations[i] as Record<string, unknown>
    if (!r || typeof r.text !== 'string' || r.text.trim().length === 0) {
      return `recommendations[${i}].text kosong.`
    }
  }

  return null
}


/**
 * Fetch data pasif 7 hari terakhir dari mood_logs / sleep_logs / journal_entries.
 * Best-effort: kalau gagal (tabel tidak ada / user belum pernah log), return null.
 */
async function fetchPassiveData(
  client: ReturnType<typeof createClient>,
  userId: string,
): Promise<IkigaiPassiveDataLike | null> {
  const since = new Date(Date.now() - 7 * 24 * 3600 * 1000).toISOString()

  try {
    const [moodRes, sleepRes, journalRes] = await Promise.all([
      client
        .from('mood_logs')
        .select('mood_score, created_at')
        .eq('user_id', userId)
        .gte('created_at', since),
      client
        .from('sleep_logs')
        .select('bed_time, wake_up_time, created_at')
        .eq('user_id', userId)
        .gte('created_at', since),
      client
        .from('journal_entries')
        .select('content, created_at')
        .eq('user_id', userId)
        .gte('created_at', since)
        .order('created_at', { ascending: false })
        .limit(10),
    ])

    const moodDaily: Record<string, number> = {}
    if (moodRes.data) {
      for (const row of moodRes.data) {
        const d = (row.created_at as string).slice(0, 10)
        const s = row.mood_score as number
        if (!moodDaily[d]) moodDaily[d] = 0
        moodDaily[d] = (moodDaily[d] + s) / 2 // rata-rata incremental
      }
    }

    const sleepHoursDaily: Record<string, number> = {}
    if (sleepRes.data) {
      for (const row of sleepRes.data) {
        const d = (row.created_at as string).slice(0, 10)
        const bed = new Date(row.bed_time as string).getTime()
        const wake = new Date(row.wake_up_time as string).getTime()
        const hours = Math.max(0, Math.min(24, (wake - bed) / 3_600_000))
        sleepHoursDaily[d] = (sleepHoursDaily[d] ?? 0) + hours
      }
    }

    const journalSnippets =
      journalRes.data?.map((j: { content: string }) => (j.content as string).trim()).filter(Boolean) ?? []

    const hasAny =
      Object.keys(moodDaily).length > 0 ||
      Object.keys(sleepHoursDaily).length > 0 ||
      journalSnippets.length > 0

    if (!hasAny) return null

    return { moodDaily, sleepHoursDaily, journalSnippets }
  } catch (err) {
    // Best-effort: jangan gagalkan pipeline kalau passive fetch error.
    console.warn('fetchPassiveData failed:', errorMessage(err))
    return null
  }
}
