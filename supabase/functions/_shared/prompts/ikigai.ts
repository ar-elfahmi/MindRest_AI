// =============================================================================
// Prompt library — Ikigai Report (TASK 3.2)
// =============================================================================
// Dipakai oleh Edge Function `generate-ikigai-report` (TASK 3.1).
//
// Tujuan:
//   - Meng-assembly prompt yang deterministik & terstruktur.
//   - Memisahkan prompt engineering dari HTTP handler (testable, swappable).
//   - Bahasa Indonesia, empatik soal Q5 (overthinking/insomnia), output Ikigai.
//
// ATURAN PROMPT (lihat ROADMAP.md section 5 "TASK 3.1 + 3.2"):
//   - Pakai bahasa Indonesia.
//   - Mention gap insomnia-overthinking (Q5) secara empatik.
//   - Output 4 lingkaran Ikigai dari Q1-Q4.
//   - 3-5 rekomendasi ACTIONABLE, bukan generik.
//   - Hindari diagnosis medis.
//   - Hasil akhir = JSON sesuai response_schema yang dikirim ke Gemini.
// =============================================================================


// ---------------------------------------------------------------------------
// Types — shape data yang dipakai prompt (TIDAK terikat DTO spesifik).
// Loose typing sengaja: Edge Function mapper yang bertanggun jawab membentuk
// objek ini dari row DB. Memudahkan unit-test prompt secara terisolasi.
// ---------------------------------------------------------------------------

/** Snapshot 6 pertanyaan assessment (lihat migration 002_create_ikigai.sql). */
export interface IkigaiAssessmentLike {
  q1_passion: string | null
  q2_skill: string | null
  q3_profession: string | null
  q4_mission: string | null
  q5_overthinking: string | null
  q6_satisfaction: number | null
}

/** Snapshot data pasif 7 hari terakhir (opsional). */
export interface IkigaiPassiveDataLike {
  /** Map tanggal 'YYYY-MM-DD' → rata-rata skor mood (skala 1-5). */
  moodDaily?: Record<string, number>
  /** Map tanggal 'YYYY-MM-DD' → total jam tidur. */
  sleepHoursDaily?: Record<string, number>
  /** Daftar ringkas journal 7 hari (sudah di-truncate untuk hemat token). */
  journalSnippets?: string[]
}


// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Bangun prompt teks panjang yang akan dikirim ke Gemini.
 *
 * @param assessment Row assessment terbaru user (wajib ada — function harus
 *                   guard error kalau null).
 * @param passiveData Data mood/sleep/journal 7 hari (opsional; null = tidak
 *                    ada / user baru).
 * @returns string — prompt final, siap di-pass ke `model.generateContent`.
 */
export function buildIkigaiPrompt(
  assessment: IkigaiAssessmentLike,
  passiveData?: IkigaiPassiveDataLike | null,
): string {
  const sections: string[] = []

  // 1) System / persona
  sections.push(SYSTEM_SECTION)

  // 2) Context user (assessment)
  sections.push(buildAssessmentSection(assessment))

  // 3) Context pasif (kalau ada)
  sections.push(buildPassiveSection(passiveData))

  // 4) Output contract (diulang di sini sebagai pengingat, walau
  //    response_schema di Gemini sudah jadi guard utama).
  sections.push(OUTPUT_CONTRACT_SECTION)

  // 5) Larangan
  sections.push(GUARDRAILS_SECTION)

  // 6) Quality rules
  sections.push(QUALITY_RULES_SECTION)

  return sections.join('\n\n')
}


// ---------------------------------------------------------------------------
// Internal — section builders
// ---------------------------------------------------------------------------

const SYSTEM_SECTION = `Kamu adalah coach Ikigai yang hangat, empatik, dan praktis.
Tugasmu: menghasilkan laporan Ikigai PERSONAL untuk pengguna MindRest berdasarkan
jawaban assessment 6 pertanyaan mereka, ditambah data pasif 7 hari terakhir
(jika tersedia).

GAYA BAHASA:
- Bahasa Indonesia yang natural dan hangat, bukan kaku/formal.
- Sapa pengguna dengan nada suportif, bukan menggurui.
- Akui bahwa Q5 (overthinking) bisa terkait kualitas tidur — ini sensitif,
  jangan diagosis, hanya normalisasi & arahkan ke rekomendasi.
- Hindari jargon psikologi/medis yang berlebihan.`

function buildAssessmentSection(a: IkigaiAssessmentLike): string {
  const safe = (s: string | null, fallback = '(tidak diisi)') =>
    s && s.trim() ? s.trim() : fallback

  return `JAWABAN ASSESSMENT PENGGUNA (6 pertanyaan):

1. PASSION — 3 hal yang paling kamu nikmati:
${safe(a.q1_passion)}

2. SKILL — hal yang kamu kuasai / sering dipuji orang lain:
${safe(a.q2_skill)}

3. PROFESSION — pekerjaan / aktivitas utama saat ini:
${safe(a.q3_profession)}

4. MISSION — bentuk kontribusi ke dunia yang kamu harapkan:
${safe(a.q4_mission)}

5. OVERTHINKING — topik yang paling sering memenuhi pikiran:
${safe(a.q5_overthinking)}
(Catatan: jika Q5 menyebut insomnia / pola tidur / pikiran malam hari,
 RAWAT secara empatik — akui itu beban nyata, normalkan, dan arahkan ke
 rekomendasi konkret yang gentle. JANGAN mendiagnosis gangguan tidur.)

6. KEPUASAN HIDUP — skala 1-10:
${a.q6_satisfaction ?? '(tidak diisi)'} (skala 1 = sangat rendah, 10 = sangat puas)`
}

function buildPassiveSection(p?: IkigaiPassiveDataLike | null): string {
  if (!p) {
    return 'DATA PASIF 7 HARI: tidak tersedia (pengguna baru / belum ada log).'
  }

  const mood = p.moodDaily
  const sleep = p.sleepHoursDaily
  const journal = p.journalSnippets

  const hasAny =
    (mood && Object.keys(mood).length > 0) ||
    (sleep && Object.keys(sleep).length > 0) ||
    (journal && journal.length > 0)

  if (!hasAny) {
    return 'DATA PASIF 7 HARI: tidak ada log yang tercatat minggu ini.'
  }

  // Ringkas jadi 1-2 baris per sumber supaya hemat token.
  const moodLine = mood && Object.keys(mood).length > 0
    ? `- Mood harian: ${formatMap(mood)}`
    : '- Mood harian: tidak ada.'

  const sleepLine = sleep && Object.keys(sleep).length > 0
    ? `- Jam tidur harian: ${formatMap(sleep)}`
    : '- Jam tidur: tidak ada.'

  const journalLine = journal && journal.length > 0
    ? `- Cuplikan jurnal (maks 5 entri pendek):\n${journal
        .slice(0, 5)
        .map((j, i) => `  ${i + 1}. ${truncate(j, 200)}`)
        .join('\n')}`
    : '- Jurnal: tidak ada.'

  return `DATA PASIF 7 HARI (gunakan sebagai nuansa, BUKAN fakta utama):
${moodLine}
${sleepLine}
${journalLine}`
}

const OUTPUT_CONTRACT_SECTION = `OUTPUT YANG DIHARAPKAN (JSON, sesuai response_schema):
- report_markdown      : string. Laporan naratif 350-600 kata dalam bahasa
                         Indonesia, hangat, terstruktur (paragraf atau
                         sub-bab Pendahuluan / Refleksi / Ikigai Kamu /
                         Penutup). Hindari bullet list di sini — ini prosa.
- ikigai_circles       : object dengan 4 field string (1-2 kalimat tiap
                         field), rangkuman hasil Q1-Q4:
                           • passion    = rangkuman Q1
                           • skill      = rangkuman Q2
                           • profession = rangkuman Q3
                           • mission    = rangkuman Q4
- recommendations      : array 3-5 item ACTIONABLE, BUKAN generik.
                         Tiap item: { "text": string }.
                         Contoh bagus: "Coba blok kalender 90 menit tiap
                         Kamis sore untuk eksplorasi side project X."
                         Contoh buruk (HINDARI): "Ikuti passion kamu."`

const GUARDRAILS_SECTION = `LARANGAN KERAS:
- JANGAN diagnosis medis/psikologis (depresi, insomnia kronis, anxiety
  disorder, ADHD, dll). Hanya normalisasi & arahkan ke pola hidup sehat.
- JANGAN klaim kepastian tentang masa depan / karir / kesehatan user.
- JANGAN sebut nama model AI, training data, atau internal process.
- JANGAN output field di luar schema. JSON HARUS valid dan bisa di-parse.
- JANGAN output rekomendasi di luar array 3-5 item.`

const QUALITY_RULES_SECTION = `ATURAN KUALITAS:
- Jika Q5 menyebut topik berat (kematian, keuangan, hubungan), MULAI
  laporan dengan kalimat empatik singkat sebelum masuk analisis.
- Rekomendasi HARUS spesifik & actionable: sebut kapan/di mana/langkah
  pertama. Hindari "perbanyak tidur", "kelola stres", dll tanpa detail.
- Hubungkan rekomendasi dengan jawaban Q1-Q4 (jangan rekomendasi random).
- Skala kepuasan (Q6) rendah (1-4) → laporan lebih lembut, akui berat.
  Skala tinggi (8-10) → laporan boleh lebih reflektif & ringan.
- Total JSON: usahakan di bawah ~3000 karakter supaya payload DB tetap kecil.`


// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function formatMap(m: Record<string, number>): string {
  const keys = Object.keys(m).sort()
  // Tampilkan ringkas: hanya angka dibulatkan 1 desimal.
  return keys.map((k) => `${k}:${Number(m[k]).toFixed(1)}`).join(', ')
}

function truncate(s: string, max: number): string {
  if (s.length <= max) return s
  return s.slice(0, max - 1) + '…'
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

/** Schema response Gemini untuk report Ikigai. */
export const IKIGAI_RESPONSE_SCHEMA = {
  type: 'object',
  properties: {
    report_markdown: {
      type: 'string',
      description:
        'Laporan naratif 350-600 kata bahasa Indonesia, hangat, terstruktur.',
    },
    ikigai_circles: {
      type: 'object',
      properties: {
        passion: {
          type: 'string',
          description: 'Rangkuman Q1 — 3 hal yang paling dinikmati.',
        },
        skill: {
          type: 'string',
          description: 'Rangkuman Q2 — hal yang dikuasai / sering dipuji.',
        },
        profession: {
          type: 'string',
          description: 'Rangkuman Q3 — pekerjaan / aktivitas utama.',
        },
        mission: {
          type: 'string',
          description: 'Rangkuman Q4 — kontribusi ke dunia yang diharapkan.',
        },
      },
      required: ['passion', 'skill', 'profession', 'mission'],
    },
    recommendations: {
      type: 'array',
      description: '3-5 item rekomendasi actionable spesifik.',
      items: {
        type: 'object',
        properties: {
          text: {
            type: 'string',
            description: 'Rekomendasi konkret (1-2 kalimat).',
          },
        },
        required: ['text'],
      },
      minItems: 3,
      maxItems: 5,
    },
  },
  required: ['report_markdown', 'ikigai_circles', 'recommendations'],
} as const
