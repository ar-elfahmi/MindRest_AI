// =============================================================================
// Prompt library — Sleep Insight (T-005 / FR-014)
// =============================================================================
// Dipakai oleh Edge Function `generate-sleep-insight`.
//
// Tujuan:
//   - Meng-assembly prompt yang deterministik & terstruktur.
//   - Memisahkan prompt engineering dari HTTP handler (testable, swappable).
//   - Pola: reuse pattern `ikigai.ts` (system → context → contract → guardrails).
//
// ATURAN PROMPT:
//   - Bahasa Indonesia.
//   - Output JSON dengan 3 list rekomendasi: activities / foods / music.
//   - Setiap list 3-5 item ACTIONABLE & SPESIFIK (bukan generik).
//   - Hindari diagnosis medis.
//   - TIDAK sebut nama model AI.
// =============================================================================


// ---------------------------------------------------------------------------
// Types — shape data yang dipakai prompt (TIDAK terikat DTO spesifik).
// Loose typing sengaja: Edge Function mapper yang bertanggung jawab membentuk
// objek ini dari row DB.
// ---------------------------------------------------------------------------

/** Sleep log ringkas yang sudah dinormalisasi untuk prompt. */
export interface SleepLogLike {
  /** ISO timestamp kapan user mulai tidur. */
  bed_time: string
  /** ISO timestamp kapan user bangun. */
  wake_up_time: string
  /** Kualitas tidur: POOR | FAIR | GOOD | EXCELLENT. */
  sleep_quality: string
  /** ISO timestamp entry dibuat (untuk filter "last N days"). */
  created_at: string
}

/** Ringkasan agregat dari N log (untuk konteks prompt). */
export interface SleepAggregateLike {
  /** Jumlah log dalam window. */
  totalLogs: number
  /** Rata-rata durasi tidur (jam). Null kalau totalLogs = 0. */
  avgDurationHours: number | null
  /** Rata-rata jam mulai tidur (desimal 0-23.99). Null kalau totalLogs = 0. */
  avgBedTimeHour: number | null
  /** Rata-rata jam bangun (desimal 0-23.99). Null kalau totalLogs = 0. */
  avgWakeTimeHour: number | null
  /** Map kualitas tidur → jumlah entry. */
  qualityCounts: Record<string, number>
  /** Daftar log individual (sudah di-truncate agar hemat token). Maks 7. */
  recentLogs: Array<{
    date: string
    bed: string
    wake: string
    quality: string
  }>
}


// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Bangun prompt teks panjang yang akan dikirim ke Gemini.
 *
 * @param periodDays Window analisis (mis. 7 atau 30).
 * @param aggregate  Ringkasan agregat dari N log (wajib — function guard kalau null).
 * @returns string — prompt final, siap di-pass ke `model.generateContent`.
 */
export function buildSleepInsightPrompt(
  periodDays: number,
  aggregate: SleepAggregateLike,
): string {
  const sections: string[] = []

  sections.push(SYSTEM_SECTION)
  sections.push(buildAggregateSection(periodDays, aggregate))
  sections.push(OUTPUT_CONTRACT_SECTION)
  sections.push(GUARDRAILS_SECTION)
  sections.push(QUALITY_RULES_SECTION)

  return sections.join('\n\n')
}


// ---------------------------------------------------------------------------
// Internal — section builders
// ---------------------------------------------------------------------------

const SYSTEM_SECTION = `Kamu adalah asisten kesehatan tidur & gaya hidup yang hangat, praktis, dan aman.
Tugasmu: menghasilkan rekomendasi personal untuk pengguna aplikasi "MindRest" berdasarkan
data tidur mereka (7-30 hari terakhir).

GAYA BAHASA:
- Bahasa Indonesia yang natural, hangat, suportif. Bukan menggurui.
- Beri apresiasi jika data menunjukkan pola positif; jangan menghakimi jika
  data kurang ideal.
- Ingatkan bahwa ini saran umum, bukan pengganti konsultasi profesional.`

function buildAggregateSection(days: number, a: SleepAggregateLike): string {
  const qualityBreakdown = Object.entries(a.qualityCounts)
    .map(([q, n]) => `${q}=${n}`)
    .join(', ')

  const avgBed = a.avgBedTimeHour !== null ? formatHour(a.avgBedTimeHour) : 'n/a'
  const avgWake = a.avgWakeTimeHour !== null ? formatHour(a.avgWakeTimeHour) : 'n/a'
  const avgDur = a.avgDurationHours !== null
    ? `${a.avgDurationHours.toFixed(1)} jam`
    : 'n/a'

  const recentBlock = a.recentLogs.length === 0
    ? '(tidak ada log individual untuk dirinci)'
    : a.recentLogs
        .map(
          (l, i) =>
            `  ${i + 1}. ${l.date} — bed ${l.bed}, wake ${l.wake}, quality ${l.quality}`,
        )
        .join('\n')

  return `DATA TIDUR PENGGUNA (${days} hari terakhir):

RINGKASAN STATISTIK:
- Jumlah log              : ${a.totalLogs}
- Rata-rata durasi tidur  : ${avgDur}
- Rata-rata jam mulai tidur: ${avgBed}
- Rata-rata jam bangun    : ${avgWake}
- Distribusi kualitas     : ${qualityBreakdown || 'tidak ada'}

ENTRI TERBARU (maks 7):
${recentBlock}`
}

const OUTPUT_CONTRACT_SECTION = `OUTPUT YANG DIHARAPKAN (JSON sesuai response_schema):
- summary         : string. Ringkasan 1-2 kalimat (maks 200 karakter)
                    dalam bahasa Indonesia tentang pola tidur user.
                    Contoh: "Tidur Anda cukup konsisten 7-8 jam, namun
                    jam mulai tidur sering larut (>23:30)."
- recommendations : object dengan 3 field, masing-masing array 3-5 item
                    ACTIONABLE dan SPESIFIK:
  • activities : array 3-5 aktivitas ringan yang cocok dilakukan user
                 untuk mendukung kualitas tidur (mis. jalan pagi 15 menit,
                 stretching sebelum tidur, journaling 5 menit, dll).
  • foods      : array 3-5 rekomendasi makanan/minuman yang mendukung
                 tidur (mis. pisang, teh chamomile, kurma, almond).
                 HINDARI kafein, alkohol, makanan berat.
  • music      : array 3-5 rekomendasi musik/genre relaksasi untuk
                 pengantar tidur (mis. lo-fi, ambient, classical,
                 nature sound, dll).

  Tiap item dalam array: { "text": string }. Text 1-2 kalimat, spesifik,
  hindari kalimat generik. Sertakan waktu/durasi bila relevan.

Contoh item bagus:
  { "text": "Jalan kaki ringan 15 menit di bawah sinar matahari pagi
              (06:30-07:00) untuk membantu ritme sirkadian." }

Contoh item BURUK (HINDARI):
  { "text": "Olahraga teratur." }
  { "text": "Makan makanan sehat." }`

const GUARDRAILS_SECTION = `LARANGAN KERAS:
- JANGAN diagnosis medis (insomnia kronis, sleep apnea, depresi, dll).
- JANGAN meresepkan obat, suplemen spesifik, atau terapi.
- JANGAN klaim kepastian tentang kondisi kesehatan user.
- JANGAN sebut nama model AI, training data, atau internal process.
- JANGAN output field di luar schema. JSON HARUS valid dan bisa di-parse.
- JANGAN rekomendasikan kafein, alkohol, nikotin, atau makanan/minuman
  yang dapat mengganggu tidur.`

const QUALITY_RULES_SECTION = `ATURAN KUALITAS:
- Pola tidur POOR (kebanyakan entry POOR/FAIR) → rekomendasi lebih
  lembut & suportif, akui tantangannya.
- Pola tidur EXCELLENT → boleh eksplorasi variasi / kebiasaan baru.
- Inkonsistensi jam tidur (std dev tinggi) → rekomendasi fokus ke sleep
  hygiene & rutinitas.
- Rekomendasi HARUS bisa dilakukan user pada umumnya (tidak butuh alat
  mahal / akses khusus).
- Total JSON: usahakan di bawah ~2000 karakter.`


// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Format jam desimal (mis. 22.5) ke "22:30". */
function formatHour(decimalHour: number): string {
  const h = Math.floor(decimalHour)
  const m = Math.round((decimalHour - h) * 60)
  return `${String(h).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`
}


// ---------------------------------------------------------------------------
// Re-export schema response untuk Gemini
// ---------------------------------------------------------------------------
//
// Schema ini HARUS match dengan output contract di atas.
// Dipakai oleh index.ts untuk `generationConfig.responseSchema`.
// (Schema didefinisikan di sini — satu sumber kebenaran — supaya perubahan
//  prompt + schema selalu sinkron.)
// ---------------------------------------------------------------------------

/** Schema response Gemini untuk sleep insight. */
export const SLEEP_INSIGHT_RESPONSE_SCHEMA = {
  type: 'object',
  properties: {
    summary: {
      type: 'string',
      description:
        'Ringkasan 1-2 kalimat (maks 200 karakter) tentang pola tidur user.',
    },
    recommendations: {
      type: 'object',
      properties: {
        activities: {
          type: 'array',
          description: '3-5 aktivitas ringan yang mendukung kualitas tidur.',
          items: {
            type: 'object',
            properties: {
              text: {
                type: 'string',
                description: 'Rekomendasi aktivitas (1-2 kalimat).',
              },
            },
            required: ['text'],
          },
          minItems: 3,
          maxItems: 5,
        },
        foods: {
          type: 'array',
          description: '3-5 rekomendasi makanan/minuman pendukung tidur.',
          items: {
            type: 'object',
            properties: {
              text: {
                type: 'string',
                description: 'Rekomendasi makanan (1-2 kalimat).',
              },
            },
            required: ['text'],
          },
          minItems: 3,
          maxItems: 5,
        },
        music: {
          type: 'array',
          description: '3-5 rekomendasi musik relaksasi pengantar tidur.',
          items: {
            type: 'object',
            properties: {
              text: {
                type: 'string',
                description: 'Rekomendasi musik (1-2 kalimat).',
              },
            },
            required: ['text'],
          },
          minItems: 3,
          maxItems: 5,
        },
      },
      required: ['activities', 'foods', 'music'],
    },
  },
  required: ['summary', 'recommendations'],
} as const
