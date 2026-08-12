# Copy-Paste Prompts — UI Migration Eksekusi

> Tiap blok di bawah = **1 prompt siap tempel** ke AI agent manapun
> (Claude Code, Cursor, pi, Copilot CLI, dll.). Mulai dari atas, sesuai kebutuhan.

Semua prompt mengasumsikan:
- Repo di `C:/laragon/www/MindRest_AI` (atau cwd setara)
- Agent sudah punya akses baca/tulis file + shell
- `JAVA_HOME` = `C:\Program Files\Android\Android Studio\br` (Windows)

---

## 🟢 PROMPT 1 — Boot sesi (jalankan di awal SETIAP sesi baru)

> Pakai ini sebelum mengerjakan tiket apapun. Membiasakan agent dengan konteks.

```
Kamu akan mengeksekusi migrasi UI MindRest AI. Baca berurutan dulu sebelum melakukan apapun:

1. .scratch/ui-migration/AGENT_WORKFLOW.md  (aturan main)
2. .scratch/ui-migration/MIGRATION_GUIDE.md  (resep 9 langkah + dp→token mapping)
3. .scratch/ui-migration/README.md           (status board — cari tiket ⬜ todo berikutnya)

Lalu lapor ke saya: tiket mana yang akan kamu kerjakan, total hardcoded dp/sp/hex di file itu, dan apakah ada blocker (mis. perlu komponen baru). Jangan mulai edit sebelum saya konfirmasi.

Aturan keras:
- Jangan ubah signature composable / ViewModel / Repository. Hanya presentation layer.
- 1 tiket = 1 branch = 1 PR. Jangan campur tiket.
- Mock data / backend wiring TIDAK disentuh (tiket terpisah).
- JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" untuk gradle.
```

---

## 🟡 PROMPT 2 — Eksekusi 1 tiket (setelah boot)

> Ganti `<NN>` dan `<screen>` sesuai tiket. Ini prompt inti per-tiket.

```
Kerjakan tiket <NN> (migrasi <Screen> ke design token).

Ikuti persis 9 langkah di .scratch/ui-migration/MIGRATION_GUIDE.md.
Detail spesifik tiket ini ada di .scratch/ui-migration/issues/<NN>-<screen>.md.

Alur:
1. git checkout -b ui/migrate-<screen>
2. Build + install + screenshot SEBELUM → simpan ke .scratch/ui-migration/screenshots/<NN>-before.png
   (JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"; adb=C:/Users/lenovo/AppData/Local/Android/Sdk/platform-tools/adb.exe)
3. Eksekusi 9 langkah migrasi (AppScaffold + screenEdge, dp→spacing token, sp→typography, hex→colorScheme, Card→AppCard, SectionHeader, EmptyState)
4. ./gradlew :app:compileDebugKotlin --console=plain  → harus 0 error, 0 warning baru
5. Build + install + screenshot SESUDAH → <NN>-after.png
6. Jalankan reviewer subagent (fresh context) untuk cek correctness + cleanup
7. Update status board di README.md (tiket <NN> → ✅), commit dengan pesan:
   "refactor(ui): migrate <Screen> to design tokens (ticket <NN>)"
8. Lapor: diff summary + screenshot before/after + sisa hardcoded (dp/sp/hex count sebelum vs sesudah)

Jangan push/merge sebelum saya review hasilnya.
```

---

## 🔵 PROMPT 3 — Reviewer mandiri (kalau mau cek PR tiket yang sudah selesai)

> Pakai di sesi terpisah untuk review independen, fresh context.

```
Review PR tiket migrasi UI <NN> (<Screen>) secara independen (fresh context, belum pernah lihat implementasinya).

Baseline: commit foundation (Motion + 9 components + showcase).
Diff: git diff main...ui/migrate-<screen>

Cek 2 axis:
A. STANDARDS — ikut MIGRATION_GUIDE.md?
   - 0 hardcoded dp baru (kecuali ukuran ikon/stroke/fixed)
   - 0 hardcoded .sp (semua via MaterialTheme.typography)
   - 0 hardcoded Color(0x...) (semua via colorScheme)
   - Root pakai AppScaffold + Modifier.screenEdge()/screenEdgePadded()
   - Card pakai AppCard, section title pakai SectionHeader, empty state pakai EmptyState
   - Tidak ubah signature composable / ViewModel / Repository
B. SPEC — visual parity?
   - Bandingkan .scratch/ui-migration/screenshots/<NN>-before.png vs <NN>-after.png
   - Layout identik, hanya lebih konsisten (tidak ada regression)

Lapor: PASS/FAIL per axis + list masalah konkretnya (file:line) + saran fix. Jangan apply fix sendiri.
```

---

## 🟠 PROMPT 4 — Selesaikan wave (run多条 tiket berurutan, 1 sesi)

> Hanya kalau context window cukup & screen-screen wave itu ringan. Berisiko degradasi kualitas kalau terlalu banyak. Preferensi: tetap clear per tiket.

```
Kerjakan wave <N> secara berurutan dalam sesi ini. Tiket: <daftar NN screen>.

Untuk SETIAP tiket:
1. Ikuti PROMPT 2 lengkap (screenshot before, 9 langkah, build, screenshot after, review, commit)
2. Update status board README.md ke ✅ setelah commit
3. Tampilkan ringkasan singkat tiket selesai, lalu LANJUT tiket berikutnya

Jika context mulai penuh (approaching ~120k token) atau kualitas turun, STOP setelah tiket terakhir yang sukses, lapor sisanya untuk sesi baru. Jangan dorong ke kondisi degradasi.

Wave <N> selesai ketika semua tiketnya ✅ + build hijau.
```

---

## 🔴 PROMPT 5 — Definition of done check (tiket terakhir selesai)

> Pakai setelah tiket 20 selesai, untuk verifikasi seluruh migrasi.

```
Verifikasi seluruh UI migration sudah selesai (definition of done di AGENT_WORKFLOW.md).

Cek semua:
1. Status board README.md: 20 tiket harus ✅ done
2. grep -rcE '\b[0-9]+\.dp\b' app/src/main/java/com/example/features/  → total sisa < 50
3. grep -rcE 'Color\(0x' app/src/main/java/com/example/features/  → harus 0
4. ./gradlew :app:compileDebugKotlin --console=plain  → 0 error, 0 warning
5. ./gradlew :app:assembleDebug  → APK sukses
6. Buka DesignSystemShowcaseScreen (adb deep link mindrest://designsystem) di light + dark → tampil benar

Lapor: PASS/FAIL per cek + buat tag git `v1.1-ui-foundation` kalau semua pass (jangan push tag sebelum saya konfirmasi).
```

---

## ⚙️ PROMPT 6 — Eksrtraksi komponen baru (kalau ada tiket butuh komponen belum ada)

> Pakai ketika agent menemukan screen butuh komponen yang belum ada di foundation. Stop dulu, ekstrak komponen, baru lanjut tiket.

```
Tiket <NN> (<Screen>) butuh komponen baru yang belum ada di foundation: <nama komponen> (deskripsi: <apa fungsinya>).

Jangan inline di screen. Ikuti pola foundation yang sudah ada:
1. Buat file baru di app/src/main/java/com/example/core/designsystem/components/<Nama>.kt
2. Pakai token (LocalSpacing, LocalShapes, LocalElevation, LocalMotion, MaterialTheme.colorScheme/typography)
3. Tambah @Preview (light + dark + dynamic color)
4. Tambahkan ke DesignSystemShowcaseScreen.kt sebagai section baru
5. Update MIGRATION_GUIDE.md: tambah komponen di resep
6. Commit terpisah: "feat(design-system): add <Nama> component"
7. Baru lanjut eksekusi tiket <NN>

Lapor dulu sebelum bikin: apakah komponen ini benar-benar reusable (dipakai ≥2 screen) atau cukup inline? Kalau hanya 1 screen, inline saja.
```

---

## 📋 Checklist sebelum mulai sesi pertama

- [ ] Foundation sudah di-commit & di-push (Sesi 0 di AGENT_WORKFLOW.md)
- [ ] `git status` bersih di main
- [ ] Emulator/device terhubung (`adb devices`)
- [ ] Folder `.scratch/ui-migration/screenshots/` dibuat
- [ ] JAVA_HOME + path adb diketahui agent

## 📋 Checklist sebelum tutup sesi

- [ ] Status board README.md up-to-date (tiket ✅ atau 🟡)
- [ ] Semua commit sudah di-push (atau PR dibuka)
- [ ] `git status` bersih (tidak ada perubahan menggantung)
- [ ] Screenshot before/after tersimpan
- [ ] Catat tiket berikutnya untuk sesi depan (atau agent baca status board)
