# 01 — Migrate HomeScreen to Design Tokens (🎯 TRACER BULLET)

**Wave:** 1 (Pilot) · **Effort:** XL ~3-5h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt`
**LOC:** 1021 · **Hardcoded dp:** 76 · **Hardcoded sp:** 5 · **Hardcoded hex:** 11

> **Tiket ini adalah pilot.** Kerjakan PERTAMA. Resep yang terbukti di sini
> menjadi standar untuk 19 tiket lain. Kalau resep di `MIGRATION_GUIDE.md`
> ada yang kurang, perbarui guide setelah tiket ini selesai.

## Blocking
- Foundation siap ✅
- **Mock data (`weeklyScores = listOf(62, 68, ...)` L205) TIDAK disentuh** —
  itu tiket wiring backend terpisah, bukan UI migration. Biarkan apa adanya.

## Spesifik screen ini
- **TIDAK punya Scaffold** (audit: `scaffold=0`) → harus DITAMBAHKAN `AppScaffold`.
- **16 kartu** ad-hoc → semua migrasi ke `AppCard` (lihat mapping variant di bawah).
- Punya header sapaan "Halo, ..." → kandidat `BrandHeader` (serif displaySmall).
- Punya beberapa section ("Mood minggu ini", "Jurnal terbaru", dst.) → `SectionHeader`.
- Punya `weeklyScores` chart-ish → biarkan logic, hanya ganti styling.
- Root saat ini: kemungkinan `Column(Modifier.padding(horizontal = 20.dp))` → `screenEdgePadded()`.
- 11 hardcoded `Color(0x...)` → cek apakah gradient/dekoratif atau semantic.

## Rencana eksekusi detail

### Step 1 — Screenshot SEBELUM
Buka app di emulator (sudah login), navigasi ke Home, screenshot penuh (scroll).

### Step 2 — Struktur root
```kotlin
// SEBELUM (perkiraan)
Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
    // header
    // 16 kartu
}

// SESUDAH
AppScaffold { inner ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .screenEdgePadded(),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
    ) {
        BrandHeader(title = "Halo, $name", subtitle = "...", trailingIcon = ..., onTrailingClick = ...)
        // sections
    }
}
```
Catatan: pakai `spacing.sectionGap` (32dp) antar section, `spacing.componentGap` (12dp) antar kartu dalam section sama.

### Step 3 — Mapping 16 kartu → AppCard variant
Identifikasi tiap kartu, pilih variant:
| Kartu (perkiraan dari nama) | Variant | Alasan |
|---|---|---|
| Greeting / welcome card | `Brand` | gradient primary, hero |
| Mood today quick-action | `Elevated` | primary CTA |
| Weekly mood chart | `Tonal` | konten data |
| Journal preview | `Tonal` | konten |
| Sleep summary | `Tonal` | konten |
| Relaxation shortcut | `Tonal` / `Outlined` | |
| Ikigai entry | `Brand` / `Elevated` | CTA |
| Quick stats | `Outlined` | dense |
| ... (lanjutkan untuk semua 16) | | |

### Step 4 — Section headers
Setiap group kartu dengan judul → bungkus:
```kotlin
SectionHeader(title = "Mood minggu ini", actionLabel = "Lihat semua", onActionClick = { onNavigateToMood() })
Column(verticalArrangement = Arrangement.spacedBy(spacing.componentGap)) { /* kartu */ }
```

### Step 5 — Token migration (dp → spacing)
Ikuti mapping table di `MIGRATION_GUIDE.md`. Fokus area:
- `padding(horizontal = 20.dp)` → hapus (sudah di `screenEdge`)
- `padding(vertical = 16.dp)` → hapus (sudah di `screenEdgePadded`)
- `Spacer(Modifier.height(32.dp))` antar section → `Arrangement.spacedBy(spacing.sectionGap)` di parent
- `Spacer(Modifier.height(12.dp))` antar kartu → `Arrangement.spacedBy(spacing.componentGap)`
- `padding(20.dp)` di dalam kartu → hapus (AppCard sudah pakai `cardPadding`)
- ukuran ikon `Modifier.size(24.dp)` → **keep**

### Step 6 — Tipografi & warna
- `fontSize = 28.sp, fontWeight = SemiBold` di header → `style = displaySmall` (pakai BrandHeader)
- `Color(0xFF7C72F5)` → `colorScheme.primary`
- 11 hex lain → cek satu-satu, map ke colorScheme

### Step 7 — Build + verifikasi
```bash
./gradlew :app:compileDebugKotlin --console=plain   # 0 error
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Buka Home, screenshot SESUDAH, bandingkan. **Layout harus identik, hanya lebih konsisten.**

### Step 8 — Reviewer
Jalankan subagent `reviewer` untuk correctness + cleanup sebelum commit.

## Acceptance (10 kriteria di MIGRATION_GUIDE.md)
- [ ] 0 hardcoded dp baru (kecuali ikon/stroke/fixed)
- [ ] 0 hardcoded `.sp`
- [ ] 0 hardcoded `Color(0x...)`
- [ ] Root: `AppScaffold` + `screenEdgePadded()`
- [ ] 16 kartu → `AppCard`
- [ ] Section titles → `SectionHeader`
- [ ] (tidak ada empty state di Home — skip kriteria itu)
- [ ] compileDebugKotlin: 0 error
- [ ] Visual parity (before/after screenshot)
- [ ] WCAG AA preserved

## Catatan / risiko
- **Jangan ubah signature composable** — `onNavigateToJournal` dll. tetap, hanya body yang berubah.
- Kalau `weeklyScores` chart pakai `Charts.kt` component, **jangan migrasi `Charts.kt`** di tiket ini (tiket terpisah jika perlu).
- Header greeting mungkin sudah pakai font serif — konfirmasi `BrandHeader` cocok, atau pertahankan inline kalau layoutnya unik.
- Setelah selesai: **update `MIGRATION_GUIDE.md`** jika ada lesson learned, lalu set status `✅ done`.

## Setelah selesai
- Update status board di `README.md` (01 → ✅).
- Commit: `refactor(ui): migrate HomeScreen to design tokens`.
- Laporkan ke user: resep terbukti, lanjut wave 2.
