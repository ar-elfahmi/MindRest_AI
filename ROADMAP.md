# MindRest_AI — Roadmap Eksekusi

> **Deliverable akhir dari sesi grilling (konsultasi).**
>
> **🆕 Status source of truth pindah ke `CHANGELOG.md` (milestone per-task) + `ORCHESTRATION.md` (alur kerja + task pipeline).**
> ROADMAP ini sekarang = **strategi & sequencing**, bukan eksekusi.
> Task siap copy-paste ada di folder `TASKS/`.
>
> **Aturan:** Sebelum menjalankan task apapun, baca `ORCHESTRATION.md` + `CHANGELOG.md` + section 0 `AUDIT.md` (TEMUAN KRITIS).

---

## 1. Executive Summary — 6 Keputusan Final

| # | Pertanyaan | Keputusan |
|---|---|---|
| Q1 | Tujuan produk? | **Ikigai discovery** sebagai output utama; mood/sleep/journal = feeder data |
| Q2 | MVP pertama? | **AI foundation → Ikigai report** (bukan Sleep Therapy duluan) |
| Q3 | Output Ikigai? | **Laporan markdown + 4 Lingkaran visual + 3-5 Rekomendasi aktivitas** |
| Q4 | Data input Ikigai? | **Hybrid**: 6 pertanyaan assessment + passive data opsional (mood/sleep/journal 7 hari) |
| Q5 | Arsitektur AI? | **Supabase Edge Function (Deno TS)**, Free tier, `GEMINI_API_KEY` di secret server-side |
| Q6 | Bottom sheet? | **Simplifikasi jadi quick-mood only** (emoji 1-5, hapus slider tidur) |

**North Star (2-4 minggu):** User onboard → isi profil + 6 pertanyaan assessment → AI generate **laporan Ikigai personal** dengan 4 lingkaran + rekomendasi. Ini yang bisa di-demo & dijual.

---

## 2. Arsitektur Target

```
┌─────────────────────────────────────────────────────────────┐
│  ANDROID APP (Jetpack Compose)                              │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐ │
│  │  Onboarding │  │  Ikigai      │  │  Daily check-in    │ │
│  │  Profil     │  │  Assessment  │  │  (mood only)       │ │
│  │  Kesehatan  │  │  (6 Q)       │  │  ← bottom sheet    │ │
│  └──────┬──────┘  └──────┬───────┘  └─────────┬──────────┘ │
│         │                │                     │            │
│         ▼                ▼                     ▼            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Supabase Postgres                                    │  │
│  │  profiles(+) · ikigai_assessments · ikigai_reports   │  │
│  │  mood_logs · sleep_logs · journal_entries (existing) │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS + JWT
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  SUPABASE EDGE FUNCTIONS (Deno TS)                          │
│                                                              │
│  ┌────────────────────────────┐  ┌───────────────────────┐ │
│  │ generate-ikigai-report     │  │ daily-notification    │ │
│  │ - verify JWT               │  │ (Sleep Therapy, W4)   │ │
│  │ - rate limit (1x/hari)     │  │ - cron pg_cron        │ │
│  │ - fetch assessment+logs    │  │ - profile + mood ctx  │ │
│  │ - assemble prompt          │  │ - generate activity   │ │
│  │ - call Gemini (secret)     │  │ - insert notification │ │
│  │ - parse JSON, save report  │  └───────────────────────┘ │
│  │ - return report            │                            │
│  └────────────────────────────┘                            │
│                                                              │
│  GEMINI_API_KEY (Supabase secret, BUKAN di Android)        │
└─────────────────────────────────────────────────────────────┘
```

**Prinsip kunci:**
- 🔒 `GEMINI_API_KEY` **tidak pernah** menyentuh Android client. Hanya di Supabase secret.
- 🚦 Rate limit di Edge Function, bukan di client.
- 📊 Semua prompt assembly di server (clean, testable, no client logic sprawl).

---

## 3. Roadmap 4 Minggu

### 🔴 MINGGU 1 — Damage Control + AI Foundation

| Task | Prioritas | Output |
|---|---|---|
| **1.1** Fix bottom sheet (stop data noise) | 🔴 URGENT | `sleep_logs` berhenti terkontaminasi |
| **1.2** Aktifkan dead code screens | 🔴 URGENT | Route MoodTracking + SleepTracking hidup |
| **1.3** Setup Supabase Edge Function env | 🟡 Penting | `supabase functions deploy` jalan |
| **1.4** Gemini hello-world function | 🟡 Penting | Edge Function bisa call Gemini |

### 🟠 MINGGU 2 — Data Foundation (Ikigai Input)

| Task | Prioritas | Output |
|---|---|---|
| **2.1** Schema migration (profiles extend + ikigai tables) | 🔴 Blocker | Tabel baru live di Supabase |
| **2.2** Onboarding profil kesehatan UI | 🟡 Penting | User isi BB/TB/pekerjaan/keluhan |
| **2.3** Ikigai assessment UI (6 pertanyaan) | 🔴 Blocker | User isi 6 Q → save ke `ikigai_assessments` |
| **2.4** Repository + ViewModel untuk Ikigai | 🔴 Blocker | CRUD assessment jalan |

### 🟢 MINGGU 3 — AI Pipeline (Ikigai Report)

| Task | Prioritas | Output |
|---|---|---|
| **3.1** Edge Function `generate-ikigai-report` | 🔴 Core | AI generate report → save DB (✅ 2026-08-10) |
| **3.2** Prompt library + JSON schema | 🔴 Core | Output terstruktur `{report, circles, recs}` (✅ 2026-08-10) |
| **3.3** Ikigai report display UI | 🔴 Core | Tampilkan laporan + 4 lingkaran + rekomendasi checkbox |

### 🔵 MINGGU 4 — Polish + Sleep Therapy Preview

| Task | Prioritas | Output |
|---|---|---|
| **4.1** Refresh ikigai dengan passive data | 🟢 Nice | Versi 2+ report (assessment + logs 7 hari) |
| **4.2** Daily notification Edge Function + pg_cron | 🟡 Demo | Notifikasi AI harian (Sleep Therapy preview) |
| **4.3** Wiring notifikasi ke app | 🟡 Demo | User terima push, tap → buka app |
| **4.4** Cleanup SleepHub hardcode sections | 🟢 Nice | Hapus/buang section yang tidak punya data source |

---

## 4. Database Migration Plan

### 4.1 Extend `profiles` (untuk Sleep Therapy)

```sql
-- File: supabase/migrations/001_extend_profiles.sql
ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS height_cm INT,
  ADD COLUMN IF NOT EXISTS weight_kg INT,
  ADD COLUMN IF NOT EXISTS occupation TEXT,
  ADD COLUMN IF NOT EXISTS complaints TEXT[] DEFAULT '{}';
-- complaints = array, mis. {'insomnia', 'overthinking', 'anxiety'}
```

### 4.2 Buat tabel Ikigai

```sql
-- File: supabase/migrations/002_create_ikigai.sql

CREATE TABLE IF NOT EXISTS ikigai_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  q1_passion TEXT,        -- "3 hal yang paling kamu nikmati"
  q2_skill TEXT,          -- "hal yang kamu jago / sering dipuji"
  q3_profession TEXT,     -- "pekerjaan / aktivitas utama"
  q4_mission TEXT,        -- "bentuk kontribusi ke dunia"
  q5_overthinking TEXT,   -- "paling sering overthinking soal"
  q6_satisfaction INT,    -- 1-10 skala kepuasan hidup
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ikigai_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  assessment_id UUID NOT NULL REFERENCES ikigai_assessments(id) ON DELETE CASCADE,
  report_markdown TEXT NOT NULL,         -- laporan text dari AI
  ikigai_circles JSONB NOT NULL,         -- {passion, skill, profession, mission}
  recommendations JSONB NOT NULL,        -- [{id, text, done}] array 3-5 item
  version INT NOT NULL DEFAULT 1,        -- 1=baseline, 2+=refreshed
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index + RLS
CREATE INDEX idx_ikigai_assessments_user ON ikigai_assessments(user_id, created_at DESC);
CREATE INDEX idx_ikigai_reports_user ON ikigai_reports(user_id, generated_at DESC);

ALTER TABLE ikigai_assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE ikigai_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own assessments" ON ikigai_assessments
  FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can read own reports" ON ikigai_reports
  FOR SELECT USING (auth.uid() = user_id);
-- NOTE: INSERT ke ikigai_reports HANYA dari Edge Function (service role),
-- jadi user tidak bisa inject report palsu.
```

---

## 5. Task Specs (siap copy ke AI agent)

Format tiap task: Goal → Read First → Scope → DO → DON'T → Acceptance.

---

### 🔴 TASK 1.1 — Fix Bottom Sheet (URGENT: stop data noise)

```
TASK: Simplifikasi DailyCheckInBottomSheet jadi QUICK-MOOD ONLY.
      Hapus slider tidur (sumber data sleep_logs yang palsu/noise).

WHY (penting!): Saat ini handler onSave memanggil deriveSleepFromDuration()
yang MENEBAK bed_time/wake_time dari slider. Semua row sleep_logs yang
masuk lewat bottom sheet = data tidak akurat, akan mengontaminasi input AI.

READ FIRST:
  - AUDIT.md section 0.3 (data tidur palsu)
  - app/src/main/java/com/example/features/home/presentation/screen/DailyCheckInBottomSheet.kt
  - app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt L254-280 (handler onSave)
  - app/src/main/java/com/example/features/mood/presentation/screen/MoodTrackingScreen.kt (lihat moodEmojis pattern)

SCOPE (boleh diedit):
  - DailyCheckInBottomSheet.kt
  - HomeScreen.kt (handler onSave + helper functions)

DO:
  1. Ganti chip emosi (availableEmotions list) → emoji picker 1-5,
     reuse pattern dari moodEmojis di MoodTrackingScreen:
       1→😢 2→🙁 3→😐 4→🙂 5→😁
  2. HAPUS section "Kualitas istirahat semalam" (circular dial + slider).
  3. Ubah signature onSave dari:
       onSave(emotions: List<String>, sleepDurationHours: Float)
     menjadi:
       onSave(moodScore: Int)
  4. Update HomeScreen handler:
       onSave = { moodScore ->
         showCheckInSheet = false
         hasCheckedInToday = true
         moodViewModel.saveMoodScore(moodScore)
         // HAPUS: sleepViewModel.saveSleepLog(...) + deriveSleepFromDuration()
         // Pertahankan snackbar feedback (positive vs negative)
       }
  5. HAPUS function mapEmotionsToMoodScore() dan deriveSleepFromDuration()
     dari HomeScreen.kt (sudah tidak dipakai).
  6. Update tombol footer: text jadi "Simpan Mood" (bukan "Simpan Jurnal Pagi").
  7. Pertahankan testTag untuk UI testing (update tag sesuai struktur baru).

DON'T:
  - Jangan ubah MoodRepository / MoodViewModel (saveMoodScore sudah ada & jalan).
  - Jangan ubah schema.sql.
  - Jangan hapus DailyCheckInBottomSheet file-nya (cuma simplifikasi isinya).
  - Jangan sentuh SleepTrackingScreen / SleepHubScreen (task 1.2 urus route-nya).

ACCEPTANCE:
  - [ ] Slider tidur & circular dial tidak ada lagi di bottom sheet.
  - [ ] User tap emoji → tap "Simpan Mood" → mood_score masuk mood_logs (verifikasi DB).
  - [ ] TIDAK ADA row baru di sleep_logs dari bottom sheet.
  - [ ] deriveSleepFromDuration & mapEmotionsToMoodScore dihapus dari HomeScreen.kt.
  - [ ] Snackbar feedback tetap jalan.
  - [ ] gradle assembleDebug sukses, tidak ada warning baru.
```

---

### 🔴 TASK 1.2 — Aktifkan Dead Code Screens

```
TASK: Tambah route + tombol navigasi untuk MoodTrackingScreen & SleepTrackingScreen
      (saat ini fully-wired tapi tidak punya route = dead code).

READ FIRST:
  - AUDIT.md section 0.1 (dead code)
  - app/src/main/java/com/example/MainActivity.kt (NavHost, lihat tabRoutes & composable blocks)
  - app/src/main/java/com/example/core/navigation/Screen.kt
  - app/src/main/java/com/example/features/mood/presentation/screen/MoodTrackingScreen.kt
  - app/src/main/java/com/example/features/sleep/presentation/screen/SleepTrackingScreen.kt

SCOPE:
  - core/navigation/Screen.kt
  - MainActivity.kt

DO:
  1. Tambah 2 route di Screen.kt:
       object MoodTracking : Screen("mood_tracking")
       object SleepTracking : Screen("sleep_tracking")
  2. Tambah composable block di NavHost (MainActivity.kt):
       composable(Screen.MoodTracking.route) {
         MoodTrackingScreen(onNavigateBack = { navController.popBackStack() })
       }
       composable(Screen.SleepTracking.route) {
         SleepTrackingScreen(onNavigateBack = { navController.popBackStack() })
       }
     (Tambah param onNavigateBack ke signature screen jika belum ada.)
  3. Tambah tombol navigasi:
     - HomeScreen: tombol "Lihat Mood Detail" → navigate MoodTracking
     - SleepHubScreen: tombol FAB "+ Log Sleep" → navigate SleepTracking
  4. Pastikan TopAppBar kedua screen punya navigationIcon back button.

DON'T:
  - Jangan ubah logic di MoodTrackingScreen/SleepTrackingScreen (sudah wired).
  - Jangan ubah ViewModel/Repository.
  - Jangan sentuh tab routes (Home/Sleep/Relax/Ikigai/Profile tetap).

ACCEPTANCE:
  - [ ] User bisa buka MoodTrackingScreen dari Home → lihat emoji picker + "Riwayat Mood".
  - [ ] User bisa buka SleepTrackingScreen dari SleepHub → isi bed/wake + quality → save.
  - [ ] Back button di kedua screen balik ke screen sebelumnya.
  - [ ] Data sleep yang masuk sekarang AKURAT (bed_time dari user input, bukan tebakan).
  - [ ] gradle assembleDebug sukses.
```

---

### 🟡 TASK 1.3 — Setup Supabase Edge Function Environment

```
TASK: Setup local + remote environment untuk Supabase Edge Functions.
      Deploy function hello-world untuk validasi pipeline.

READ FIRST (Supabase docs — WAJIB pakai Context7):
  - resolve-library-id "Supabase" → query "Edge Functions getting started deploy CLI"

PREREQUISITES (user harus lakukan manual):
  - Install Supabase CLI: https://supabase.com/docs/guides/cli
  - supabase login
  - supabase link --project-ref <PROJECT_REF> (dari Dashboard > Settings > General)
  - Set secret: supabase secrets set GEMINI_API_KEY=<dari Google AI Studio>

SCOPE:
  - Buat folder: supabase/functions/
  - File baru: supabase/functions/hello/index.ts

DO:
  1. Init structure: supabase/functions/_shared/cors.ts (copas dari Supabase docs).
  2. Buat supabase/functions/hello/index.ts:
       import { serve } from "https://deno.land/std/http/server.ts";
       import { corsHeaders } from "../_shared/cors.ts";
       serve(async (req) => {
         return new Response(JSON.stringify({ message: "hello from edge" }), {
           headers: corsHeaders, status: 200
         });
       });
  3. Deploy: supabase functions deploy hello --no-verify-jwt
  4. Test dengan curl:
       curl -i --location --request POST \
         'https://<PROJECT_REF>.functions.supabase.co/hello' \
         --header 'Authorization: Bearer <ANON_KEY>'
  5. Catat PROJECT_REF + function URL di README.md baru: supabase/functions/README.md

DON'T:
  - Jangan taruh GEMINI_API_KEY di file code (harus lewat secrets).
  - Jangan deploy function yang panggil Gemini di task ini (task 1.4 urus itu).

ACCEPTANCE:
  - [ ] supabase functions deploy hello sukses tanpa error.
  - [ ] curl ke endpoint hello return 200 + JSON.
  - [ ] supabase/functions/README.md ada dokumentasi cara deploy & test.
  - [ ] GEMINI_API_KEY ter-set di secrets (cek: supabase secrets list).
```

---

### 🟡 TASK 1.4 — Gemini Hello-World Function

```
TASK: Edge Function yang call Gemini API. Validasi infra AI sebelum bangun pipeline kompleks.

READ FIRST (WAJIB Context7):
  - resolve-library-id "Google Generative AI" → query "Gemini SDK Deno generate content JSON"

SCOPE:
  - File baru: supabase/functions/test-gemini/index.ts

DO:
  1. Pakai SDK @google/genai (Deno-compatible):
       import { GoogleGenerativeAI } from "npm:@google/generative-ai@^0.21";
       const genAI = new GoogleGenerativeAI(Deno.env.get("GEMINI_API_KEY")!);
       const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
  2. Function terima POST body { prompt: string }, call Gemini, return response text.
  3. Handle error: key missing, rate limit, network. Return JSON {ok, data/error}.
  4. Deploy & test dengan curl, prompt "Sebut 3 warna primer dalam 1 kalimat".
  5. Ukur latency (catat di README): cold start vs warm.

DON'T:
  - Jangan hardcode GEMINI_API_KEY (Deno.env.get).
  - Jangan taruh logic prompt kompleks (ini cuma smoke test infra).

ACCEPTANCE:
  - [ ] Function deploy sukses.
  - [ ] curl POST {prompt} return 200 + response dari Gemini.
  - [ ] Kalau GEMINI_API_KEY missing → return error jelas (bukan crash).
  - [ ] Latency tercatat di README (mis. "cold start ~2s, warm ~800ms").
```

---

### 🔴 TASK 2.1 — Schema Migration (Ikigai tables)

```
TASK: Jalankan SQL migration 4.1 + 4.2 (lihat ROADMAP.md section 4).

READ FIRST:
  - ROADMAP.md section 4 (SQL lengkap)
  - supabase/schema.sql (existing, untuk lihat pola RLS existing)

SCOPE:
  - File baru: supabase/migrations/001_extend_profiles.sql
  - File baru: supabase/migrations/002_create_ikigai.sql
  - Update: supabase/schema.sql (append kedua migration supaya single source)

DO:
  1. Buat 2 file migration dengan SQL persis dari section 4 ROADMAP.md.
  2. Test di Supabase SQL Editor (Dashboard) sebelum commit.
  3. Verifikasi: Table Editor muncul tabel ikigai_assessments, ikigai_reports.
  4. Verifikasi RLS: insert sebagai user A → user B tidak bisa SELECT.

DON'T:
  - Jangan drop tabel existing.
  - Jangan ubah policy RLS mood_logs/sleep_logs/journal_entries.

ACCEPTANCE:
  - [ ] profiles punya kolom height_cm, weight_kg, occupation, complaints.
  - [ ] ikigai_assessments & ikigai_reports ada dengan kolom sesuai spec.
  - [ ] RLS test: user A tidak bisa baca data user B.
```

---

### 🔴 TASK 2.3 + 2.4 — Ikigai Assessment UI + Repository

```
TASK: Buat onboarding assessment 6 pertanyaan + repository untuk save ke DB.

READ FIRST:
  - ROADMAP.md section "6 pertanyaan assessment" (Q4 grilling)
  - features/mood/presentation/screen/MoodTrackingScreen.kt (pola form + VM)
  - core/network/dto/SupabaseDtos.kt (pola DTO)

SCOPE:
  - File baru: features/ikigai/data/repository/IkigaiRepository.kt
  - File baru: features/ikigai/data/dto/IkigaiDtos.kt
  - File baru: features/ikigai/presentation/state/IkigaiAssessmentUiState.kt
  - File baru: features/ikigai/presentation/viewmodel/IkigaiAssessmentViewModel.kt
  - File baru: features/ikigai/presentation/screen/IkigaiAssessmentScreen.kt
  - Update: core/navigation/Screen.kt (route IkigaiAssessment)
  - Update: MainActivity.kt (NavHost entry)

DO:
  1. Buat IkigaiAssessmentRow DTO (id, userId, q1-q6, createdAt) dengan @SerialName.
  2. IkigaiRepository: saveAssessment(row): Result<UUID> (return id baru).
  3. ViewModel: 6 field state (q1_text, q2_text, q3_text, q4_text, q5_choice, q6_slider),
     onSaveAssessment() → save ke DB.
  4. Screen: 6 pertanyaan dengan stepper (1/6, 2/6, ...). q5 = pilihan chip,
     q6 = slider 1-10. Tombol "Generate Laporan" di akhir → save → navigate ke loading screen.
  5. Pertanyaan PERSIS dari ROADMAP.md Q4 (6 pertanyaan di tabel).
  6. Tambah route + NavHost entry.

DON'T:
  - Jangan generate report di task ini (task 3.x urus itu).
  - Jangan ubah ikigai_reports (diisi oleh Edge Function, bukan client).
  - Jangan skip validation (q1-q4 wajib diisi).

ACCEPTANCE:
  - [ ] User bisa navigasi ke IkigaiAssessmentScreen.
  - [ ] Stepper menampilkan progress 1/6 sampai 6/6.
  - [ ] Save → row baru di ikigai_assessments (verifikasi DB).
  - [ ] Setelah save, navigasi ke screen "Generating report..." (placeholder).
  - [ ] Validation: q1-q4 kosong → disable tombol next/save.
  - [ ] gradle assembleDebug sukses.
```

---

### 🔴 TASK 3.1 + 3.2 — Edge Function generate-ikigai-report

```
TASK: Edge Function yang fetch assessment, assemble prompt, call Gemini,
      parse JSON terstruktur, save ke ikigai_reports.

READ FIRST (WAJIB Context7):
  - resolve-library-id "Supabase" → query "Edge Function access database service role auth"
  - resolve-library-id "Google Generative AI" → query "Gemini response JSON mode schema"
  - Baca supabase/functions/test-gemini/index.ts (hasil TASK 1.4) — lihat pola error
    classifier yang SUDAH DIOPTIMALKAN (regex word-boundary, JANGAN ulang bug
    substring "rate" match "generateContent")
  - Baca supabase/functions/README.md (hasil TASK 1.4) — tabel latency cold/warm

⚠️ CRITICAL — MODEL GEMINI (temuan TASK 1.4):
  - JANGAN pakai "gemini-1.5-flash" → sudah DEPRECATED, return 404.
  - JANGAN pakai "gemini-2.0-*" atau "gemini-2.5-*" → 429/404 di API key ini.
  - GUNAKAN: model = "gemini-3.5-flash" (BUKAN lite — laporan Ikigai butuh
    kualitas output markdown panjang + JSON terstruktur, lite kurang mumpuni).
  - Expected latency: ~9.6s cold start, ~600ms-1s warm. UI client WAJIB loading
    state karena total round-trip bisa 3-5 detik.

SCOPE:
  - File baru: supabase/functions/generate-ikigai-report/index.ts
  - File baru: supabase/functions/_shared/prompts/ikigai.ts (prompt library)
  - Update: supabase/functions/README.md

DO:
  1. Function terima POST dengan Authorization: Bearer <JWT>.
  2. Verify JWT (pakai supabase-js atau getUser), dapat user_id.
  3. RATE LIMIT: SELECT dari ikigai_reports WHERE user_id AND generated_at > now()-24h.
     Kalau ada → return 429 "Already generated today".
  4. Fetch assessment terbaru user (1 row) + opsional mood/sleep/journal 7 hari.
  5. Assemble prompt dari _shared/prompts/ikigai.ts (prompt library).
  6. Call Gemini dengan response_mime: "application/json" + response_schema:
       {
         report_markdown: string,
         ikigai_circles: { passion: string, skill: string, profession: string, mission: string },
         recommendations: [{ text: string }]  // 3-5 item
       }
  7. INSERT ke ikigai_reports (pakai service role key, bypass RLS).
  8. Return report JSON ke client.

PROMPT LIBRARY (supabase/functions/_shared/prompts/ikigai.ts):
  Export function buildIkigaiPrompt(assessment, passiveData?) yang return string.
  Prompt harus:
  - Pakai bahasa Indonesia (sesuai user)
  - Mention gap insomnia-overthinking (Q5) secara empatik
  - Output 4 lingkaran Ikigai dari Q1-Q4
  - 3-5 rekomendasi actionable, bukan generik
  - Hindari diagnosis medis

DON'T:
  - Jangan hardcode assessment di prompt (parameter dari DB).
  - Jangan skip rate limit (cost leak risk).
  - Jangan expose GEMINI_API_KEY di response/error.

ACCEPTANCE (all ✅ verified 2026-08-10):
  - [x] curl POST dengan JWT valid → 200 + report JSON sesuai contract.
  - [x] ikigai_reports ada row baru dengan report_markdown + ikigai_circles + recommendations.
  - [x] Rate limit: 2x call dalam 24h → 429 `already_generated_today`.
  - [x] JWT invalid → 401 (gateway-level atau handler-level).
  - [x] Error Gemini (max output truncation) → 502 + message jelas, excerpt raw output untuk debug, TANPA leak key.
  - [x] README update dengan cara test (curl command) + tabel acceptance.

Detail di `supabase/functions/README.md` bagian "Acceptance Test Result".
Temuan saat testing: `maxOutputTokens: 2048` tidak cukup untuk `gemini-3.5-flash`
karena thinking model → di-fix ke `8192` (di-comment di index.ts).
```

---

### 🔴 TASK 3.3 — Ikigai Report Display UI

```
TASK: Screen yang menampilkan hasil laporan Ikigai: markdown + 4 lingkaran + rekomendasi.

READ FIRST:
  - ROADMAP.md section "Output Ikigai sweet spot"
  - features/sleep/presentation/screen/SleepHubScreen.kt (lihat SleepStageRadialChart → pattern Canvas 4-bagian)
  - core/designsystem/components/Charts.kt (komponen chart existing)

SCOPE:
  - File baru: features/ikigai/presentation/screen/IkigaiReportScreen.kt
  - File baru: features/ikigai/presentation/state/IkigaiReportUiState.kt
  - File baru: features/ikigai/presentation/viewmodel/IkigaiReportViewModel.kt
  - File baru: features/ikigai/data/repository/IkigaiReportRepository.kt (GET report)
  - Update: core/navigation/Screen.kt + MainActivity.kt (route IkigaiReport)

DO:
  1. ViewModel: loadReport() → fetch ikigai_reports terbaru user (GET only, RLS).
     triggerGenerate() → POST ke Edge Function generate-ikigai-report, poll/wait, reload.
  2. Screen 3 section:
     a. HEADER: tanggal generate + tombol "Refresh" (disabled kalau rate-limited).
     b. LAPORAN: render report_markdown (pakai library markdown atau simple Text).
     c. 4 LINGKARAN: Canvas custom, 4 quadrant dengan label (Cintai/Skill/Profesi/Misi)
        + isi dari ikigai_circles JSON. Reuse pattern SleepStageRadialChart.
     d. REKOMENDASI: LazyColumn 3-5 card, masing-masing dengan checkbox.
        Checkbox state di-save ke ikigai_reports.recommendations[].done (UPDATE lewat repo).
  3. Loading state: tampilkan skeleton saat generate.
  4. Empty state: "Belum ada laporan" + tombol "Mulai Assessment".

DON'T:
  - Jangan generate report di client (harus lewat Edge Function).
  - Jangan render markdown pakai WebView (pakai Compose Text/AnnotatedString).
  - Jangan hardcode data report.

ACCEPTANCE:
  - [ ] User selesai assessment → navigate ke ReportScreen → loading → report muncul.
  - [ ] 4 lingkaran ter-render dengan isi dari AI.
  - [ ] Rekomendasi checkbox bisa di-toggle → state persist (verifikasi DB).
  - [ ] Tombol Refresh → 429 snackbar kalau rate-limited.
  - [ ] Empty state benar untuk user belum punya report.
  - [ ] gradle assembleDebug sukses.
```

---

## 6. Definition of Done — Per Milestone

### Milestone 1 (akhir minggu 1): Damage Control + AI Infra
- ✅ Bottom sheet hanya save mood (DB sleep_logs bersih dari noise).
- ✅ MoodTrackingScreen & SleepTrackingScreen bisa diakses user.
- ✅ Edge Function deploy jalan, Gemini smoke test sukses.
- ✅ Latency Gemini tercatat (decision point: cukup cepat untuk UX?).

### Milestone 2 (akhir minggu 2): Data Foundation
- ✅ 3 tabel baru live (profiles extend, ikigai_assessments, ikigai_reports).
- ✅ User bisa isi profil kesehatan + 6 pertanyaan assessment.
- ✅ Data tersimpan ke DB (verifikasi RLS).

### Milestone 3 (akhir minggu 3): MVP IKIGAI ✨
- ✅ User onboard → assessment → AI generate report → lihat hasil.
- ✅ Ini adalah **DEMO SIAP JUAL**.
- ✅ Rate limit jalan, error handling solid.

### Milestone 4 (akhir minggu 4): Polish + Sleep Preview
- ✅ Notifikasi AI harian jalan (Sleep Therapy preview).
- ✅ SleepHub hardcode dibersihkan.
- ✅ App siap untuk beta test kelompok kecil.

---

## 7. Aturan Eksekusi untuk AI Agent

1. **1 task = 1 sesi agent.** Jangan stack 2 task dalam 1 prompt.
2. **Selalu baca** `ROADMAP.md` + `AUDIT.md` section 0 sebelum mulai.
3. **Context7 WAJIB** untuk Supabase Edge Function & Gemini SDK — jangan andalkan training data.
4. **Test manual** setiap task: verifikasi DB + UI sebelum anggap selesai.
5. **Jangan** edit schema.sql tanpa koordinasi (task 2.1 eksplisit).
6. **GEMINI_API_KEY** tidak pernah di Android client. Hanya di Supabase secret.
7. **Commit per task** dengan pesan: `feat(ikigai): task 1.1 - simplify bottom sheet`.

---

## 8. Risk Register

| Risiko | Mitigasi |
|---|---|
| Gemini SDK Deno break/breaking change | Lock version di import (`@google/generative-ai@^0.21`) |
| Cold start Edge Function >3s | UI loading state baik + warm-up saat app launch |
| Rate limit Gemini harian habis | Rate limit di Edge Function + cache report (regenerate ≤1x/hari) |
| User tidak isi assessment → no data | Onboarding wajib + default placeholder report |
| Prompt output tidak konsisten | JSON mode + response_schema + few-shot examples di prompt |
| Cost Gemini membengkak | Monitor di Google AI Console, set budget alert |
| RLS ikigai_reports bocor | INSERT hanya via Edge Function service role, user SELECT-only |

---

## 9. Quick Reference

- **Audit kondisi codebase:** `AUDIT.md`
- **Task Fase 2 (sudah selesai):** `TASKS_FASE2.md`
- **Setup Supabase:** `supabase/README.md`
- **6 keputusan grilling:** Section 1 dokumen ini
- **Schema migration:** Section 4 dokumen ini
- **Prompt library:** `supabase/functions/_shared/prompts/ikigai.ts` (dibuat di task 3.1)

---

## 10. Update Log

- **v1** _(tanggal)_: Roadmap awal dari hasil grilling. 6 keputusan final, 4 minggu plan, 9 task specs.
