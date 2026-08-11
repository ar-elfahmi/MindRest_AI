# T-001 — Stabilize & Commit Ikigai M2/M3 Work

**Priority**: 🔴 URGENT — jalan pertama, sebelum task lain
**Estimated effort**: 30–60 menit
**FR terkait**: FR-012, FR-013
**Dependencies**: none
**Blocks**: T-002, T-003, T-004, T-005

---

## 🎯 Goal

Pastikan semua kerja lokal terkait Ikigai M2 (Assessment) dan M3 (Report) yang belum di-commit **bisa di-build sukses**, lalu **commit** agar tidak hilang. Lalu isi entry CHANGELOG lengkap.

---

## 📖 Context (kenapa task ini ada)

Repo punya banyak perubahan **untracked/modified** dari kerja sesi sebelumnya yang berisiko hilang:

- **Untracked** (belum pernah di-commit):
  - `app/src/main/java/com/example/features/ikigai/data/` (DTOs + Repository)
  - `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiAssessmentScreen.kt`
  - `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiReportLoadingScreen.kt`
  - `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiReportScreen.kt`
  - `app/src/main/java/com/example/features/ikigai/presentation/state/`
  - `app/src/main/java/com/example/features/ikigai/presentation/viewmodel/`
  - `supabase/functions/generate-ikigai-report/`
  - `supabase/migrations/002_create_ikigai.sql`
  - `supabase/migrations/003_ikigai_reports_update_policy.sql`
  - `supabase/backfill_profiles.sql`
- **Modified**:
  - `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiDashboardScreen.kt`
  - `app/src/main/java/com/example/core/navigation/Screen.kt`
  - `app/src/main/java/com/example/core/network/SupabaseClient.kt`

Sesuai `AUDIT.md` §0.1 (sebelum commit `a7b5fbb`) screen-screen Ikigai ini kemungkinan dulu dead code; di-commit `a7b5fbb` screen `MoodTracking` & `SleepTracking` diaktifkan. **T-001 ini memastikan screen Ikigai yang baru dibuat juga aktif di NavHost.**

---

## 📚 Read First (urutan)

1. `CHANGELOG.md` — cek entry terakhir biar paham state
2. `ORCHESTRATION.md` — alur kerja
3. `ROADMAP.md` §5 — Task 3.1 + 3.2 + 3.3 (spec awal Ikigai Report)
4. `app/src/main/java/com/example/core/navigation/Screen.kt` — lihat route definition
5. `app/src/main/java/com/example/MainActivity.kt` — lihat NavHost wiring
6. `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiDashboardScreen.kt` — entry point Ikigai
7. `supabase/migrations/002_create_ikigai.sql` — schema tabel

---

## ✅ Scope (boleh diedit/dibuat)

- `app/src/main/java/com/example/core/navigation/Screen.kt` (tambah route Ikigai baru)
- `app/src/main/java/com/example/MainActivity.kt` (tambah composable block untuk Ikigai screens)
- File untracked Ikigai — boleh improve (typo, bug build) tapi **JANGAN ubah design**
- `supabase/functions/generate-ikigai-report/index.ts` — boleh fix error runtime
- `CHANGELOG.md` — WAJIB update Timeline + (opsional) Master Status tabel

## ❌ DON'T Touch

- File di luar daftar Scope
- `ROADMAP.md`, `ORCHESTRATION.md`, `TASKS/*.md`
- `AUDIT.md`, `TASKS_FASE2.md`, dokumen `Dokumen Teknis*.md`
- `app/src/main/java/com/example/features/sleep/**` (T-002)
- `app/src/main/java/com/example/features/journal/**` (T-003)
- Schema SQL di `supabase/schema.sql` (master schema) — pakai file di `supabase/migrations/` saja

---

## 🛠️ Implementation Steps

### Step 1: Verifikasi kondisi lokal

```bash
cd /c/laragon/www/MindRest_AI
git status --short
git diff --stat
```

Pastikan daftar untracked/modified di atas sesuai.

### Step 2: Build attempt (baseline)

```bash
cd /c/laragon/www/MindRest_AI
./gradlew assembleDebug --warning-mode summary 2>&1 | tail -50
```

**Expected**: bisa jadi error karena screen Ikigai belum di-route, atau ada referensi yang missing.

### Step 3: Fix minimum agar build sukses

- Pastikan semua screen Ikigai (Assessment, ReportLoading, Report) ditambahkan ke `Screen.kt` sebagai sealed object route
- Pastikan `MainActivity.kt` ada `composable("ikigai_assessment") { ... }`, `composable("ikigai_report") { ... }`, dll
- Pastikan import tidak ada yang missing
- Fix error build lain yang muncul **minimal** (jangan redesign)

### Step 4: Tambah nav dari IkigaiDashboardScreen

Cek `IkigaiDashboardScreen.kt` — kalau ada tombol "Mulai Assessment" / "Lihat Report", pastikan `onClick` memanggil `navController.navigate(...)` ke route yang baru ditambah di Step 3.

### Step 5: Build ulang (verifikasi)

```bash
./gradlew assembleDebug 2>&1 | tail -30
```

Harus `BUILD SUCCESSFUL`. Kalau gagal, **jangan commit**, tulis entry CHANGELOG ❌ dengan diagnosis.

### Step 6: Commit

```bash
cd /c/laragon/www/MindRest_AI
git add app/src/main/java/com/example/features/ikigai/ \
        app/src/main/java/com/example/core/navigation/Screen.kt \
        app/src/main/java/com/example/MainActivity.kt \
        app/src/main/java/com/example/core/network/SupabaseClient.kt \
        supabase/functions/generate-ikigai-report/ \
        supabase/migrations/002_create_ikigai.sql \
        supabase/migrations/003_ikigai_reports_update_policy.sql \
        supabase/backfill_profiles.sql
git commit -m "feat(ikigai): wire M2 (assessment) + M3 (report) screens + edge function

- Activate IkigaiAssessmentScreen, IkigaiReportLoadingScreen, IkigaiReportScreen
  in NavHost (routes: ikigai_assessment, ikigai_report/{assessmentId})
- Add nav from IkigaiDashboardScreen to assessment/report
- Add DTOs + Repository for ikigai_assessments & ikigai_reports
- Add Edge Function generate-ikigai-report (Deno, Gemini call)
- Add migration 002 (create ikigai tables) + 003 (update policy)
- Add backfill_profiles.sql (one-time data fix)"
```

### Step 7: Update CHANGELOG.md

Tambah entry di section `## Timeline` (paling bawah) dengan format dari `ORCHESTRATION.md`. Lalu update tabel Master Status:
- FR-012: 🟡 → 🟢 (kalau Assessment UI bisa dibuka dari Dashboard)
- FR-013: 🟡 → 🟢 (kalau Report UI ada; Edge Function wiring dites terpisah di T-003)

**Kalau Edge Function `generate-ikigai-report` belum dites end-to-end** (butuh `GEMINI_API_KEY`), tulis di `Risks/Notes`: "FR-013 UI wired, Edge Function belum dites karena butuh GEMINI_API_KEY — masuk T-003".

---

## ✔️ Acceptance Checklist (semua harus ✅)

- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
- [ ] `git status` bersih (tidak ada lagi untracked Ikigai work — boleh ada untracked lain yang bukan Ikigai, abaikan)
- [ ] Commit baru ada: `git log --oneline -3` menampilkan feat(ikigai) M2+M3
- [ ] `CHANGELOG.md` punya entry baru dengan format benar
- [ ] Master Status tabel di CHANGELOG.md diupdate untuk FR-012, FR-013

---

## 📝 Reporting (WAJIB — agent isi di CHANGELOG)

```markdown
### [<tanggal> <jam>] T-001 | agent: <identifier> | FR: FR-012, FR-013
**Goal**: Stabilize & commit Ikigai M2 (Assessment) + M3 (Report) work yang uncommitted
**Files changed**: <list path dari git diff --stat, atau "none — pure verification">
**Acceptance**:
- [✅|❌] gradle assembleDebug sukses
- [✅|❌] git status bersih untuk file Ikigai
- [✅|❌] Commit baru ada di git log
- [✅|❌] CHANGELOG.md terupdate
- [✅|❌] Master Status FR-012/FR-013 diupdate
**Build**: ✅ sukses | ❌ <error>
**Risks/Notes**: <kalimat — penting: apakah Edge Function generate-ikigai-report dites runtime? Butuh GEMINI_API_KEY?>
**Next blocker**: <kalimat — biasanya "siap untuk T-002 (sleep aggregation commit)" atau "FR-013 Edge Function runtime test tertunda sampai T-003">
---
```

---

## 🚨 Kalau Gagal

Jangan commit. Tulis entry CHANGELOG dengan `Build: ❌` dan diagnosis lengkap di `Risks/Notes`. Lapor ke mediator dengan error message.
