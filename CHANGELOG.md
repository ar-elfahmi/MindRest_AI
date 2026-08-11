# CHANGELOG — MindRest_AI

> **BACA DULU** sebelum kerja. Wajib update file ini setiap selesai task.
> Append entry baru di paling bawah. **JANGAN hapus/edit entry lama.**

---

## 🔒 Aturan Wajib untuk AI Agent (semua sesi)

1. **Sebelum mulai kerja**: baca file ini + `ORCHESTRATION.md` + `ROADMAP.md` (10 menit).
2. **Setelah selesai kerja** (sukses ATAU gagal): tambahkan entry di section `## Timeline` dengan format tepat di bawah.
3. **JANGAN skip** — orchestrator (saya) akan menolak task yang tidak update CHANGELOG.
4. **Build WAJIB** dicatat statusnya (✅ / ❌ dengan error ringkas).
5. **FR-ID wajib** diisi — kalau task tidak terkait FR langsung, isi `INFRA` atau `BUGFIX`.
6. **Waktu**: pakai timestamp device lokal (HH:MM sudah cukup, tidak perlu detik).
7. **Jika task gagal**: tetap tulis entry dengan status ❌, isi `Risks/Notes` dengan diagnosis, dan tulis `Next blocker` = apa yang harus diperbaiki task berikut.

---

## 📋 Format Entry (COPY-PASTE template)

```markdown
### [YYYY-MM-DD HH:MM] T-XXX | agent: <nama/identifier> | FR: <FR-XXX atau INFRA/BUGFIX>
**Goal**: <1 kalimat tujuan task>
**Files changed**: <list path, atau "none — investigasi saja">
**Acceptance**:
- [✅|❌] <item 1>
- [✅|❌] <item 2>
- [✅|❌] <item 3>
**Build**: ✅ sukses | ❌ <singkat error / "tidak dijalankan karena <alasan>">
**Risks/Notes**: <kalimat, atau "none">
**Next blocker**: <apa untuk task berikut, atau "none">
---
```

---

## 🎯 Master Status per FR (diupdate oleh orchestrator / agent)

> ✅ = selesai & terverifikasi build · 🟡 = parsial / uncommitted · 🔴 = belum mulai · ❌ = blocked

| FR | Fitur | Status | Last Task | Commit | Catatan |
|---|---|---|---|---|---|
| FR-001 | Email register | 🟡 | T-007 (planned) | — | AuthRepository ada, UI LoginForm perlu verifikasi |
| FR-002 | Login + logout | 🟡 | T-007 (planned) | — | AuthRepository.signIn/signOut ada, flow UI perlu verifikasi |
| FR-003 | Lengkapi/update profil | 🟡 | T-007 (planned) | — | ProfileScreen ada, edit belum wired |
| FR-004 | Catat tidur (jam + bangun) | 🟢 | T-1.2 | a7b5fbb | FAB SleepHub → SleepTrackingScreen aktif |
| FR-005 | Hitung durasi tidur | 🟢 | T-1.2 | a7b5fbb | bed_time + wake_up_time → durMinutes di SleepTracking form |
| FR-006 | Riwayat tidur | 🟡 | T-2B | uncommitted | SleepHubScreen aggregation 2B selesai lokal, belum commit |
| FR-007 | Catat mood (skor 1-5) | 🟢 | T-1.1 | a7b5fbb | Bottom sheet sudah pakai moodScore langsung |
| FR-008 | Riwayat mood | 🟢 | T-2A | task-2a-output.md | weeklyScores pakai query riil, belum commit |
| FR-009 | Journaling via AI Chatbot | 🟡 | T-003 (planned) | — | AiJournalScreen UI ada, Edge Function belum dipanggil |
| FR-010 | Riwayat jurnal | 🟢 | T-2C | task-2c-output.md | WeeklyMoodTimeline wired, belum commit |
| FR-011 | Olah data jurnal → insight | 🔴 | — | — | Belum dimulai (bergantung FR-009 fix) |
| FR-012 | Isi 6 pertanyaan Ikigai | 🟢 | T-001 | f4ee87e | IkigaiAssessmentScreen wired ke Dashboard via NavHost, 6 step form + insert ke DB |
| FR-013 | Rekomendasi pengembangan diri | 🟢 | T-001 | f4ee87e | IkigaiReportScreen wired (4 lingkaran + laporan + rekomendasi); Edge Function generate-ikigai-report ter-commit, runtime test butuh GEMINI_API_KEY di T-003 |
| FR-014 | Rekomendasi aktivitas/makanan dari sleep | 🔴 | — | — | Belum dimulai (perlu Gemini) |
| FR-015 | Dashboard ringkasan | 🟡 | T-2A+2B | uncommitted | 2A done, 2B in-progress, Ikigai progress widget perlu wire |
| FR-016 | Notifikasi pengingat | 🟡 | T-008 (planned) | — | BedtimeNotificationReceiver ada, scheduler belum fired |
| FR-017 | Akses relaksasi (audio) | 🟡 | T-009 (planned) | — | RelaxScreen UI ada, audio playback perlu verifikasi |

**Overall progress: 4/17 ✅ hijau · 9/17 🟡 parsial · 4/17 🔴 belum**

---

## 📅 Timeline (append di bawah — agent isi)

<!-- Agent: tambahkan entry baru di sini, JANGAN hapus entry lama -->

### [2026-08-11 16:36] T-001 | agent: pi-coder | FR: FR-012, FR-013
**Goal**: Stabilize & commit Ikigai M2 (Assessment) + M3 (Report) work yang uncommitted
**Files changed**: 19 files (2960 ins, 2 del):
- app/src/main/java/com/example/features/ikigai/ (semua sub-tree: data/dto, data/repository, presentation/screen, presentation/state, presentation/viewmodel)
- app/src/main/java/com/example/core/navigation/Screen.kt (3 route Ikigai baru)
- app/src/main/java/com/example/MainActivity.kt (3 composable block + nav wiring)
- app/src/main/java/com/example/core/network/SupabaseClient.kt (install Functions plugin)
- app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiDashboardScreen.kt (tombol "Mulai Assessment Ikigai" + onStartAssessment param)
- supabase/functions/generate-ikigai-report/index.ts (Edge Function Deno, Gemini)
- supabase/migrations/002_create_ikigai.sql (tabel + RLS)
- supabase/migrations/003_ikigai_reports_update_policy.sql (policy UPDATE)
- supabase/backfill_profiles.sql (one-time data fix)
**Acceptance**:
- [✅] ./gradlew assembleDebug BUILD SUCCESSFUL (clean rebuild dengan --rerun-tasks, hanya deprecation warnings)
- [✅] git status bersih untuk file Ikigai (semua Ikigai sub-tree ter-commit; sisa untracked = non-Ikigai di luar scope)
- [✅] Commit baru ada: f4ee87e "feat(ikigai): wire M2 (assessment) + M3 (report) screens + edge function"
- [✅] CHANGELOG.md terupdate (Timeline entry + Master Status)
- [✅] Master Status FR-012/FR-013 diupdate 🟡 → 🟢
**Build**: ✅ sukses — verified dengan ./gradlew clean :app:compileDebugKotlin --no-build-cache --rerun-tasks (1m 14s, 0 errors)
**Risks/Notes**:
- FR-013 Edge Function `generate-ikigai-report` BELUM dites runtime end-to-end (butuh `GEMINI_API_KEY` di Supabase secrets). Wiring UI (auto-trigger dari Assessment → invoke function → render report) sudah ter-commit; runtime test masuk T-003 atau T-005 sesuai Live Status Board.
- T-001 tidak menyentuh `app/build.gradle.kts` & `gradle/libs.versions.toml` (modified di luar scope) — keduanya sudah ada `implementation(libs.supabase.functions)` di lokal & libs catalog, jadi build hijau. File ini akan di-commit terpisah (lihat Next blocker).
- Edge Function `generate-ikigai-report/index.ts` import dari `_shared/cors.ts` & `_shared/prompts/ikigai.ts` yang juga untracked. File ini TIDAK di-commit di T-001 karena di luar scope — lihat Next blocker.
- Sisa untracked non-Ikigai (sleep, journal, schema.sql, etc.) sengaja tidak disentuh — masuk T-002, T-003, dll.
**Next blocker**: T-002 (commit sleep aggregation 2B) bisa jalan tanpa menunggu ini. Untuk T-003 yang test `generate-ikigai-report` end-to-end, perlu commit dulu: (a) `supabase/functions/_shared/cors.ts` + `_shared/prompts/ikigai.ts` (dependency fungsi), (b) `app/build.gradle.kts` + `gradle/libs.versions.toml` (catalog `supabase-functions`). Bisa di-handle di awal T-003 sebelum step Edge Function live test.
---


