# T-002 — Sleep Aggregation 2B: Commit & Verify

**Priority**: 🟠 Tinggi (jalan setelah T-001 sukses)
**Estimated effort**: 15–30 menit
**FR terkait**: FR-006 (sleep history)
**Dependencies**: T-001 (jangan ganggu Ikigai)
**Blocks**: T-004 (dashboard integration perlu 2B committed)

---

## 🎯 Goal

Pastikan Task 2B (Sleep aggregation wire ke SleepHubScreen) yang sudah selesai lokal (`task-2b-output.md` style, tapi tidak ada file output — hanya modified files) ter-commit bersih dan build sukses.

---

## 📖 Context

Dari `git status`:
- `M app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt`
- `M app/src/main/java/com/example/features/sleep/presentation/state/SleepUiState.kt`
- `M app/src/main/java/com/example/features/sleep/presentation/viewmodel/SleepViewModel.kt`

Per `AUDIT.md` §0.5: SleepHubScreen "RIWAYAT TIDUR TERAKHIR" + `weeklySleepScores` sudah di-wire 2B, **tapi section lain masih hardcode**. Itu nanti di T-004.

---

## 📚 Read First (urutan)

1. `CHANGELOG.md` — entry T-001 harus sudah ada
2. `app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt` — lihat method aggregation yang ditambah
3. `app/src/main/java/com/example/features/sleep/presentation/screen/SleepHubScreen.kt` — cari `sampleWeeklyScores = listOf(...)` (harusnya sudah diganti)
4. `task-2a-output.md` — pola output 2A sebagai referensi format (kalau masih ada)

---

## ✅ Scope (boleh diedit)

- 3 file modified di atas (SleepRepository, SleepUiState, SleepViewModel)
- `SleepHubScreen.kt` — **HANYA** kalau masih ada hardcode `sampleWeeklyScores` yang terlewat
- `CHANGELOG.md` — WAJIB update

## ❌ DON'T Touch

- File Ikigai (T-001)
- File Journal (T-003)
- `SleepTrackingScreen.kt` — form input sudah oke
- File `core/`
- `schema.sql`

---

## 🛠️ Implementation Steps

### Step 1: Diff review

```bash
cd /c/laragon/www/MindRest_AI
git diff app/src/main/java/com/example/features/sleep/ | head -200
```

Verifikasi yang berubah masuk akal: tambah method `getWeeklySleepScores()`, state baru, wiring ke SleepHubScreen.

### Step 2: Build check

```bash
./gradlew assembleDebug 2>&1 | tail -30
```

Kalau error, fix minimum (jangan redesign). Kalau masih error setelah 10 menit, tulis entry ❌ dan lapor mediator.

### Step 3: Commit

```bash
cd /c/laragon/www/MindRest_AI
git add app/src/main/java/com/example/features/sleep/
git commit -m "feat(sleep): wire weekly sleep scores aggregation (Task 2B)

- SleepRepository: add getWeeklySleepScores() returning Map<LocalDate, Int>
  from sleep_logs.sleep_quality aggregated by day
- SleepUiState: add weeklyScores field
- SleepViewModel: load weekly scores on init, expose via state
- SleepHubScreen: replace hardcoded sampleWeeklyScores with VM state

FR-006 progress: weekly aggregation live, history list still partial
(see AUDIT.md §0.5 — other sections of SleepHub still hardcoded,
to be addressed in T-004)"
```

### Step 4: Update CHANGELOG

Tambah entry Timeline + update Master Status:
- FR-006: 🟡 → 🟢 (kalau weekly aggregation live & build sukses)

---

## ✔️ Acceptance

- [ ] Build sukses
- [ ] Commit baru ada
- [ ] Tidak ada lagi hardcode `sampleWeeklyScores` di `SleepHubScreen.kt` (verify dengan grep)
- [ ] CHANGELOG entry + status updated

---

## 📝 Reporting (sama format T-001)
