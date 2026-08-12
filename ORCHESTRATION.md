# ORCHESTRATION — MindRest_AI

> Dokumen ini = **kontrak kerja** antara **orchestrator (saya, sesi konsultasi)**,
> **mediator (Anda)**, dan **AI agent (sesi eksekusi)**. Baca sebelum mulai kerja apa pun.

---

## 🔁 Alur Kerja (satu task = satu loop)

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. ORCHESTRATOR (saya, sesi ini)                                │
│    → Rancang task berikutnya berdasarkan CHANGELOG + ROADMAP    │
│    → Tulis prompt lengkap di TASKS/T-XXX-*.md                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓ copy-paste prompt
┌─────────────────────────────────────────────────────────────────┐
│ 2. MEDIATOR (Anda)                                              │
│    → Buka sesi AI agent BARU (atau lanjut sesi yang ada)        │
│    → Paste prompt dari TASKS/T-XXX-*.md                         │
└─────────────────────────────────────────────────────────────────┘
                              ↓ agent kerja
┌─────────────────────────────────────────────────────────────────┐
│ 3. AI AGENT (sesi eksekusi)                                     │
│    → Baca CHANGELOG.md (cek entry sebelumnya)                   │
│    → Baca ROADMAP.md + file-file di "Read First" pada prompt    │
│    → Kerjakan sesuai scope pada prompt                          │
│    → WAJIB update CHANGELOG.md (entry baru di Timeline)         │
│    → Lapor balik ke mediator dengan ringkasan + link entry      │
└─────────────────────────────────────────────────────────────────┘
                              ↓ lapor hasil
┌─────────────────────────────────────────────────────────────────┐
│ 4. MEDIATOR (Anda)                                              │
│    → Buka sesi orchestrator (saya) lagi                         │
│    → Lapor: "T-XXX selesai, link entry CHANGELOG, ada blocker X"│
└─────────────────────────────────────────────────────────────────┘
                              ↓ saya review
┌─────────────────────────────────────────────────────────────────┐
│ 5. ORCHESTRATOR (saya)                                          │
│    → Verifikasi CHANGELOG entry + diff vs acceptance            │
│    → Update Master Status tabel di CHANGELOG.md                 │
│    → Rancang task berikut                                       │
└─────────────────────────────────────────────────────────────────┘
```

**Kunci**: tiap task = **1 sesi agent** (jangan stack 2 task dalam 1 prompt).

---

## 📂 File Peta (kita pakai 4 file saja sebagai sumber kebenaran)

| File | Fungsi | Siapa yang update |
|---|---|---|
| **`CHANGELOG.md`** | Buku besar milestone + status FR per-task | **Agent** (wajib tiap selesai) + **Orchestrator** (master status tabel) |
| **`ORCHESTRATION.md`** (ini) | Alur kerja + sequencing task | **Orchestrator** saja |
| **`TASKS/T-XXX-*.md`** | Prompt siap copy-paste per task | **Orchestrator** saja |
| **`ROADMAP.md`** | Roadmap 4-minggu + 9 task spec awal | **Orchestrator** saja (jika sequencing berubah) |

File lain (`AUDIT.md`, `TASKS_FASE2.md`, `Dokumen Teknis*.md`) = **referensi baca**, tidak diubah agent.

---

## 📊 Live Status Board (snapshot)

> Snapshot ini hanya untuk **referensi cepat**. Sumber kebenaran = tabel di `CHANGELOG.md`.

**Sedang berjalan**: T-004 + T-005 ✅ done (T-004 commit e96481e FR-015 🟢; T-005 commits 6410a68 + c0ef359 FR-014 🔴→🟡). Mig 005 di-apply & EF `generate-sleep-insight` di-deploy oleh orchestrator (agent T-005 skip step deploy). Lanjut T-007 (Auth) atau T-009 (Reminder/Relax).

**Next up** (urut, jangan loncat):
1. **T-007** — Auth flow end-to-end test (FR-001, FR-002) — sign-up + login flow
2. **T-009** — Reminder + Relax (FR-016, FR-017) — notification + audio playback

**Backlog** (nanti, polling final):
- **T-006** — Profile edit (FR-003)
- **T-010** — Statistics charts pakai data riil (Fase 3 dari AUDIT)
- **T-011** — Edit/delete logs (Fase 4 dari AUDIT)
- Runtime E2E test FR-009, FR-011, FR-014 — butuh user JWT nyata (sign-up via app, kirim chat/refresh insight)
- FR-013 Edge Function `generate-ikigai-report` runtime test masih deferred
- PR upstream ke `RozanRamadani/MindRest_AI`
- Commit proposal/SRS/build.bat (kalau user mau)

---

## ⚠️ Aturan Mutlak untuk Agent

1. **CHANGELOG entry WAJIB.** Tidak ada entry = task dianggap tidak selesai.
2. **Jangan edit file di luar Scope** yang disebut prompt.
3. **Jangan skip Read First** — lihat file yang dirujuk sebelum coding.
4. **Build WAJIB** dicatat statusnya. Kalau gagal, jangan commit, tulis entry ❌ + blocker.
5. **1 task = 1 sesi.** Jangan kerjakan 2 task dalam 1 prompt meskipun terasa cepat.
6. **Format entry CHANGELOG persis** seperti template — orchestrator parse secara otomatis.
7. **Kalau ragu**, lebih baik entry "investigasi saja" dengan `Files changed: none` daripada diam.

---

## 🚨 Risk Register (yang harus diwaspadai tiap task)

| Risiko | Mitigasi |
|---|---|
| Agent lupa update CHANGELOG | Orchestrator cek entry pertama saat verifikasi |
| Agent kerja di luar scope | Setiap prompt sudah list "DON'T Touch" |
| Uncommitted work hilang | T-001 WAJIB dijalankan pertama untuk commit Ikigai M2/M3 |
| `GEMINI_API_KEY` di Supabase secrets | ✅ VERIFIED (HTTP 200, 666ms, model `gemini-3.5-flash-lite`). Lihat `verify-gemini.ps1` + note untuk re-run kalau perlu |
| `JAVA_HOME` belum ter-set di shell Windows default | Agent T-001b harus `set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` manual sebelum `./gradlew`. Tambahkan ke README/setup supaya tidak hit blocker tiap task |
| `.env.example` masih modified dari sesi sebelumnya | Perlu review terpisah (orchestrator atau T khusus) sebelum commit — perubahan agent lama belum final |
| `SUPABASE_URL`/`ANON_KEY` masih placeholder | Agent akan dapat error network, tulis ❌ di CHANGELOG dengan diagnosis |

---

## 🛠️ Cara Mediator Pakai Workflow Ini (intruksi untuk Anda)

1. **Mulai sekarang**: copy prompt dari `TASKS/T-001-*.md`.
2. Buka sesi AI agent **baru** (agar context bersih — kecuali T-001 perlu baca state lokal, baru lanjut sesi yang sudah baca repo).
3. Paste prompt. Tunggu agent selesai.
4. **Verifikasi cepat**: buka `CHANGELOG.md`, cek apakah entry baru ada di Timeline + status FR berubah.
5. Balik ke saya, lapor: *"T-001 selesai, [link/quote entry CHANGELOG], ada/tidak ada blocker"*.
6. Saya update Master Status tabel, lalu tulis prompt T-002.

---

## 📌 Update Log (orchestrator)

- **v1** (2026-08-10): setup awal. CHANGELOG + ORCHESTRATION + TASKS/T-001..T-005 dirancang. Master status FR-001..FR-017 diinisialisasi dari audit + git log.
