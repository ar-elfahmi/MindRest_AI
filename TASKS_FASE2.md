# Tasks Fase 2 — Wire READ Aggregation ke UI

> **Outcome:** HomeScreen, SleepHubScreen, JournalHistoryScreen pakai data riil dari Supabase (bukan hardcode).
>
> **Strategi eksekusi:**
> - **2A** (Mood aggregation) → jalankan **DULU**.
> - **2B** (Sleep aggregation) dan **2C** (WeeklyMoodTimeline) → **PARALEL** setelah 2A selesai.
>
> Setiap task adalah **1 prompt siap copy-paste** untuk 1 AI agent. Self-contained: agent tidak perlu baca codebase lain.

---

## 📦 Shared Context (untuk semua task)

**Codebase:** Android Kotlin + Jetpack Compose + Supabase + MVVM.
**Konvensi:**
- Repository return `Result<T>` (success/failure).
- ViewModel pakai `MutableStateFlow` + `update { it.copy(...) }`.
- UI pakai `viewModel.uiState.collectAsState()`.
- ID user didapat dari `SupabaseClient.requireClient().auth.currentUserOrNull()?.id` (cek null dulu).

**Reusable component yang sudah ada** (JANGAN duplikasi):
- `core/designsystem/components/Buttons.kt` — tombol
- `core/designsystem/components/Cards.kt` — `CardWithHeader`, dll
- `core/designsystem/components/Indicators.kt` — cek apakah ada `LoadingState`, `EmptyState`
- `core/designsystem/components/Charts.kt` — ada `WellnessChart` (cek signature-nya)

**Cara query Supabase postgrest:**
```kotlin
SupabaseClient.requireClient().postgrest["mood_logs"].select {
    filter { eq("user_id", userId) }
    order("created_at", Order.DESCENDING)
}
.decodeList<MoodLogRow>()
```
Aggregate via `.gte("created_at", sevenDaysAgoIso)` (cek API filter).

---

# 🟢 TASK 2A — Mood Aggregation + Wire ke HomeScreen

## Goal
Hapus hardcode `weeklyScores = listOf(62, 68, 74, 71, 65, 80, 84)` di `HomeScreen.kt`. Ganti dengan data riil dari Supabase: rata-rata mood user 7 hari terakhir, dinormalisasi ke skala 0–100, dikelompokkan per hari (Senin–Minggu).

## Files to Read First (urutan)
1. `app/src/main/java/com/example/features/mood/data/repository/MoodRepository.kt` — lihat pola existing.
2. `app/src/main/java/com/example/features/mood/presentation/viewmodel/MoodViewModel.kt` — lihat `onLoadHistory()` sebagai referensi.
3. `app/src/main/java/com/example/features/mood/presentation/state/MoodUiState.kt` — lihat field.
4. `app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt` — fokus baris 200–220 (pemanggilan `weeklyScores`) dan 545–600 (komponen chart).
5. `app/src/main/java/com/example/core/network/dto/SupabaseDtos.kt` — lihat `MoodLogRow`.

## Scope (boleh diedit)
- `app/src/main/java/com/example/features/mood/data/repository/MoodRepository.kt`
- `app/src/main/java/com/example/features/mood/presentation/viewmodel/MoodViewModel.kt`
- `app/src/main/java/com/example/features/mood/presentation/state/MoodUiState.kt`
- `app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt`

## DON'T Touch
- ❌ `schema.sql`
- ❌ `SleepRepository`, `JournalRepository`
- ❌ `AuthRepository`, `ProfileRepository` (belum ada)
- ❌ `core/network/SupabaseClient.kt`
- ❌ Library baru

## DO — Implementation Steps

### Step 1: Tambah DTO untuk aggregation result

**Edit** `MoodRepository.kt` (top-level, di atas interface atau di bawah DTO existing):

```kotlin
/**
 * Hasil rata-rata mood untuk 1 hari.
 * @param dayOfWeekIndex 0=Monday, 6=Sunday (sesuai urutan chart).
 * @param averageScore nilai 1.0–5.0 (null jika tidak ada data pada hari itu).
 * @param entryCount jumlah log yang masuk ke rata-rata tersebut.
 */
data class DailyMoodAverage(
    val dayOfWeekIndex: Int,
    val averageScore: Double?,
    val entryCount: Int,
)
```

### Step 2: Tambah method di MoodRepository interface

**Edit** `MoodRepository.kt`:

```kotlin
interface MoodRepository {
    suspend fun insertMoodLog(log: MoodLogInsert): Result<Unit>
    suspend fun getMoodLogs(userId: String, limit: Long = 50): Result<List<MoodLogRow>>

    /**
     * Ambil rata-rata mood harian untuk [days] hari terakhir (default 7).
     * Hari tanpa data = null averageScore.
     * Urutan hasil: [Monday, Tuesday, ..., Sunday] (7 elemen).
     */
    suspend fun getDailyMoodAverages(
        userId: String,
        days: Int = 7
    ): Result<List<DailyMoodAverage>>
}
```

### Step 3: Implement di MoodRepositoryImpl

```kotlin
override suspend fun getDailyMoodAverages(
    userId: String,
    days: Int
): Result<List<DailyMoodAverage>> {
    return try {
        // Hitung cutoff timestamp dalam ISO 8601 (UTC).
        val cutoff = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            .minusDays(days.toLong())
            .toString()

        val logs = SupabaseClient.requireClient().postgrest["mood_logs"].select {
            filter {
                eq("user_id", userId)
                gte("created_at", cutoff)
            }
        }.decodeList<MoodLogRow>()

        // Group by day-of-week (Monday=0 .. Sunday=6).
        // Java DayOfWeek: MONDAY=1 .. SUNDAY=7. Konversi ke 0-based Senin.
        val grouped = logs.groupBy { row ->
            val dow = java.time.OffsetDateTime.parse(row.createdAt)
                .toZonedDateTime()
                .dayOfWeek.value  // 1..7
            dow - 1  // 0..6
        }

        val result = (0..6).map { dayIdx ->
            val dayLogs = grouped[dayIdx].orEmpty()
            val avg = if (dayLogs.isEmpty()) null
                else dayLogs.map { it.moodScore }.average()
            DailyMoodAverage(
                dayOfWeekIndex = dayIdx,
                averageScore = avg,
                entryCount = dayLogs.size,
            )
        }
        Result.success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Catatan:** kalau library Supabase Kotlin belum support `gte()`, gunakan raw filter string `filter = "created_at=gte.$cutoff"`. Cek docs jika ragu.

### Step 4: Tambah state di MoodUiState

**Edit** `MoodUiState.kt`:

```kotlin
data class MoodUiState(
    val selectedMood: Int? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val recentMoods: List<MoodLogRow> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
    // BARU — weekly aggregation
    val weeklyMoodScores: List<Int> = List(7) { 0 },  // 0..100, 7 elemen (Mon..Sun)
    val isLoadingWeekly: Boolean = false,
    val weeklyError: String? = null,
)
```

### Step 5: Tambah function di MoodViewModel

**Edit** `MoodViewModel.kt`:

```kotlin
fun onLoadWeeklyScores() {
    viewModelScope.launch {
        val client = SupabaseClient.requireClient()
        if (client == null) {
            _uiState.update { it.copy(weeklyError = "Supabase belum dikonfigurasi.") }
            return@launch
        }
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            _uiState.update { it.copy(weeklyError = "User not logged in") }
            return@launch
        }

        _uiState.update { it.copy(isLoadingWeekly = true, weeklyError = null) }

        repository.getDailyMoodAverages(userId, days = 7)
            .onSuccess { dailyAverages ->
                // Konversi skor 1-5 ke 0-100: round(avg / 5 * 100).
                // Hari tanpa data → isi 0 (atau pertahankan nilai sebelumnya, tapi default 0 lebih jelas untuk "no data").
                val scores = dailyAverages.map { d ->
                    d.averageScore?.let { (it / 5.0 * 100).toInt().coerceIn(0, 100) } ?: 0
                }
                _uiState.update {
                    it.copy(
                        isLoadingWeekly = false,
                        weeklyMoodScores = scores,
                        weeklyError = null,
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoadingWeekly = false,
                        weeklyError = e.message ?: "Failed to load weekly scores.",
                    )
                }
            }
    }
}

fun onWeeklyErrorShown() {
    _uiState.update { it.copy(weeklyError = null) }
}
```

### Step 6: Wire ke HomeScreen

**Edit** `HomeScreen.kt`:

a) Cari dekonstruksi state di signature HomeScreen (sekitar L130-180). Tambahkan ViewModel:
```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.mood.presentation.viewmodel.MoodViewModel

@Composable
fun HomeScreen(
    moodViewModel: MoodViewModel = viewModel(),
    // ... existing params
) {
    val moodUiState by moodViewModel.uiState.collectAsState()
    // ...
```

b) Tambah `LaunchedEffect` untuk auto-load:
```kotlin
LaunchedEffect(Unit) {
    moodViewModel.onLoadWeeklyScores()
}
```

c) Ganti baris **L205**:
```kotlin
// SEBELUM:
weeklyScores = listOf(62, 68, 74, 71, 65, 80, 84),

// SESUDAH:
weeklyScores = moodUiState.weeklyMoodScores,
```

d) Optional: tampilkan loading state — cek apakah `WellnessChart` punya parameter loading. Kalau tidak, tambahkan `if (moodUiState.isLoadingWeekly) CircularProgressIndicator()` di atas chart.

e) Optional: tampilkan error via Snackbar (pakai `moodUiState.weeklyError` + `LaunchedEffect` pattern yang sudah ada di `MoodTrackingScreen`).

## Acceptance Checklist
- [ ] Tidak ada lagi `listOf(62, 68, ...)` atau hardcoded weekly scores di `HomeScreen.kt`.
- [ ] `HomeScreen` recompose ketika `moodUiState.weeklyMoodScores` berubah.
- [ ] User tanpa data: chart menampilkan 7 angka 0 (atau empty state, sesuai keputusan implementasi).
- [ ] User dengan data: chart menampilkan 7 nilai 0–100 yang berubah tiap reload.
- [ ] `gradle assembleDebug` sukses, tidak ada warning baru.
- [ ] Error network: tampilkan snackbar, tidak crash.

## Complexity
**Risiko:** rendah. **Estimasi:** 30–45 menit untuk agent yang sudah kenal Compose/Supabase.

---

# 🟢 TASK 2B — Sleep Aggregation + Wire ke SleepHubScreen

## Goal
Hapus `sampleWeeklyScores = listOf(78, 82, 70, 85, 88, 75, 90)` di `SleepHubScreen.kt`. Ganti dengan skor 0–100 dari rata-rata `sleep_quality` 7 hari terakhir (POOR=25, FAIR=50, GOOD=75, EXCELLENT=100), dikelompokkan per hari (Senin–Minggu).

## Files to Read First
1. `app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt`
2. `app/src/main/java/com/example/features/sleep/presentation/viewmodel/SleepViewModel.kt`
3. `app/src/main/java/com/example/features/sleep/presentation/state/SleepUiState.kt` (ada `SleepQuality` enum: POOR, FAIR, GOOD, EXCELLENT)
4. `app/src/main/java/com/example/features/sleep/presentation/screen/SleepHubScreen.kt` — fokus L65-75 (definisi `sampleWeeklyScores`) dan L190-200 (pemakaian).
5. `app/src/main/java/com/example/core/network/dto/SupabaseDtos.kt` — lihat `SleepLogRow`.

## Scope
- `app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt`
- `app/src/main/java/com/example/features/sleep/presentation/viewmodel/SleepViewModel.kt`
- `app/src/main/java/com/example/features/sleep/presentation/state/SleepUiState.kt`
- `app/src/main/java/com/example/features/sleep/presentation/screen/SleepHubScreen.kt`

## DON'T Touch
- ❌ `schema.sql`
- ❌ `MoodRepository`, `JournalRepository`
- ❌ `AuthRepository`

## DO — Implementation Steps

### Step 1: Tambah DTO aggregation result

**Edit** `SleepRepository.kt`:

```kotlin
data class DailySleepScore(
    val dayOfWeekIndex: Int,  // 0=Mon, 6=Sun
    val averageScore: Double?,  // 0.0..1.0 (null = no data)
    val entryCount: Int,
)
```

### Step 2: Mapping helper (private di file yang sama atau di companion object)

```kotlin
private fun SleepQuality.toScore(): Double = when (this) {
    SleepQuality.POOR -> 0.25
    SleepQuality.FAIR -> 0.50
    SleepQuality.GOOD -> 0.75
    SleepQuality.EXCELLENT -> 1.00
}
```

### Step 3: Tambah method di interface

```kotlin
interface SleepRepository {
    suspend fun insertSleepLog(log: SleepLogInsert): Result<Unit>
    suspend fun getSleepLogs(userId: String, limit: Long = 50): Result<List<SleepLogRow>>

    suspend fun getDailySleepScores(
        userId: String,
        days: Int = 7
    ): Result<List<DailySleepScore>>
}
```

### Step 4: Implement

```kotlin
override suspend fun getDailySleepScores(
    userId: String,
    days: Int
): Result<List<DailySleepScore>> {
    return try {
        val cutoff = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            .minusDays(days.toLong())
            .toString()

        val logs = SupabaseClient.requireClient().postgrest["sleep_logs"].select {
            filter {
                eq("user_id", userId)
                gte("created_at", cutoff)
            }
        }.decodeList<SleepLogRow>()

        val grouped = logs.groupBy { row ->
            java.time.OffsetDateTime.parse(row.createdAt)
                .toZonedDateTime()
                .dayOfWeek.value - 1  // 0..6
        }

        val result = (0..6).map { dayIdx ->
            val dayLogs = grouped[dayIdx].orEmpty()
            val avg = if (dayLogs.isEmpty()) null
                else dayLogs.mapNotNull { row ->
                    runCatching { SleepQuality.valueOf(row.sleepQuality) }.getOrNull()
                }.map { it.toScore() }.takeIf { it.isNotEmpty() }?.average()
            DailySleepScore(
                dayOfWeekIndex = dayIdx,
                averageScore = avg,
                entryCount = dayLogs.size,
            )
        }
        Result.success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Step 5: Tambah state di SleepUiState

```kotlin
data class SleepUiState(
    // ... existing fields ...
    val weeklySleepScores: List<Int> = List(7) { 0 },  // 0..100
    val isLoadingWeekly: Boolean = false,
    val weeklyError: String? = null,
)
```

### Step 6: Tambah function di SleepViewModel

Mirip 2A Step 5, tapi dengan konversi 0.0–1.0 → 0–100:

```kotlin
fun onLoadWeeklyScores() {
    viewModelScope.launch {
        val client = SupabaseClient.requireClient()
        if (client == null) { /* error */ return@launch }
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) { /* error */ return@launch }

        _uiState.update { it.copy(isLoadingWeekly = true, weeklyError = null) }

        repository.getDailySleepScores(userId, days = 7)
            .onSuccess { dailyScores ->
                val scores = dailyScores.map { d ->
                    d.averageScore?.let { (it * 100).toInt().coerceIn(0, 100) } ?: 0
                }
                _uiState.update {
                    it.copy(isLoadingWeekly = false, weeklySleepScores = scores, weeklyError = null)
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(isLoadingWeekly = false, weeklyError = e.message)
                }
            }
    }
}
```

### Step 7: Wire ke SleepHubScreen

a) Inject ViewModel + collect state (pola sama seperti 2A).
b) LaunchedEffect panggil `sleepViewModel.onLoadWeeklyScores()`.
c) Ganti L72 dan L195 dari `sampleWeeklyScores` → `sleepUiState.weeklySleepScores`.

## Acceptance Checklist
- [ ] `sampleWeeklyScores` tidak ada lagi di file (atau rename jadi deprecated `private val` dengan comment "fallback kalau VM null").
- [ ] Chart menampilkan skor 0–100 sesuai `sleep_quality` user.
- [ ] Empty data: 7 angka 0.
- [ ] Build sukses.
- [ ] Error handling jalan.

## Complexity
**Risiko:** rendah. **Estimasi:** 30–45 menit.

---

# 🟡 TASK 2C — Wire WeeklyMoodTimeline di JournalHistoryScreen

## Goal
Hapus `emojis = listOf("😊", "😐", "😔", "😫", "😊", "😊", "")` di `JournalHistoryScreen.kt` (sekitar L219) dan `WeeklyMoodTimeline` hardcoded. Ganti dengan data riil: skor rata-rata mood per hari dalam minggu ini (Senin–Minggu, dari tabel `mood_logs`).

## ⚠️ Dependency
**Jalankan SETELAH 2A selesai** — task ini pakai method `getDailyMoodAverages()` yang dibuat 2A dari `MoodRepository`.

## Files to Read First
1. `app/src/main/java/com/example/features/journal/presentation/screen/JournalHistoryScreen.kt` — fokus L195-235 (`WeeklyMoodTimeline`).
2. `app/src/main/java/com/example/features/mood/presentation/viewmodel/MoodViewModel.kt` — pastikan `onLoadWeeklyScores()` dari 2A sudah ada.
3. `app/src/main/java/com/example/features/mood/data/repository/MoodRepository.kt` — pastikan `getDailyMoodAverages()` ada.
4. `AUDIT.md` baris 19 (entri JournalHistoryScreen 🟡) dan task 2A di `TASKS_FASE2.md`.

## Scope
- `app/src/main/java/com/example/features/journal/presentation/screen/JournalHistoryScreen.kt`

(Tidak edit MoodRepository/MoodViewModel lagi — pakai yang sudah ada.)

## DON'T Touch
- ❌ `MoodRepository.kt` (sudah ada method)
- ❌ `MoodViewModel.kt` (sudah ada function)
- ❌ Schema, sleep/journal repository

## DO — Implementation Steps

### Step 1: Inject MoodViewModel

Tambah di signature `JournalHistoryScreen` (atau parent Composable):

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.mood.presentation.viewmodel.MoodViewModel

@Composable
fun JournalHistoryScreen(
    journalViewModel: JournalViewModel = viewModel(),
    moodViewModel: MoodViewModel = viewModel(),  // BARU
    // ... existing params
)
```

### Step 2: Collect state + auto-load

```kotlin
val moodUiState by moodViewModel.uiState.collectAsState()

LaunchedEffect(Unit) {
    moodViewModel.onLoadWeeklyScores()
    journalViewModel.onLoadHistory()
}
```

### Step 3: Replace hardcoded WeeklyMoodTimeline

Cari blok `WeeklyMoodTimeline(modifier = ...)` (sekitar L195-200) dan modifikasi signature-nya agar terima skor riil:

```kotlin
@Composable
private fun WeeklyMoodTimeline(
    scores: List<Int>,  // 7 elemen (Mon..Sun), 0..100
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Gunakan scores untuk render timeline.
    // Konversi: 0-20 = 😔, 21-40 = 😕, 41-60 = 😐, 61-80 = 🙂, 81-100 = 😊
    val emojis = scores.map { score ->
        when {
            score == 0 -> "·"  // no data
            score <= 20 -> "😔"
            score <= 40 -> "😕"
            score <= 60 -> "😐"
            score <= 80 -> "🙂"
            else -> "😊"
        }
    }
    val days = listOf("S", "S", "R", "K", "J", "S", "M")  // existing — sesuaikan urutan Sen..Min
    // ... render existing UI, tapi pakai `emojis` baru
}
```

Pemanggilan di parent:
```kotlin
WeeklyMoodTimeline(
    scores = moodUiState.weeklyMoodScores,
    isLoading = moodUiState.isLoadingWeekly,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
)
```

### Step 4: Empty / loading state

- Kalau `isLoading`: tampilkan `CircularProgressIndicator` atau shimmer skeleton.
- Kalau `weeklyMoodScores.all { it == 0 }`: tampilkan empty state ("Belum ada data mood minggu ini").

## Acceptance Checklist
- [ ] `val emojis = listOf("😊", ...)` L219 sudah tidak ada (atau cuma `private val` untuk fallback).
- [ ] Timeline berubah sesuai data mood user.
- [ ] Loading & empty state tampil dengan benar.
- [ ] Build sukses.

## Complexity
**Risiko:** rendah–sedang (tergantung kompleksitas `WeeklyMoodTimeline` Composable). **Estimasi:** 20–35 menit.

---

# 📋 Definition of Done (semua 3 task)

1. ✅ Tidak ada hardcode `listOf(...)` untuk skor/emoji di 3 screen target.
2. ✅ User dengan data → chart/timeline menampilkan nilai sesuai DB.
3. ✅ User tanpa data → empty state / nilai 0 (tidak crash).
4. ✅ Error network → snackbar atau fallback, tidak crash.
5. ✅ `gradle assembleDebug` sukses tanpa warning baru.
6. ✅ RLS test: user A tidak bisa lihat data user B (cek dengan login 2 akun).
7. ✅ Auto-load di `LaunchedEffect(Unit)` agar data ter-fetch saat screen dibuka.

---

# 🚀 Cara Eksekusi

**Step 1:** Buka sesi AI agent baru. Tempel prompt ini:

```
Baca file AUDIT.md dan TASKS_FASE2.md di root project.
Eksekusi TASK 2A — Mood Aggregation + Wire ke HomeScreen.
Jangan kerjakan task lain. Laporkan file yang diedit + diff summary saat selesai.
```

**Step 2:** Setelah 2A selesai dan lulus build:

Buka 2 sesi paralel (atau 2 agent di sesi yang sama dengan message berbeda):

```
Sesi 1: "Baca TASKS_FASE2.md. Eksekusi TASK 2B — Sleep Aggregation."
Sesi 2: "Baca TASKS_FASE2.md. Eksekusi TASK 2C — Wire WeeklyMoodTimeline."
```

**Step 3:** Review hasil (cek checklist + build) → commit per task.

---

# 🔄 Update Berkala

Setelah semua 3 task selesai, update `AUDIT.md`:
- Ubah status `HomeScreen`, `SleepHubScreen`, `JournalHistoryScreen` ke `🟡` atau `✅`.
- Tandai task 2A/2B/2C dengan `[x]` di section 6.
- Tambah entry di section 10 (Update Log): `v1.1 — Fase 2 selesai, tanggal X.`

---
