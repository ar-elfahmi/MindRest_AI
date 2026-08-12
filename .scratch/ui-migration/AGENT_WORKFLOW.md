# Agent Workflow — Eksekusi UI Migration (Multi-Sesi)

> **Operating manual untuk AI agent di sesi baru.** Satu tiket = satu sesi.
> State dilewatkan via **file + git**, bukan memori antar-sesi.

## 📚 Baca pertama (urut) — di SETAP sesi baru

Sebelum mengerjakan apapun, agent WAJIB baca 3 file ini berurutan:

1. **File ini** — `AGENT_WORKFLOW.md` (aturan main)
2. **`MIGRATION_GUIDE.md`** — resep 9 langkah + dp→token mapping table
3. **`README.md`** — status board (cari tiket `⬜ todo` berikutnya)
4. Tiket yang dipilih (mis. `issues/01-home-screen.md`)

Setelah baca, agent tahu persis: apa konteksnya, tiket mana berikutnya, dan resepnya.

---

## 🚨 Sesi 0 — PRE-FLIGHT (sekali saja, sebelum tiket pertama)

> **Bloker kritis:** Foundation UI (sesi sebelumnya) sudah dibangun tapi **belum di-commit**.
> 17 file berubah (Color/Theme/MainActivity/Screen + 11 file baru + showcase).
> Fresh agent tidak bisa mulai bersih sampai ini di-commit.

**Eksekusi sekali di sesi pertama** (atau Anda lakukan sendiri sebelum agent lain mulai):

```bash
cd C:/laragon/www/MindRest_AI

# 1. Verifikasi build masih hijau
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:compileDebugKotlin --console=plain   # harus SUCCESS

# 2. Commit foundation (pisahkan jadi 2 commit bersih)
git add app/src/main/java/com/example/core/designsystem/Motion.kt \
        app/src/main/java/com/example/core/designsystem/Color.kt \
        app/src/main/java/com/example/core/designsystem/Theme.kt \
        app/src/main/java/com/example/core/designsystem/components/AppScaffold.kt \
        app/src/main/java/com/example/core/designsystem/components/AppCard.kt \
        app/src/main/java/com/example/core/designsystem/components/SectionHeader.kt \
        app/src/main/java/com/example/core/designsystem/components/EmptyState.kt \
        app/src/main/java/com/example/core/designsystem/components/LoadingShimmer.kt \
        app/src/main/java/com/example/core/designsystem/components/ScreenEdge.kt \
        app/src/main/java/com/example/core/designsystem/components/BrandHeader.kt \
        app/src/main/java/com/example/core/designsystem/components/AppChip.kt \
        app/src/main/java/com/example/core/designsystem/components/AppBottomSheet.kt
git commit -m "feat(design-system): add Motion tokens + 9 core components (foundation)"

git add app/src/main/java/com/example/core/navigation/Screen.kt \
        app/src/main/java/com/example/MainActivity.kt \
        app/src/main/AndroidManifest.xml \
        app/src/main/java/com/example/core/designsystem/showcase/
git commit -m "feat(design-system): wire showcase screen via mindrest://designsystem deep link"

# 3. Push supaya semua agent punya baseline sama
git push origin main

# 4. (Opsional) catat baseline commit
git log -1 --format='%h %s'   # ← ini BASELINE, simpan untuk referensi review
```

Setelah ini, `git status` bersih. Semua agent mulai dari baseline yang sama.

---

## 🔁 Loop per-tiket (inti eksekusi)

**Satu tiket = satu sesi = satu PR.** Jangan campur tiket di sesi yang sama.

### Tahap 1 — Klaim tiket
1. Baca `README.md` status board → cari tiket `⬜ todo` prioritas terendah (urut wave 1→6).
2. Edit `README.md`: ubah tiket itu jadi `🟡 in-progress`.
3. Commit: `git commit -am "chore: claim ticket <NN> <Screen>"`.
4. Push.

### Tahap 2 — Branch
```bash
git checkout -b ui/migrate-<screen>   # mis: ui/migrate-home
```

### Tahap 3 — Screenshot SEBELUM
```bash
# build + install + buka screen + screenshot manual di emulator
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
# (adb = C:\Users\lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe)
# simpan: .scratch/ui-migration/screenshots/<NN>-before.png
```
**Wajib.** Ini satu-satunya acuan visual parity.

### Tahap 4 — Eksekusi resep (9 langkah di `MIGRATION_GUIDE.md`)
Ikuti persis. Jangan ubah signature composable / ViewModel / Repository. Hanya presentation.

**Delegasi subagent** (kalau agent-nya support, mis. pi-subagents):
- `scout` — kalau area screen asing / kompleks (LifestyleScreen, StatisticsScreen)
- `worker` — implementasi
- `reviewer` (fresh context) — correctness + cleanup sebelum commit

### Tahap 5 — Build + verifikasi
```bash
./gradlew :app:compileDebugKotlin --console=plain   # 0 error, 0 warning baru
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
# screenshot SESUDAH: .scratch/ui-migration/screenshots/<NN>-after.png
# BANDINGKAN dengan SEBELUM — tidak boleh ada regression
```

### Tahap 6 — Review
Jalankan `reviewer` subagent (atau `/code-review` skill) terhadap `git diff baseline..HEAD`.
Fokus: 0 hardcoded dp/sp/hex baru, AppCard/SectionHeader/EmptyState dipakai benar.

### Tahap 7 — Selesai & merge
```bash
# update status board
# (edit README.md: tiket NN → ✅ done)
git add README.md .scratch/ui-migration/screenshots/
git commit -am "refactor(ui): migrate <Screen> to design tokens (ticket <NN>)"
git push origin ui/migrate-<screen>
# buka PR, review, merge ke main
git checkout main && git pull && git branch -d ui/migrate-<screen>
```

### Tahap 8 — Handoff ke sesi berikutnya
- **Clear context / sesi baru.** Jangan lanjut tiket kedua di sesi yang sama.
- State berikutnya: status board di `main` sudah up-to-date → agent berikutnya baca itu.

---

## 🔀 Koordinasi multi-agent (paralel)

**Bisa paralel** karena tiap tiket = 1 file screen independen. Aturan:

1. **Maksimal 1 agent per wave dalam sesi yang sama repo** (konflik status board).
   Atau: tiap agent pakai **branch sendiri + worktree sendiri** → aman paralel penuh.
2. **Tidak ada 2 agent kerjakan tiket yang sama.** Klaim (Tahap 1) = lock.
3. **Status board = single source of truth.** Pull sebelum mulai, push setelah selesai.
   Kalau konflik: rebase, ambil versi paling lengkap.
4. **Merge berurutan** (PR wave 1 dulu, baru wave 2) walau dikerjakan paralel — hindari konflik `README.md`.

### Rekomendasi pola
- **1 agent, sekuensial** (paling aman): Tiket 01 → clear → 02 → clear → ...
- **2-3 agent, per-wave paralel** (cepat): tiap agent ambil 1 tiket dalam wave sama, branch terpisah, merge berurutan.
- **Jangan** >1 wave bersamaan (tiket antar-wave kadang share komponen yang belum di-ekstrak).

---

## 🚦 Quality gates (jangan dilewati)

Tiap PR tiket **harus** lolos semua:

| Gate | Cek | Tolak kalau |
|---|---|---|
| Compile | `compileDebugKotlin` 0 error | ada error |
| Warning | 0 warning baru vs baseline | ada deprecation/unused baru |
| Visual | screenshot before/after identik layout | ada regression |
| Token | `grep -cE '\b[0-9]+\.dp\b' <screen>` turun drastis (sisa hanya ukuran ikon/stroke) | masih banyak literal |
| Hex | `grep -cE 'Color\(0x' <screen>` = 0 (kecuali feature color konstan di Color.kt) | ada hex inline |
| Behavior | tidak ubah signature/VM/repo | ada perubahan logic |
| A11y | tidak ada teks kontras baru yang gagal AA | ada teks low-contrast baru |

---

## 🏁 Definition of done (seluruh migrasi)

Selesai ketika SEMUA terpenuhi:
- [ ] 20 tiket status `✅ done` di `README.md`
- [ ] `grep -rcE '\b[0-9]+\.dp\b' app/src/main/java/com/example/features/` total < 50 (sisa = ukuran ikon/stroke/fixed)
- [ ] `grep -rcE 'Color\(0x' app/src/main/java/com/example/features/` = 0 (semua hex di Color.kt)
- [ ] `./gradlew :app:assembleDebug` sukses
- [ ] `DesignSystemShowcaseScreen` via deep link tampil benar (light + dark)
- [ ] Semua PR merged ke main, `git status` bersih
- [ ] Tag release: `git tag v1.1-ui-foundation && git push --tags`

---

## 🧯 Troubleshooting

| Masalah | Solusi |
|---|---|
| Agent bingung token mana | Arahkan buka `DesignSystemShowcaseScreen` di emulator (`adb shell am start -a android.intent.action.VIEW -d "mindrest://designsystem"`) |
| Butuh komponen belum ada | **Stop.** Buat tiket ekstraksi komponen dulu di `issues/`. Jangan inline. |
| Screen terlalu besar (Lifestyle 1167 LOC) | Boleh ekstrak sub-composable, tapi tetap 1 tiket 1 PR. Jangan over-engineer. |
| Konflik merge di README.md | Rebase, ambil status paling update. |
| Build gagal setelah merge | `git bisect` dari baseline, cari tiket yang break. |
| JAVA_HOME tidak set | `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` |
| adb tidak di PATH | full path: `C:\Users\lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe` |

---

## 📂 Peta file eksekusi

```
.scratch/ui-migration/
├── AGENT_WORKFLOW.md          ← FILE INI (baca pertama tiap sesi)
├── MIGRATION_GUIDE.md         ← resep 9 langkah + mapping table
├── README.md                  ← status board (source of truth progress)
├── PROMPTS.md                 ← copy-paste prompt per sesi
├── generate-tickets.sh        ← regenerator tiket
├── screenshots/               ← before/after per tiket (buat folder ini)
└── issues/
    ├── 00-TEMPLATE.md
    └── 01–20-<screen>.md      ← tiket per screen
```
