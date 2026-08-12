# FU-02 — HomeScreen: bind sleep widgets to real SleepUiState data

**Category:** data-wiring (BUKAN UI-migration 20-screen) · **Effort:** M ~2-3h · **Status:** ⬜ todo
**Branch:** `fix/home-sleep-data-binding` (1 tiket = 1 branch = 1 PR)

## Masalah
Dua widget sleep di HomeScreen tampil **data palsu / salah sumber**:

1. **Sleep Score Hero Card** — angka score, durasi tidur, dan tren **semua hardcoded**:
   `score = 84`, `sleptText = "7h 23m"`, `trendText = "+12% from last week"`.
   Default yang benar (user baru) = 0 / belum ada data.
2. **Weekly Sleep Chart Card** — menampilkan data **MOOD** (`moodUiState.weeklyMoodScores`)
   padahal judul kartu "WEEKLY SLEEP". Tech-debt yang sudah diketahui (comment di
   kode: "rename + sumber data dirapikan saat TASK 3A").

## Bukti (file:line)
- `app/.../home/presentation/screen/HomeScreen.kt:215-217` (SleepScoreHeroCard):
  ```kotlin
  SleepScoreHeroCard(
      score = 84,                        // ← hardcoded placeholder
      sleptText = "7h 23m",              // ← hardcoded placeholder
      trendText = "+12% from last week", // ← hardcoded placeholder
      ...
  )
  ```
- `app/.../home/presentation/screen/HomeScreen.kt:318-320` (WeeklySleepChartCard):
  ```kotlin
  WeeklySleepChartCard(
      avgText = "Avg 7.4h this week",            // ← hardcoded
      weeklyScores = moodUiState.weeklyMoodScores, // ← SALAH sumber: data MOOD
      onClick = onNavigateToStatistics
  )
  ```

## Sumber data yang benar (sudah ada)
- `SleepViewModel` + `SleepUiState` (`app/.../sleep/presentation/state/SleepUiState.kt`):
  - `weeklySleepScores: List<Int>` (7 elemen 0..100, Mon..Sun) — sudah ada, di-load
    via `onLoadWeeklyScores()` dari `SleepRepository`.
  - Catatan: `HomeScreen` sudah inject `SleepViewModel` tapi belum konsumsi field ini.
- Sleep score harian + durasi terakhir: perlu query dari `recentSleepLogs` atau tambah
  field aggregasi di `SleepUiState` (mis. `lastNightScore`, `lastNightDuration`,
  `weekOverWeekTrend`).
- `avgText` "Avg 7.4h" → hitung rata-rata durasi dari weekly logs (atau placeholder
  "--" saat kosong).

## Scope kerjaan
1. Bind `WeeklySleepChartCard.weeklyScores` → `sleepUiState.weeklySleepScores`
   (BUKAN `moodUiState.weeklyMoodScores`).
2. Bind `SleepScoreHeroCard.score/sleptText/trendText` ke field SleepUiState:
   - Tambah field aggregasi di `SleepUiState` + compute di `SleepViewModel`
     (repo query: last-night sleep log + week-over-week delta).
   - Default 0 / "--" / "no data yet" saat user baru (jangan placeholder palsu).
3. Rename/label "WEEKLY SLEEP" tetap (judul sudah benar, datanya yang salah).
4. `avgText` dihitung dari data riil, bukan hardcoded "7.4h".

## Out of scope
- Generasi insight teks (sudah ada tiket T-005/FR-014 Gemini — `sleepInsightPreview`).
- UI/layout card (sudah di-migrate di tiket 01).

## Acceptance
- [ ] User baru (0 sleep log) → score 0, sleptText "--", trendText "--",
      weekly chart kosong (sudah di-collapse di tiket 01 via `if (any > 0)`).
- [ ] User dgn data → score/durasi/tren real dari DB, chart 7-bar dari
      `weeklySleepScores` (BUKAN mood).
- [ ] Tidak ada lagi hardcoded `84` / `"7h 23m"` / `"+12%..."` / `"Avg 7.4h"`.
- [ ] `compileDebugKotlin` sukses, no new warnings.
- [ ] Reviewer subagent.

## Catatan
Hati-hati: `moodUiState.weeklyMoodScores` dipakai di MANA LAGI? Cek dulu sebelum
remove — kalau hanya untuk kartu sleep ini, aman. `weeklyMoodScores` mood mungkin
dipakai di kartu mood (T-004) atau statistik. Jangan putus data mood yang benar.
