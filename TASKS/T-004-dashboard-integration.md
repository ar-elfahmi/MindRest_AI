# T-004 — Dashboard Integration Final (FR-015)

**Priority**: 🟢 Sedang (setelah T-001..T-003 sukses)
**Estimated effort**: 60–90 menit
**FR terkait**: FR-015 (dashboard ringkasan)
**Dependencies**: T-001 (Ikigai progress widget butuh Ikigai screens aktif), T-002 (sleep weekly scores committed)
**Blocks**: none (UI polish task)

---

## 🎯 Goal

Pastikan `HomeScreen` punya **4 widget data riil** (bukan mock/hardcode):
1. Tren tidur 7 hari (dari T-002)
2. Tren mood 7 hari (dari T-2A — sudah commit di sesi lain)
3. Progress Ikigai (apakah user sudah pernah assessment?)
4. Preview Sleep Insight (link ke T-005 / placeholder)

---

## 📖 Context

Per `AUDIT.md` §0.5 + cek langsung `HomeScreen.kt`:
- weeklyScores sudah pakai data riil (T-2A)
- weeklySleepScores sudah pakai data riil (T-002)
- Ikigai progress widget: perlu query `ikigai_assessments` count by user
- Sleep insight preview: placeholder dulu, real logic di T-005

---

## 📚 Read First (urutan)

1. `CHANGELOG.md` — entry T-001, T-002, T-003 harus sudah ada
2. `app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt` — lihat struktur section
3. `app/src/main/java/com/example/features/ikigai/data/repository/IkigaiRepository.kt` — lihat method existing
4. `app/src/main/java/com/example/features/sleep/presentation/viewmodel/SleepViewModel.kt` — state weekly scores

---

## ✅ Scope (boleh diedit/dibuat)

- `app/src/main/java/com/example/features/home/**`
- `app/src/main/java/com/example/features/ikigai/data/repository/IkigaiRepository.kt` — tambah `getAssessmentCount()` kalau belum ada
- `CHANGELOG.md`

## ❌ DON'T Touch

- File Sleep, Mood (sesuai T-001/T-002)
- File Journal (T-003)
- `core/navigation/Screen.kt`
- File `core/**`

---

## 🛠️ Implementation Steps

### Step 1: Tambah method IkigaiRepository

```kotlin
suspend fun getAssessmentCount(): Result<Int>
```

Query: `SELECT COUNT(*) FROM ikigai_assessments WHERE user_id = current_user`.

### Step 2: HomeViewModel state

Tambah:
```kotlin
data class HomeUiState(
  ...existing,
  val ikigaiAssessmentCount: Int = 0,
  val sleepInsightPreview: String? = null,  // null = belum ada
)
```

### Step 3: Wire di HomeScreen

Cari section "Ikigai progress" / "Sleep insight" — ganti `0` atau string hardcode dengan state dari VM.

Empty state: kalau `ikigaiAssessmentCount == 0`, tampilkan CTA "Mulai Ikigai Assessment" → navigate ke `ikigai_assessment`. Kalau > 0, tampilkan "Lihat Laporan" + badge "X assessment".

### Step 4: Build & commit

```bash
./gradlew assembleDebug
git add app/src/main/java/com/example/features/home/ app/src/main/java/com/example/features/ikigai/data/
git commit -m "feat(home): integrate Ikigai progress + Sleep Insight preview widget (FR-015)

- IkigaiRepository.getAssessmentCount()
- HomeViewModel: expose ikigaiAssessmentCount, sleepInsightPreview
- HomeScreen: replace hardcoded Ikigai section with VM-driven state
- Empty state CTA when user belum pernah assessment"
```

### Step 5: Update CHANGELOG

- FR-015: 🟡 → 🟢

---

## ✔️ Acceptance

- [ ] Build sukses
- [ ] Tidak ada lagi hardcode `0` / `"Mulai Sekarang"` di Ikigai section HomeScreen
- [ ] Empty state CTA navigasi benar
- [ ] Commit + CHANGELOG entry lengkap

---

## 📝 Reporting (sama format T-001)
