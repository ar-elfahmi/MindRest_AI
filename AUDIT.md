# MindRest_AI — UI/Feature Audit

> **Tujuan:** Dokumen ini memetakan kondisi nyata setiap Screen vs backend.
> Output dipakai sebagai **input untuk semua task AI agent berikutnya**.
>
> **Cara baca:**
> - ✅ = sudah wired ke ViewModel/Repository
> - 🟡 = sebagian wired, sebagian masih hardcode
> - ❌ = sepenuhnya mock / hardcode / belum pakai backend
> - ⚠️ = ada catatan penting

**Tanggal audit:** _(isi tanggal saat ini)_
**Audit dilakukan oleh:** agent sesi konsultasi (Claude/MiniMax-M3)

---

## 1. Inventaris Screen & Status

| # | Screen | Path | Status | Mock data utama |
|---|---|---|---|---|
| 1 | **Login / Register** | `features/authentication/presentation/screen/AuthScreens.kt` | ✅ | (gradients saja, bukan data) |
| 2 | **HomeScreen** | `features/home/presentation/screen/HomeScreen.kt` | ❌ | `weeklyScores = listOf(62, 68, 74, 71, 65, 80, 84)` L205 |
| 3 | **MoodTrackingScreen** | `features/mood/presentation/screen/MoodTrackingScreen.kt` | ✅ | — (emoji + label = konstanta UI, bukan mock data) |
| 4 | **SleepTrackingScreen** | `features/sleep/presentation/screen/SleepTrackingScreen.kt` | ✅ | — |
| 5 | **SleepHubScreen** | `features/sleep/presentation/screen/SleepHubScreen.kt` | ❌ | `recommendations = listOf(...)` L53, `sampleWeeklyScores` L72 |
| 6 | **JournalScreen** | `features/journal/presentation/screen/JournalScreen.kt` | 🟡 | form input OK; cek apakah save panggil repo |
| 7 | **AiJournalScreen** | `features/journal/presentation/screen/AiJournalScreen.kt` | ❌ | chat hardcoded `messages by remember { mutableStateOf(listOf(ChatMessage(...))) }` L65; **belum panggil Gemini** |
| 8 | **JournalHistoryScreen** | `features/journal/presentation/screen/JournalHistoryScreen.kt` | 🟡 | `recentEntries` wired ✅; `WeeklyMoodTimeline` masih `emojis = listOf(...)` L219 |
| 9 | **IkigaiDashboardScreen** | `features/ikigai/presentation/screen/IkigaiDashboardScreen.kt` | ❌ | cek isi: kemungkinan besar hardcode chart data |
| 10 | **LifestyleScreen** | `features/lifestyle/presentation/screen/LifestyleScreen.kt` | ❌ | `initialLifestyleGoals = listOf(...)` L78 + `weekDays` L506 |
| 11 | **ReminderScreen** | `features/reminder/presentation/screen/ReminderScreen.kt` | ❌ | tidak baca dari DB |
| 12 | **SettingsScreen** | `features/settings/presentation/screen/SettingsScreen.kt` | 🟡 | cek: logout/profile mungkin OK, edit profile belum |
| 13 | **AchievementsScreen** | `features/achievements/presentation/screen/AchievementsScreen.kt` | ❌ | `sampleAchievements = listOf(...)` L67 + banyak `steps = listOf(...)` |
| 14 | **StatisticsScreen** | `features/statistics/presentation/screen/StatisticsScreen.kt` | ❌ | `weeklyTrendData`, `monthlyTrendData`, `yearlyTrendData` hardcode L52-80 |
| 15 | **NotificationScreen** | `features/notification/presentation/screen/NotificationScreen.kt` | ❌ | `val notifications = listOf(...)` L37 |
| 16 | **AdvancedRelaxationScreen** | `features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt` | ❌ | `val modes = listOf("Gerak", "Napas", "Suara")` L96 + video placeholder L160 |
| 17 | **DailyCheckInBottomSheet** | `features/home/presentation/screen/DailyCheckInBottomSheet.kt` | 🟡 | `availableEmotions = listOf(...)` L65 — cek apakah emosion list dipakai di picker atau dari DB |

**Total screen:** 17 | **Wired:** 3 (17%) | **Sebagian:** 4 (24%) | **Mock:** 10 (59%)

---

## 0. 🚨 TEMUAN KRITIS — Audit Ulang Pasca Fase 2 (WAJIB BACA)

> Temuan ini menemukan **kesenjangan arsitektur** yang lebih dalam dari sekadar mock data.
> Baca dulu sebelum mengerjakan task apapun. Banyak asumsi di section 6 (backlog) berubah.

### 0.1 Ada 2 Screen yang Sudah Fully Wired tapi = DEAD CODE (tidak punya route)

| Screen | Status kode | Ada route di NavHost? | User pernah lihat? |
|---|---|---|---|
| `MoodTrackingScreen.kt` | ✅ Emoji picker 1-5 + history "Riwayat Mood Terakhir" ter-wiring penuh | ❌ **TIDAK** (tidak ada `Screen.MoodTracking`) | ❌ |
| `SleepTrackingScreen.kt` | ✅ TextField bed/wake + auto-calc duration + quality chips + history | ❌ **TIDAK** | ❌ |

**Implikasi:** Task 2A/2B berhasil di **layer data + ViewModel**, tapi **UI yang user lihat bukan UI yang sudah wired**. Kode berkualitas tidak pernah dieksekusi.

### 0.2 UI yang User LIHAT & PAKAI sebenarnya = `DailyCheckInBottomSheet`

Yang muncul dari Home ("Check-in Hari Ini") BUKAN emoji picker, melainkan:
- **Chip emosi** (Lelah/Tenang/Cemas/Bersyukur/Overthinking/Damai/Fokus/Bersemangat) — bukan emoji 1-5
- **Slider 0-12 jam** untuk durasi tidur — bukan input bed/wake time

### 0.3 ⚠️ DATA TIDUR DI DB = PALSU (ditebak dari slider)

`HomeScreen.kt` L257-276 — handler `onSave` bottom sheet:
```kotlin
val (bedTime, wakeTime, quality) = deriveSleepFromDuration(sleepHours)
sleepViewModel.saveSleepLog(bedTime, wakeTime, quality)
```

`deriveSleepFromDuration()` (L876) **menebak**:
- `wakeTime` = waktu sekarang (user "baru bangun")
- `bedTime` = sekarang − durasi slider
- `quality` = mapping dari jam (<5h→POOR, 5-7h→FAIR, 7-9h→GOOD, >9h→EXCELLENT)

**Itulah kenapa di Test 3 "data tidak sesuai"** — kolom `bed_time`/`wake_up_time` di `sleep_logs` itu hasil tebakan, bukan input user. **Semua data tidur yang sudah masuk DB bersifat noisy/tidak akurat.**

### 0.4 Data Mood juga Noisy (tapi lebih dapat diterima)

`mapEmotionsToMoodScore()` (L858) konversi emosi→skor 1-5 berdasar net positif-negatif. Ini heuristic, bukan input langsung. Bisa diterima untuk MVP tapi tidak ideal.

### 0.5 `SleepHubScreen` masih banyak hardcode walau task 2B sukses

Task 2B berhasil wire `weeklySleepScores` + "RIWAYAT TIDUR TERAKHIR". TAPI section lain masih full mock:
- ❌ `SleepScoreCard(score=88, hours=7, minutes=42)` — "Last Night's Sleep Quality" hardcode
- ❌ `MetricTile("Bedtime"/"11:15 PM")`, `("Wake Time"/"06:57 AM")`, `("Efficiency"/"94%")` — hardcode
- ❌ `SleepStageRadialChart(light=0.48, deep=0.24, rem=0.28)` — hardcode (tidak ada data ini di DB!)
- ❌ `recommendations = listOf(...)` — AI tips hardcode

**Note:** tabel `sleep_logs` **tidak menyimpan** sleep stage (light/deep/rem) atau efficiency. Section "SLEEP STAGE DISTRIBUTION" tidak bisa di-wire tanpa skema baru atau hapus dari UI.

### 0.6 Flow Journal→Chatbot BUKAN bug (intended design)

Test 4 yang menemukan "Mulai Refleksi → redirect chatbot" = **memang begitu desainnya**. Flow:
```
JournalHistoryScreen → "Mulai Refleksi" → AiJournalScreen (chat)
                                              → (Fase 6) Gemini summary
                                              → personalisasi Ikigai guidance
```
Belum jalan karena Gemini belum diintegrasikan. **Bukan prioritas fix sekarang.**

### 0.7 Status revisi tabel section 1

Baris yang perlu update status:
- `MoodTrackingScreen`: ✅ (kode) → ⚠️ DEAD CODE
- `SleepTrackingScreen`: ✅ (kode) → ⚠️ DEAD CODE
- `DailyCheckInBottomSheet`: 🟡 → ⚠️ PARTIAL + data tidur palsu
- `SleepHubScreen`: ❌ → 🟡 (2B sukses, section lain masih mock)
- `HomeScreen`: ❌ → 🟡 (2A sukses untuk weekly chart, bottom sheet masih bermasalah)

### 0.8 Keputusan Design — FINAL (resolved via grilling session)

Hasil grilling dengan user → **6 keputusan final** (lihat `ROADMAP.md` untuk detail eksekusi):

1. **Tujuan produk:** Ikigai discovery sebagai output; mood/sleep/journal = feeder.
2. **MVP pertama:** AI foundation dulu → Ikigai report (bukan Sleep Therapy duluan).
3. **Output Ikigai:** Laporan markdown + 4 Lingkaran visual + 3-5 Rekomendasi.
4. **Data input Ikigai:** Hybrid — 6 pertanyaan assessment + passive data opsional.
5. **Arsitektur AI:** Supabase Edge Function (Deno TS), Free tier, `GEMINI_API_KEY` di secret server-side.
6. **`DailyCheckInBottomSheet`:** Simplifikasi jadi **quick-mood only** (ganti chip→emoji, **HAPUS slider tidur**). Sleep input via `SleepTrackingScreen` yang route-nya diaktifkan.

⚠️ **Task 1.1 (URGENT):** Hapus `deriveSleepFromDuration` + slider → stop data noise di `sleep_logs`. Kerjakan pertama.

---

## 2. Inventaris Repository & Status

| # | Repository | File | Methods | Status |
|---|---|---|---|---|
| 1 | `MoodRepository` | `features/mood/data/repository/MoodRepository.kt` | `insertMoodLog`, `getMoodLogs` | ✅ read+write |
| 2 | `SleepRepository` | `features/sleep/data/repository/SleepRepository.kt` | `insertSleepLog`, `getSleepLogs` | ✅ read+write |
| 3 | `JournalRepository` | `features/journal/data/repository/JournalRepository.kt` | `insertJournalEntry`, `getJournalEntries` | ✅ read+write |
| 4 | `AuthRepository` | `features/authentication/data/repository/AuthRepository.kt` | (cek) | ✅ (login/register/Google) |
| 5 | `GoogleAuthHelper` | `features/authentication/data/repository/GoogleAuthHelper.kt` | — | ✅ |
| 6 | `ProfileRepository` | — | — | ⚠️ **TIDAK ADA** (tabel `profiles` ada di DB, tapi tidak ada repo) |
| 7 | `LifestyleRepository` | — | — | ❌ (tabel juga belum ada di DB) |
| 8 | `AchievementsRepository` | — | — | ❌ (tabel juga belum ada) |
| 9 | `ReminderRepository` | — | — | ❌ (cuma `BedtimeNotificationReceiver.kt`) |
| 10 | `IkigaiRepository` | — | — | ❌ |

**Gap terbesar:**
- **Tidak ada `update`/`delete`** di semua repository → user tidak bisa edit/hapus mood log, sleep log, journal entry
- **Tidak ada stats query** (rata-rata, tren, distribusi) → StatisticsScreen hardcode

---

## 3. Inventaris Database Schema

| Tabel | File | Status | Rows |
|---|---|---|---|
| `profiles` | `supabase/schema.sql` | ✅ | user_id, display_name, avatar_url, updated_at (trigger auto-create) |
| `mood_logs` | `supabase/schema.sql` | ✅ | id, user_id, mood_score, created_at |
| `sleep_logs` | `supabase/schema.sql` | ✅ | id, user_id, bed_time, wake_up_time, sleep_quality, created_at |
| `journal_entries` | `supabase/schema.sql` | ✅ | id, user_id, content, created_at |
| `lifestyle_logs` | — | ❌ | **belum ada tabel** |
| `achievements` | — | ❌ | **belum ada tabel** |
| `reminders` | — | ❌ | **belum ada tabel** |
| `ikigai_*` | — | ❌ | **belum ada tabel** |

**RLS status:** ✅ enabled untuk semua tabel existing (cek schema.sql untuk policy detail).

---

## 4. Inventaris Reusable Components (sudah ada)

> ✅ = sudah ada di `core/designsystem/components/`

- `Audio.kt`, `Brand.kt`, `Buttons.kt`, `Cards.kt`, `Charts.kt`, `Chat.kt`, `IkigaiTimeline.kt`, `Indicators.kt`, `Navigation.kt`, `Profile.kt`, `Reminder.kt`, `Settings.kt`, `TextFields.kt`

**Reusable umum yang BELUM ada (atau belum standar):**
- ⚠️ `EmptyState` — setiap screen pakai copy berbeda. **Bikin 1 shared component di Fase 1.**
- ⚠️ `LoadingState` (skeleton/spinner reusable) — cek apakah sudah ada di `Indicators.kt`.
- ⚠️ `ErrorState` + retry button — cek apakah sudah ada di `Indicators.kt` atau `Cards.kt`.

---

## 5. Dependency & Tech Stack

| Layer | Tech |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose (Material3) |
| Architecture | MVVM + Repository |
| Async | Kotlin Coroutines + Flow |
| Backend | Supabase (PostgreSQL + Auth + RLS) |
| DB client | `io.github.jan.supabase` (postgrest, auth) |
| Auth | Email/Password + Google via Credential Manager |
| AI | Gemini (dependency di `.env`, `GEMINI_API_KEY`) — **belum dipakai** |
| Notification | `BedtimeNotificationReceiver.kt` ada; scheduler belum jelas |
| Build | Gradle 8.11.1, Kotlin DSL |

---

## 6. Task Backlog (urutan eksekusi)

### Fase 1 — Fondasi bersama (sequential)
> **Outcome:** semua fitur di bawah bisa jalan paralel karena fondasi ini.

- [ ] **1A.** Tambah `updateMoodLog(id, ...)` + `deleteMoodLog(id)` di MoodRepository. **Scope:** `MoodRepository.kt`, `MoodViewModel.kt`. **Risiko:** rendah.
- [ ] **1B.** Sama untuk SleepRepository (update/delete). **Scope:** `SleepRepository.kt`, `SleepViewModel.kt`. **Risiko:** rendah.
- [ ] **1C.** Sama untuk JournalRepository. **Scope:** `JournalRepository.kt`, `JournalViewModel.kt`. **Risiko:** rendah.
- [ ] **1D.** Bikin `core/ui/components/EmptyState.kt`, `LoadingState.kt`, `ErrorState.kt` (cek dulu apakah sudah ada — kalau sudah, standardize). **Scope:** `core/designsystem/components/` atau `core/ui/`. **Risiko:** rendah.
- [ ] **1E.** Bikin `ProfileRepository` (read `profiles`, update display_name/avatar_url). **Scope:** file baru + `SettingsViewModel` kalau ada. **Risiko:** sedang.
- [ ] **1F.** Tambah schema migration untuk tabel `lifestyle_logs`, `achievements`, `reminders` (lihat Fase 4). **Scope:** `supabase/schema.sql`. **Risiko:** sedang (RLS + trigger).

### Fase 2 — Wire READ yang sudah ada ke UI (paralel: 3 worker)

- [ ] **2A — MoodViewModel aggregation.** Tambah `getMoodStats(7days)` di MoodRepository (count per hari, avg). Wire ke HomeScreen `weeklyScores`. **Scope:** `MoodRepository.kt`, `MoodViewModel.kt`, `HomeScreen.kt`. **Risiko:** rendah.
- [ ] **2B — SleepViewModel aggregation.** Sama untuk sleep: `getSleepStats(7days)`. Wire ke SleepHubScreen `sampleWeeklyScores`. **Scope:** `SleepRepository.kt`, `SleepViewModel.kt`, `SleepHubScreen.kt`. **Risiko:** rendah.
- [ ] **2C — JournalViewModel stats.** Wire `JournalHistoryScreen` WeeklyMoodTimeline ke mood_logs aggregasi. **Scope:** `JournalHistoryScreen.kt`, kemungkinan butuh helper dari 2A. **Risiko:** rendah.

### Fase 3 — Statistik & chart (paralel: 3 worker)

- [ ] **3A — StatisticsScreen Mood section.** Ganti `weeklyTrendData`/`monthlyTrendData`/`yearlyTrendData` (L52-80) dengan query mood_logs. **Scope:** `StatisticsScreen.kt`, `MoodRepository.kt`, `MoodViewModel.kt`. **Risiko:** sedang.
- [ ] **3B — StatisticsScreen Sleep section.** Sama tapi untuk sleep_logs. **Scope:** `StatisticsScreen.kt`, `SleepRepository.kt`, `SleepViewModel.kt`. **Risiko:** sedang.
- [ ] **3C — NotificationScreen.** Ganti `val notifications = listOf(...)` (L37). **Keputusan dulu:** apakah pakai tabel `reminders` (Fase 4) atau read dari `bedtime` field di settings? **Risiko:** sedang.

### Fase 4 — Edit/Delete & fitur baru (paralel: 4-6 worker)

- [ ] **4A — Edit Mood log.** UI: long-press row → bottom-sheet edit. Backend: `updateMoodLog`. **Scope:** `MoodTrackingScreen.kt`, `MoodViewModel.kt`, `MoodRepository.kt`. **Risiko:** rendah.
- [ ] **4B — Edit Sleep log.** Sama. **Scope:** Sleep*. **Risiko:** rendah.
- [ ] **4C — Edit Journal entry.** Sama + navigasi ke detail screen. **Scope:** Journal* + `JournalDetailScreen.kt` baru. **Risiko:** sedang.
- [ ] **4D — LifestyleScreen end-to-end.** Tambah tabel `lifestyle_logs` (id, user_id, goal_type, target, done, date), repository, wire screen. **Scope:** schema.sql + Lifestyle* + new files. **Risiko:** tinggi (tabel baru + RLS).
- [ ] **4E — AchievementsScreen end-to-end.** Tambah tabel `achievements` (id, user_id, type, unlocked_at, steps), repository, wire screen. **Scope:** schema.sql + Achievements*. **Risiko:** tinggi.
- [ ] **4F — ReminderScreen + scheduler.** Tambah tabel `reminders` + WorkManager scheduling + wire screen. **Scope:** schema.sql + Reminder* + new scheduler. **Risiko:** tinggi.

### Fase 5 — Polish & advanced

- [ ] **5A — Profile editing di Settings.** Pakai `ProfileRepository` dari 1E. **Risiko:** rendah.
- [ ] **5B — Ikigai dashboard.** Keputusan: tabel atau hanya UI statis? Default: statis + 5 pertanyaan onboarding. **Risiko:** sedang.
- [ ] **5C — AdvancedRelaxationScreen.** Ganti `modes` list dengan resource/audio player. **Risiko:** tinggi (perlu audio assets).
- [ ] **5D — Logout flow.** Cek apakah sudah jalan. **Risiko:** rendah.
- [ ] **5E — Empty state global.** Semua screen pakai `EmptyState` dari 1D. **Risiko:** rendah.

### Fase 6 — AI & Integrasi (paling akhir, review manual)

- [ ] **6A — Gemini integration di AiJournalScreen.** Ganti `messages by remember {...}` dengan API call ke Gemini. **Scope:** `AiJournalScreen.kt`, `AiJournalViewModel.kt` baru, `GeminiService.kt` baru. **Risiko:** **TINGGI** (API key, prompt design, cost, error handling).
- [ ] **6B — Journal AI summary.** Tiap journal entry di-summary Gemini. **Risiko:** tinggi.
- [ ] **6C — Onboarding flow.** First-time user → 3-5 pertanyaan. **Risiko:** sedang.
- [ ] **6D — Crash reporting + analytics.** **Risiko:** rendah.

---

## 7. Aturan Main untuk AI Agent (tempel di setiap task)

```
TASK: <goal 1 kalimat>
SCOPE:
  - <file path yang boleh diedit>
  - <file path lain>
DO:
  - <step konkret>
  - <step konkret>
DON'T:
  - <larangan>
ACCEPTANCE:
  - [ ] <checklist>
  - [ ] <checklist>
REFERENCE:
  - AUDIT.md baris/task <id>
  - <file path referensi>
```

**Aturan tambahan:**
1. ❌ **Jangan** edit `schema.sql` kecuali task eksplisit menyebutnya (1F, 4D-F).
2. ❌ **Jangan** ganti nama fungsi di Repository (backward-compat untuk ViewModel).
3. ✅ Selalu pakai `core/designsystem/components/` yang sudah ada, jangan duplikasi.
4. ✅ Tulis hasil kosong (empty state), jangan crash.
5. ✅ Test dengan 2 user_id berbeda untuk pastikan RLS tidak bocor.
6. ✅ Build sukses (`gradle assembleDebug`) sebelum anggap selesai.

---

## 8. Risiko & Catatan Penting

### RLS / Keamanan
- Semua query ke Supabase **wajib** filter by `user_id = currentSessionOrNull()?.user?.id`. Cek setiap Repository baru.
- Jangan pernah pakai `service_role` key di client Android.

### Performance
- `getMoodLogs(limit: 50)` di-load di setiap screen → perlu SharedFlow atau caching supaya tidak re-fetch tiap buka screen.
- Stats aggregation di client OK untuk MVP, tapi kalau data >1000 row, pindah ke Postgres view.

### State Management
- ViewModel pakai `MutableStateFlow` + `update { }`. Jangan pakai `LiveData` baru.
- `Result<T>` (dari `core/common/Result.kt`) atau `Resource<T>` (dari `core/common/Resource.kt`) — pilih salah satu dan konsisten.

### Gemini API
- `GEMINI_API_KEY` di `.env` → pakai `BuildConfig` atau `local.properties` (BUKAN hardcode).
- Rate limiting: tambah delay 1-2 detik antar request kalau user spam tombol.
- Prompt harus **deterministic** (temperature rendah) untuk konsistensi.

---

## 9. Referensi Cepat

- **Schema SQL:** `supabase/schema.sql`
- **Env template:** `.env.example`
- **DTO:** `core/network/dto/SupabaseDtos.kt`
- **Base VM/State/Event:** `core/base/`
- **Common (Result, Resource):** `core/common/`
- **Reusable components:** `core/designsystem/components/`
- **Setup Supabase:** `supabase/README.md`

---

## 10. Update Log

_(isi每次 setelah update)_

- **v1** _(tanggal)_: Audit awal oleh Claude konsultasi.
- **v1.1** _(pasca task 2A/2B/2C)_: Task 2A/2B sukses di layer data+VM (weekly aggregation jalan, `MoodRepository.getDailyMoodAverages` + `SleepRepository.getDailySleepScores` ada). TAPI temuan kritis section 0: `MoodTrackingScreen` & `SleepTrackingScreen` adalah DEAD CODE (tidak ada route), user sebenarnya pakai `DailyCheckInBottomSheet` yang data tidurnya PALSU (ditebak dari slider via `deriveSleepFromDuration`). Flow Journal→Chatbot dikonfirmasi intended (bukan bug). **Blocker keputusan UX**: peran bottom sheet belum ditetapkan.
