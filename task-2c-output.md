# TASK 2C — Wire WeeklyMoodTimeline di JournalHistoryScreen

**Status:** ✅ Code complete. **BUILD SUCCESSFUL.**
**Project:** MindRest_AI (Android Kotlin + Jetpack Compose + Supabase + MVVM) at `C:/laragon/www/MindRest_AI`
**Source spec:** `TASKS_FASE2.md` § "🟡 TASK 2C" + `AUDIT.md` row #8 (JournalHistoryScreen).
**Dependency satisfied:** TASK 2A sudah selesai — `MoodRepository.getDailyMoodAverages()`, `MoodViewModel.onLoadWeeklyScores()`, dan field `weeklyMoodScores/isLoadingWeekly/weeklyError` di `MoodUiState` sudah ada.

---

## Files Edited (1 — within scope)

| # | File | What changed |
|---|------|--------------|
| 1 | `app/src/main/java/com/example/features/journal/presentation/screen/JournalHistoryScreen.kt` | Inject `MoodViewModel` sebagai parameter `JournalHistoryScreen`; tambah `moodState by moodViewModel.uiState.collectAsState()`; tambah `moodViewModel.onLoadWeeklyScores()` di `LaunchedEffect` (existing); ganti signature `WeeklyMoodTimeline` dari `(modifier)` → `(scores: List<Int>, isLoading: Boolean = false, todayIndex: Int = 5, modifier)`; emoji diturunkan dari skor via `scoreToEmoji()` (≤20😔, ≤40😕, ≤60😐, ≤80🙂, else😊; 0=`·`); tambah loading state (CircularProgressIndicator) dan empty state ("Belum ada data mood minggu ini."); `todayIndex` sekarang dihitung dari `LocalDate.now().dayOfWeek` via helper `todayDayIndex()` (Senin=0..Minggu=6), bukan hardcode 5. |

### Diff Summary

```kotlin
// Signature JournalHistoryScreen: tambah moodViewModel parameter
fun JournalHistoryScreen(
    onNavigateBack: () -> Unit,
    onStartNewSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel(),
    moodViewModel: MoodViewModel = viewModel(),  // BARU
)

// Auto-load + collect mood state
val moodState by moodViewModel.uiState.collectAsState()
LaunchedEffect(Unit) {
    viewModel.onLoadHistory()
    moodViewModel.onLoadWeeklyScores()  // BARU
}

// Call site
WeeklyMoodTimeline(
    scores = moodState.weeklyMoodScores,
    isLoading = moodState.isLoadingWeekly,
    todayIndex = todayDayIndex(),
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
)

// New helpers (private file-level)
private fun scoreToEmoji(score: Int): String = when { ... }
private fun todayDayIndex(): Int { ... LocalDate.now().dayOfWeek ... }

// Inside WeeklyMoodTimeline
val padded = remember(scores) { List(7) { idx -> scores.getOrNull(idx) ?: 0 } }
val emojis = remember(padded) { padded.map { scoreToEmoji(it) } }
val hasAnyData = padded.any { it > 0 }
// Empty-state Text: "Belum ada data mood minggu ini."
// Loading: CircularProgressIndicator(strokeWidth=2.dp, size=24.dp)
// Placeholder (skor 0): "·" (sebelumnya "" → "-")
```

**Diff stats:** 1 file, +132 / −41.

---

## Build Result

**Command:** `./gradlew assembleDebug --no-daemon` (JDK 21.0.10, Android Studio jbr)
**Result:** ✅ **BUILD SUCCESSFUL in 2m 12s**

```
> Task :app:compileDebugKotlin
w: HomeScreen.kt:487:53 'Icons.Filled.TrendingUp' is deprecated. Use AutoMirrored version.
> Task :app:assembleDebug

BUILD SUCCESSFUL in 2m 12s
38 actionable tasks: 7 executed, 31 up-to-date
```

Hanya 1 warning (pre-existing di HomeScreen.kt line 487, unrelated ke TASK 2C). **Tidak ada error atau warning baru di file yang diedit** (`compileDebugKotlin` dengan `clean` + grep filter ke `JournalHistoryScreen` → 0 matches).

---

## Acceptance Checklist

- [x] Hardcode `val emojis = listOf("😊", "😐", ...)` hilang dari file (grep no match, exit 1).
- [x] `WeeklyMoodTimeline` Composable menerima `scores: List<Int>` (plus `isLoading`, `todayIndex`).
- [x] Pemanggilan `WeeklyMoodTimeline` pakai `moodState.weeklyMoodScores`.
- [x] `MoodViewModel` di-inject ke `JournalHistoryScreen` (parameter `moodViewModel: MoodViewModel = viewModel()`).
- [x] `LaunchedEffect(Unit) { moodViewModel.onLoadWeeklyScores() }` ada (di-merge dengan existing `viewModel.onLoadHistory()`).
- [x] `gradlew assembleDebug` exit 0.
- [x] Tidak ada warning baru (1 pre-existing warning di HomeScreen.kt tidak terkait 2C).

---

## Risks / Catatan

1. **`todayIndex` sekarang dinamis** (sebelumnya hardcode `5`/"Sabtu"). Preview Composable akan menampilkan hari ini sebagai highlighted, sesuai ekspektasi real-world. Default parameter `5` dipertahankan untuk backward-compat bila dipanggil tanpa argumen (mis. preview eksternal).
2. **Mood VM instance-level:** `moodViewModel = viewModel()` akan membuat instance baru di `JournalHistoryScreen`. Berbeda dengan `HomeScreen` yang juga punya instance sendiri. Untuk MVP ini OK karena ViewModel hanya baca dari repository; bila nanti ada race condition antar instance, pertimbangkan scoping ke activity/nav-graph.
3. **Empty-state UX:** bila user punya skor 0 di semua 7 hari (mis. user baru), akan tampil pesan "Belum ada data mood minggu ini." di bawah baris. Loading spinner menggantikan seluruh baris emoji saat fetch.
4. **Score-to-emoji mapping:** 0→`·` (placeholder); 1-20→😔; 21-40→😕; 41-60→😐; 61-80→🙂; 81-100→😊. Konsisten dengan spek TASKS_FASE2.md.
5. **Placeholder rendering:** Hari tanpa data (skor 0) tetap menampilkan lingkaran diredupkan + `·` di tengah (sebelumnya pakai `"-"`). Perubahan minor untuk konsistensi visual dengan emoji renderer.
6. **Hari urutan timeline:** Senin=index 0, Minggu=index 6 (sesuai `MoodRepository.getDailyMoodAverages`). `todayDayIndex()` helper memastikan mapping `DayOfWeek` (MONDAY=1..SUNDAY=7) → timeline index (0..6) konsisten.
7. **Tidak ada perubahan pada file di luar scope** (MoodRepository/MoodViewModel/HomeScreen/SleepRepository/schema.sql semua untouched).

---

## Recommended Next Step

1. Smoke test: login, buka `JournalHistoryScreen` → verifikasi timeline menampilkan 7 emoji sesuai data mood 7 hari terakhir.
2. Empty-user test: akun tanpa mood log → tampil pesan empty state.
3. Lanjut TASK 2B (Sleep Aggregation) bila belum selesai — dependency 2A sudah hijau.
4. Setelah 2A/2B/2C semua selesai, update `AUDIT.md` row #8 (JournalHistoryScreen) → `✅` dan centang task 2C di section 6.

---

## Acceptance Report

```acceptance-report
{
  "criteriaSatisfied": [
    {
      "id": "criterion-1",
      "status": "satisfied",
      "evidence": "Concrete findings reported with file paths (JournalHistoryScreen.kt) and severity (None — clean build, no new warnings). Files edited: 1 (within declared scope). Diff: +132/-41 lines. Acceptance checklist all 7 items checked."
    }
  ],
  "changedFiles": [
    "app/src/main/java/com/example/features/journal/presentation/screen/JournalHistoryScreen.kt"
  ],
  "testsAddedOrUpdated": [],
  "commandsRun": [
    {
      "command": "./gradlew assembleDebug --no-daemon",
      "result": "passed",
      "summary": "BUILD SUCCESSFUL in 2m 12s. 1 pre-existing warning di HomeScreen.kt:487 (Icons.Filled.TrendingUp deprecated), unrelated to TASK 2C."
    },
    {
      "command": "./gradlew clean :app:compileDebugKotlin --no-daemon 2>&1 | grep 'JournalHistoryScreen'",
      "result": "passed",
      "summary": "Zero matches — no errors or warnings in the edited file."
    },
    {
      "command": "grep -n 'listOf(\"😊\"' app/.../JournalHistoryScreen.kt",
      "result": "passed",
      "summary": "Exit 1 (no match) — hardcoded emojis listOf(\"😊\",\"😐\",...) L219 successfully removed."
    }
  ],
  "validationOutput": [
    "Edit verified by reading modified file: WeeklyMoodTimeline now takes (scores, isLoading, todayIndex, modifier); emojis derived from scores via scoreToEmoji(); empty-state and loading-state UI implemented; todayIndex computed dynamically from LocalDate.now().dayOfWeek.",
    "MoodViewModel injection in JournalHistoryScreen signature is non-breaking (default value viewModel()); existing JournalViewModel parameter preserved.",
    "No edits to MoodRepository.kt, MoodViewModel.kt, MoodUiState.kt, HomeScreen.kt, SleepRepository.kt, JournalRepository.kt, schema.sql, or any other file outside scope (verified via git diff --stat)."
  ],
  "residualRisks": [
    "Per-instance MoodViewModel: each screen that injects it gets its own VM. Acceptable for MVP (no shared mutable state across screens besides repo).",
    "Empty-data state UX: shows 'Belum ada data mood minggu ini.' subtitle below the row — may need visual review for spacing in real device.",
    "No unit tests added; existing repo has no test infrastructure (spec does not require)."
  ],
  "noStagedFiles": true,
  "diffSummary": "Inject MoodViewModel into JournalHistoryScreen; collect moodUiState; auto-load weekly scores in LaunchedEffect; rewrite WeeklyMoodTimeline signature to accept scores: List<Int>, isLoading: Boolean, todayIndex: Int; derive emojis from scores via scoreToEmoji() (0='·', 1-20=😔, 21-40=😕, 41-60=😐, 61-80=🙂, 81-100=😊); add loading spinner and empty-state Text; compute todayIndex dynamically from LocalDate.now().dayOfWeek (Senin=0..Minggu=6) via new todayDayIndex() helper. Diff: +132/-41 in 1 file.",
  "reviewFindings": [
    "no blockers",
    "minor: todayIndex default = 5 (Sabtu) dipertahankan untuk backward-compat preview"
  ],
  "manualNotes": "Build verified with Android Studio jbr (JDK 21.0.10). Compilation clean — only 1 pre-existing Icons.Filled.TrendingUp deprecation warning in HomeScreen.kt:487, unrelated to TASK 2C."
}
```