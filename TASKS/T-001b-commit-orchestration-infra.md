# T-001b — Commit Orchestration Docs + Edge Function Infra

**Priority**: 🟠 Tinggi (ringan, 10–15 menit) — jalan **sebelum T-003**
**Estimated effort**: 10–15 menit
**FR terkait**: INFRA (mendukung FR-013 runtime test, FR-009, FR-014)
**Dependencies**: T-001 sukses (sudah ✅)
**Blocks**: T-003 (Edge Function live test), T-005 (idem)

---

## 🎯 Goal

Commit semua file infrastruktur yang masih uncommitted dari T-001 sehingga:
1. Workflow source-of-truth (CHANGELOG, ORCHESTRATION, TASKS) aman di git history
2. Fresh clone bisa build sukses (gradle catalog + Functions plugin ter-commit)
3. Edge Function `generate-ikigai-report` punya dependency (`_shared/`) ter-commit

---

## 📖 Context (apa yang sudah terjadi)

T-001 hanya commit file Ikigai. Sisa uncommitted:

**WAJIB commit (mendukung workflow + build):**
- `CHANGELOG.md` (or单 source of truth milestone)
- `ORCHESTRATION.md` (workflow contract)
- `TASKS/T-001-*.md` s/d `T-005-*.md` (5 prompt siap copy-paste)
- `app/build.gradle.kts` (modifikasi: +1 line `implementation(libs.supabase.functions)`)
- `gradle/libs.versions.toml` (modifikasi: +1 line catalog `supabase-functions`)
- `supabase/functions/_shared/cors.ts` (helper CORS untuk semua Edge Functions)
- `supabase/functions/_shared/prompts/ikigai.ts` (system prompt Gemini untuk Ikigai)
- `supabase/functions/README.md` (dokumentasi cara deploy Edge Functions)
- `supabase/functions/hello/index.ts` (sanity check Edge Function)
- `supabase/functions/test-gemini/index.ts` (Gemini call smoke test)
- `supabase/migrations/001_extend_profiles.sql` (extend profiles table)

**OPSIONAL commit (dokumen historis kerja sesi sebelumnya):**
- `task-2a-output.md` (laporan Task 2A — Mood aggregation)
- `task-2c-output.md` (laporan Task 2C — WeeklyMoodTimeline)

**JANGAN commit di task ini (di luar scope, handle terpisah kalau perlu):**
- `Dokumen Teknis.md`, `Dokument Teknis revisi.md`, `IMPLEMENTASI PERANGKAT LUNAK.md` (dokumen proposal/SRS — besar, masih draft)
- `contoh proposal (masih perlu banyak revisi).md` (draft)
- `srs revisi.docx` (binary)
- `build.bat` (build script lokal Windows, cek dulu isinya)
- `supabase/.temp/` (temporary folder Supabase CLI)

---

## 📚 Read First (urutan)

1. `CHANGELOG.md` — entry T-001 harus sudah ada (verify line 70+)
2. `ORCHESTRATION.md` — context workflow
3. `gradle/libs.versions.toml` — lihat catalog entry untuk `supabase-functions`
4. `app/build.gradle.kts` — lihat dependencies block

---

## ✅ Scope (file yang BOLEH di-commit)

Semua yang disebut di section "WAJIB commit" + (opsional) "OPSIONAL commit" di atas.

## ❌ DON'T Touch

- File Ikigai (sudah ter-commit di T-001)
- File Sleep/Journal/Mood (masuk T-002+)
- `supabase/schema.sql` (master schema — modifikasi belum final)
- `.env.example` (modifikasi belum final — agent sesi sebelumnya ubah, perlu review dulu)
- File di section "JANGAN commit" di atas

---

## 🛠️ Implementation Steps

### Step 1: Review modified gradle files

```bash
cd /c/laragon/www/MindRest_AI
git diff app/build.gradle.kts gradle/libs.versions.toml
```

Verifikasi hanya +1 line yang dimaksud (catalog `supabase-functions` + dependency). Kalau ada perubahan lain yang tidak terkait, **JANGAN commit file ini** — tulis entry ❌ dan lapor.

### Step 2: Verifikasi shared files exist

```bash
ls -la supabase/functions/_shared/cors.ts supabase/functions/_shared/prompts/ikigai.ts \
       supabase/functions/hello/index.ts supabase/functions/test-gemini/index.ts
```

Kalau ada yang missing, tulis entry ❌ + diagnosis.

### Step 3: Stage files per group

Lakukan **3 commit terpisah** (jangan digabung, biar history mudah di-review):

**Commit 1: Orchestration workflow**
```bash
cd /c/laragon/www/MindRest_AI
git add CHANGELOG.md ORCHESTRATION.md TASKS/
git commit -m "chore(orchestration): add CHANGELOG + ORCHESTRATION + TASKS workflow

Single source of truth untuk AI agent sessions:
- CHANGELOG.md: master status 17 FR + format entry milestone
- ORCHESTRATION.md: alur kerja orchestrator ↔ mediator ↔ agent
- TASKS/T-001..T-005.md: 5 prompt siap copy-paste untuk task awal

Setiap AI agent WAJIB baca ORCHESTRATION.md sebelum kerja dan update
CHANGELOG.md setelah kerja."
```

**Commit 2: Edge Function infra + shared helpers**
```bash
cd /c/laragon/www/MindRest_AI
git add supabase/functions/_shared/ \
        supabase/functions/README.md \
        supabase/functions/hello/ \
        supabase/functions/test-gemini/ \
        supabase/migrations/001_extend_profiles.sql
git commit -m "chore(supabase): add Edge Function infra + shared helpers

- supabase-functions: Functions plugin (libs catalog)
- _shared/cors.ts: CORS helper untuk semua Edge Functions
- _shared/prompts/ikigai.ts: system prompt Gemini untuk Ikigai
- hello/index.ts: sanity check Edge Function
- test-gemini/index.ts: Gemini call smoke test (butuh GEMINI_API_KEY)
- README.md: dokumentasi deploy Edge Functions via Supabase CLI
- migrations/001_extend_profiles.sql: extend profiles table

Required for: T-001 (Ikigai), T-003 (chat-gemini), T-005 (sleep-insight)."
```

**Commit 3: Gradle catalog untuk Functions plugin**
```bash
cd /c/laragon/www/MindRest_AI
git add app/build.gradle.kts gradle/libs.versions.toml
git commit -m "build(gradle): enable Supabase Functions plugin

- libs.versions.toml: add supabase-functions catalog entry
- app/build.gradle.kts: implementation(libs.supabase.functions)

Required by: supabase/functions/* Edge Function clients in Kotlin."
```

**Commit 4 (opsional, hanya jika ingin): Historical task outputs**
```bash
cd /c/laragon/www/MindRest_AI
git add task-2a-output.md task-2c-output.md
git commit -m "docs: add historical task outputs (T-2A, T-2C)

Capture hasil kerja sesi sebelumnya untuk referensi reviewer."
```

### Step 4: Verifikasi akhir

```bash
cd /c/laragon/www/MindRest_AI
git log --oneline -8      # 4 commit baru (atau 3+1 opsional)
git status --short        # harus bersih untuk file yang di-commit
```

### Step 5: Update CHANGELOG

Tambah entry Timeline:
```markdown
### [<tanggal> <jam>] T-001b | agent: <identifier> | FR: INFRA
**Goal**: Commit orchestration workflow docs + Edge Function infra + gradle catalog
**Files changed**: (list 4 commit di atas)
**Acceptance**:
- [✅|❌] 3 (atau 4) commit terpisah sukses
- [✅|❌] git status bersih untuk file yang di-commit
- [✅|❌] Fresh clone bisa build sukses (verify dengan `./gradlew assembleDebug`)
- [✅|❌] CHANGELOG entry ditulis
**Build**: ✅ sukses | ❌ <error>
**Risks/Notes**: <none atau catatan>
**Next blocker**: T-002 (sleep aggregation commit) bisa langsung jalan
---
```

---

## ✔️ Acceptance

- [ ] 3 (atau 4) commit terpisah sukses
- [ ] `git status` bersih untuk file yang di-commit
- [ ] `./gradlew assembleDebug` masih sukses setelah commit (jalankan untuk verify)
- [ ] CHANGELOG entry T-001b ada + format benar
- [ ] ORCHESTRATION.md live status board T-001b = ✅ done

---

## 📝 Reporting (format sama dengan T-001)

Setelah agent commit sukses, tambah catatan:
- Berapa commit yang dibuat (3 atau 4)
- Apakah verifikasi `./gradlew assembleDebug` dilakukan setelah commit (seharusnya iya)
- Apakah ada file dari list "JANGAN commit" yang ternyata ikut ke-commit (seharusnya tidak)
