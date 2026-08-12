// =============================================================================
// Edge Function: generate-sleep-insight
// =============================================================================
// T-005 (FR-014 — Rekomendasi aktivitas/makanan/musik dari riwayat tidur).
//
// Tujuan:
//   1. Verify JWT user → dapat user_id.
//   2. Fetch sleep_logs milik user (default 7 hari terakhir, max 30).
//   3. Hitung ringkasan agregat (avg durasi, avg bed/wake hour, quality counts).
//   4. Bangun prompt via _shared/prompts/sleep_insight.ts.
//   5. Call Gemini dengan JSON mode + response_schema.
//   6. Parse + enrich (tambah id per rekomendasi, period_days).
//   7. INSERT ke sleep_insights via SERVICE ROLE (bypass RLS).
//   8. Return insight JSON ke client.
//
// ENV yang dipakai:
//   - SUPABASE_URL              (otomatis)
//   - SUPABASE_ANON_KEY         (otomatis)
//   - SUPABASE_SERVICE_ROLE_KEY (WAJIB)
//   - GEMINI_API_KEY            (WAJIB)
//
// BACA DULU:
//   - supabase/functions/test-gemini/index.ts (pola error classifier)
//   - supabase/functions/generate-ikigai-report/index.ts (pola JWT verify
//     + service-role fetch + JSON schema — arsitektur persis)
//   - supabase/functions/_shared/prompts/sleep_insight.ts
//   - supabase/migrations/005_sleep_insights.sql
// =============================================================================

import { corsHeaders } from '../_shared/cors.ts'
import {
  buildSleepInsightPrompt,
  SLEEP_INSIGHT_RESPONSE_SCHEMA,
  type SleepAggregateLike,
  type SleepLogLike,
} from '../_shared/prompts/sleep_insight.ts'

// Lock SDK ke major version yang sama dengan function lain.
import { GoogleGenerativeAI } from 'npm:@google/generative-ai@^0.21'
import { createClient } from 'npm:@supabase/supabase-js@^2.45'


// ---------------------------------------------------------------------------
// Konfigurasi
// ---------------------------------------------------------------------------

// Model: gemini-3.5-flash — rekomendasi 3 list + summary (~600 kata output).
// Lite kurang konsisten untuk JSON schema 3 sub-array.
const GEMINI_MODEL = 'gemini-3.5-flash'

// Window analisis: default 7 hari, max 30 (sesuai spec task).
const DEFAULT_PERIOD_DAYS = 7
const MAX_PERIOD_DAYS = 30

// Batas payload output Gemini (sanity check). 30 KB cukup untuk 3 list
// @ 5 item + summary. Kalau lebih = indikasi output rusak / hallucination.
const MAX_INSIGHT_BYTES = 30_000

// Batas jumlah log individual yang di-include di prompt.
// Hemat token — hanya 7 entry terakhir, sisanya di-aggregate.
const MAX_RECENT_LOGS_IN_PROMPT = 7


// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface GenerateSleepInsightRequest {
  /** Window analisis dalam hari. Opsional (default 7). */
  period_days?: number | null
}

interface SleepInsightOutput {
  summary: string
  recommendations: {
    activities: Array<{ text: string }>
    foods: Array<{ text: string }>
    music: Array<{ text: string }>
  }
}

type OkResponse = {
  ok: true
  data: {
    insight: {
      id: string
      summary: string
      recommendations: {
        activities: Array<{ id: string; text: string }>
        foods: Array<{ id: string; text: string }>
        music: Array<{ id: string; text: string }>
      }
      period_days: number
      generated_at: string
    }
    /** Total log yang dipakai untuk analisis (untuk info UI). */
    logs_analyzed: number
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
  // Pakai service role key terpisah untuk query/insert (lihat STEP 5+).
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
          error: { code: 'invalid_jwt', message: 'JWT tidak valid atau kadaluarsa.' },
        },
        401,
      )
    }
    userId = data.user.id
  } catch (err) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'auth_verification_failed', message: errorMessage(err) },
      },
      401,
    )
  }

  // ─── STEP 3: Parse + validate body ─────────────────────────────────────
  let body: GenerateSleepInsightRequest
  try {
    const raw = await req.json().catch(() => ({}))
    body = parseBody(raw)
  } catch (err) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'invalid_body', message: errorMessage(err) },
      },
      400,
    )
  }

  const periodDays = clampPeriodDays(body.period_days ?? DEFAULT_PERIOD_DAYS)

  // ─── STEP 4: Fetch sleep_logs (admin client, service role) ───────────
  // Pakai service role → bypass RLS, filter manual by user_id.
  // Alasan: kita butuh query tanpa RLS untuk aggregate window panjang.
  // Aman karena user_id sudah verified dari JWT.
  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  })

  const since = new Date(Date.now() - periodDays * 24 * 3600 * 1000).toISOString()

  const { data: logs, error: logsErr } = await adminClient
    .from('sleep_logs')
    .select('bed_time, wake_up_time, sleep_quality, created_at')
    .eq('user_id', userId)
    .gte('created_at', since)
    .order('created_at', { ascending: false })

  if (logsErr) {
    return jsonResponse(
      {
        ok: false,
        error: { code: 'sleep_logs_fetch_failed', message: logsErr.message },
      },
      500,
    )
  }

  if (!logs || logs.length === 0) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'no_sleep_logs',
          message:
            `Belum ada log tidur dalam ${periodDays} hari terakhir. Tambah log tidur dulu untuk mendapat insight personal.`,
        },
      },
      404,
    )
  }

  // ─── STEP 5: Hitung ringkasan agregat ─────────────────────────────────
  const aggregate = computeAggregate(logs as SleepLogLike[])

  // ─── STEP 6: Bangun prompt ────────────────────────────────────────────
  const prompt = buildSleepInsightPrompt(periodDays, aggregate)

  // ─── STEP 7: Call Gemini (JSON mode + schema) ────────────────────────
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
      responseSchema: SLEEP_INSIGHT_RESPONSE_SCHEMA,
      // Suhu moderate untuk variasi rekomendasi (tapi tetap deterministik
      // untuk konsistensi kategori).
      temperature: 0.7,
      // Naikkan maxOutputTokens supaya 3 list @ 5 item + summary tidak
      // terpotong. 4096 cukup untuk output terstruktur + buffer thinking.
      maxOutputTokens: 4096,
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
    // Observability: log raw output untuk debugging Gemini quality.
    console.log('[generate-sleep-insight] raw length:', rawText.length)
    console.log('[generate-sleep-insight] raw head (500):', rawText.slice(0, 500))
    console.log('[generate-sleep-insight] raw tail (200):', rawText.slice(-200))
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

  if (rawText.length > MAX_INSIGHT_BYTES) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'output_too_large',
          message: `Output Gemini ${rawText.length} bytes > ${MAX_INSIGHT_BYTES}.`,
        },
      },
      502,
    )
  }

  // ─── STEP 8: Parse + validate JSON ────────────────────────────────────
  let parsed: SleepInsightOutput
  try {
    parsed = JSON.parse(rawText)
  } catch (err) {
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

  // Enrich: tambah id (UUID) per rekomendasi untuk client-side rendering.
  const enrichedRecs = {
    activities: parsed.recommendations.activities.map((r) => ({
      id: crypto.randomUUID(),
      text: r.text,
    })),
    foods: parsed.recommendations.foods.map((r) => ({
      id: crypto.randomUUID(),
      text: r.text,
    })),
    music: parsed.recommendations.music.map((r) => ({
      id: crypto.randomUUID(),
      text: r.text,
    })),
  }

  // ─── STEP 9: INSERT ke sleep_insights (service role, bypass RLS) ─────
  // Pola sama dengan generate-ikigai-report: user tidak bisa insert
  // sendiri (purity arsitektur — semua INSERT via EF).
  const { data: insertedRow, error: insErr } = await adminClient
    .from('sleep_insights')
    .insert({
      user_id: userId,
      period_days: periodDays,
      recommendations: enrichedRecs,
      summary: parsed.summary,
    })
    .select('id, generated_at')
    .single()

  if (insErr || !insertedRow) {
    return jsonResponse(
      {
        ok: false,
        error: {
          code: 'insert_failed',
          message: insErr?.message ?? 'Insert ke sleep_insights gagal.',
        },
      },
      500,
    )
  }

  return jsonResponse(
    {
      ok: true,
      data: {
        insight: {
          id: insertedRow.id,
          summary: parsed.summary,
          recommendations: enrichedRecs,
          period_days: periodDays,
          generated_at: insertedRow.generated_at,
        },
        logs_analyzed: logs.length,
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
 * Klasifikasi kasar error Gemini. POLA PERSIS dengan chat-gemini &
 * generate-ikigai-report (regex word-boundary).
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

/** Parse + sanitize body. Toleran terhadap field opsional. */
function parseBody(raw: unknown): GenerateSleepInsightRequest {
  if (!raw || typeof raw !== 'object') {
    throw new Error('Body bukan object.')
  }
  const obj = raw as Record<string, unknown>
  const periodDays =
    typeof obj.period_days === 'number' && Number.isFinite(obj.period_days)
      ? obj.period_days
      : null
  return { period_days: periodDays }
}

/** Clamp period_days ke range valid [1, MAX_PERIOD_DAYS]. */
function clampPeriodDays(input: number): number {
  if (!Number.isFinite(input)) return DEFAULT_PERIOD_DAYS
  return Math.max(1, Math.min(MAX_PERIOD_DAYS, Math.floor(input)))
}

/** Validasi ringan output AI sebelum di-INSERT ke DB. */
function validateAiOutput(o: unknown): string | null {
  if (!o || typeof o !== 'object') return 'Output bukan object.'

  const obj = o as Record<string, unknown>

  if (typeof obj.summary !== 'string' || obj.summary.trim().length === 0) {
    return 'summary kosong.'
  }
  if (obj.summary.length > 300) {
    return `summary terlalu panjang (${obj.summary.length} chars, max 300).`
  }

  const recs = obj.recommendations as Record<string, unknown> | undefined
  if (!recs || typeof recs !== 'object') return 'recommendations bukan object.'

  for (const key of ['activities', 'foods', 'music']) {
    const arr = recs[key]
    if (!Array.isArray(arr)) return `recommendations.${key} bukan array.`
    if (arr.length < 3 || arr.length > 5) {
      return `recommendations.${key} harus 3-5 item, dapat ${arr.length}.`
    }
    for (let i = 0; i < arr.length; i++) {
      const item = arr[i] as Record<string, unknown>
      if (!item || typeof item.text !== 'string' || item.text.trim().length === 0) {
        return `recommendations.${key}[${i}].text kosong.`
      }
    }
  }

  return null
}

/**
 * Hitung ringkasan agregat dari N log untuk konteks prompt.
 *
 * @param logs Sudah di-fetch dari DB, urut DESC by created_at.
 */
function computeAggregate(logs: SleepLogLike[]): SleepAggregateLike {
  const totalLogs = logs.length

  // Durasi (jam) per entry
  const durations: number[] = []
  const bedHours: number[] = []
  const wakeHours: number[] = []
  const qualityCounts: Record<string, number> = {}

  for (const row of logs) {
    const bed = new Date(row.bed_time).getTime()
    const wake = new Date(row.wake_up_time).getTime()
    if (Number.isFinite(bed) && Number.isFinite(wake) && wake > bed) {
      durations.push((wake - bed) / 3_600_000)
    }

    const bedDate = new Date(row.bed_time)
    const wakeDate = new Date(row.wake_up_time)
    if (!isNaN(bedDate.getTime())) {
      bedHours.push(bedDate.getUTCHours() + bedDate.getUTCMinutes() / 60)
    }
    if (!isNaN(wakeDate.getTime())) {
      wakeHours.push(wakeDate.getUTCHours() + wakeDate.getUTCMinutes() / 60)
    }

    const q = (row.sleep_quality ?? '').toUpperCase()
    if (q) qualityCounts[q] = (qualityCounts[q] ?? 0) + 1
  }

  const avg = (arr: number[]): number | null =>
    arr.length === 0 ? null : arr.reduce((a, b) => a + b, 0) / arr.length

  // Recent logs: maks 7 entry terbaru, formatted ringkas untuk prompt.
  const recentLogs = logs.slice(0, MAX_RECENT_LOGS_IN_PROMPT).map((row) => ({
    date: (row.created_at ?? '').slice(0, 10),
    bed: formatTimeShort(row.bed_time),
    wake: formatTimeShort(row.wake_up_time),
    quality: (row.sleep_quality ?? 'UNKNOWN').toUpperCase(),
  }))

  return {
    totalLogs,
    avgDurationHours: avg(durations),
    avgBedTimeHour: avg(bedHours),
    avgWakeTimeHour: avg(wakeHours),
    qualityCounts,
    recentLogs,
  }
}

/** Format ISO timestamp jadi "HH:MM" UTC (ringkas untuk prompt). */
function formatTimeShort(iso: string): string {
  const d = new Date(iso)
  if (isNaN(d.getTime())) return '??:??'
  return `${String(d.getUTCHours()).padStart(2, '0')}:${String(d.getUTCMinutes()).padStart(2, '0')}`
}
