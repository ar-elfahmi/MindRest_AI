# UI Migration Guide — Mekanisme dp→Token

> **Baca ini dulu sebelum mengerjakan tiket apapun.** Ini adalah resep mekanis
> untuk migrasi 1 screen. Setiap tiket di `issues/` mengikuti resep ini.

Foundation sudah siap (sesi sebelumnya): `Spacing`, `Typography`, `Color`,
`Shape`, `Elevation`, `Motion` tokens + 9 core components
(`AppScaffold`, `AppCard`, `SectionHeader`, `EmptyState`, `LoadingShimmer`,
`ScreenEdge`, `BrandHeader`, `AppChip`, `AppModalBottomSheet`).

## Tujuan tiap screen

> **0 hardcoded `dp` (kecuali ukuran tetap komponen), 0 hardcoded `.sp`, 0 hardcoded `Color(0x...)`.**
> Semua via token / `MaterialTheme`. Visual parity tetap (tidak ada layout regression).

---

## Resep 9 langkah (urut)

### 1. Wrap root di `AppScaffold`
Cari top-level `Scaffold(...)` di screen. Ganti dengan `AppScaffold(...)`.
Kalau screen TIDAK punya Scaffold (audit: `HomeScreen`, `AchievementsScreen`,
`ProfileScreen`, `StatisticsScreen`), bungkus seluruh content di:

```kotlin
AppScaffold { inner ->
    Column(Modifier.padding(inner).screenEdge()) { /* ... */ }
}
```

### 2. Edge padding konsisten
Ganti `Modifier.padding(horizontal = 20.dp)` di root content → `Modifier.screenEdge()`.
Untuk `LazyColumn`/`LazyRow`, pakai `contentPadding = screenEdgeValues()`.

```kotlin
// SEBELUM
Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) { ... }
LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp)) { ... }

// SESUDAH
Column(modifier = Modifier.screenEdgePadded()) { ... }
LazyColumn(contentPadding = screenEdgeValues()) { ... }
```

### 3. Ganti hardcoded `dp` → `LocalSpacing` token
Akses token via `val spacing = LocalSpacing.current` di dalam `@Composable`,
lalu pakai `spacing.spaceN`. **Mapping table** (round to nearest token):

| Hardcoded | Token | Catatan |
|---|---|---|
| `1.dp`, `2.dp` | **keep** | border/stroke hairline |
| `3, 4, 5.dp` | `space1` (4) | |
| `6, 7.dp` | `space2` (8) | round up, bukan down |
| `8, 9, 10.dp` | `space2` (8) | |
| `11, 12, 13.dp` | `space3` (12) | |
| `14, 15, 16.dp` | `space4` (16) | |
| `17, 18, 19.dp` | `space4` (16) | |
| `20.dp` | `screenHorizontal` (20) | edge; atau `space5` |
| `21, 22, 23.dp` | `space6` (24) | |
| `24, 25, 28.dp` | `space6` (24) | |
| `32.dp` | `sectionGap` (32) / `space8` | antar-section |
| `40.dp` | `space10` | |
| `44, 48.dp` | `space12` (48) | |
| `56, 64.dp` | `space16` (64) | |
| `80.dp` | `space20` | |
| `12.dp` (component gap) | `componentGap` | antar-komponen dalam card |

**Kecualikan dari migrasi** (ukuran tetap komponen — keep as literal):
- ukuran ikon (`Modifier.size(24.dp)`)
- ukuran ilustrasi/gambar fixed
- stroke width (`strokeWidth = 2.dp`)
- corner radius yang sudah pakai `LocalShapes`

### 4. Ganti hardcoded `.sp` → `Typography` style
JANGAN pakai `fontSize = 16.sp`. Pakai style:
```kotlin
// SEBELUM
Text("Halo", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
// SESUDAH
Text("Halo", style = MaterialTheme.typography.displaySmall)
```

Mapping style (lihat `Type.kt` untuk detail):
- Judul besar screen → `displaySmall` (serif) atau `headlineMedium`
- Judul section → `headlineMedium` / `titleLarge`
- Body teks → `bodyMedium` / `bodyLarge`
- Label/caption → `labelMedium` / `labelSmall`
- Angka/timer → `NumberXl` / `NumberL` / `NumberM` / `NumberS` (monospace)

### 5. Ganti hardcoded `Color(0x...)` → `MaterialTheme.colorScheme`
```kotlin
// SEBELUM
Text("x", color = Color(0xFF7C72F5))
Box(Modifier.background(Color(0xFF111828)))
// SESUDAH
Text("x", color = MaterialTheme.colorScheme.primary)
Box(Modifier.background(MaterialTheme.colorScheme.surface))
```

Mapping warna umum (lihat `Color.kt` + `Theme.kt`):
- `#7C72F5` / `#5850E7` → `colorScheme.primary`
- `#111828` / `#090C1A` (dark) / `#FFFFFF` (light) → `colorScheme.surface` / `background`
- `#8A9ABB` / `#6E6E80` → `colorScheme.onSurfaceVariant`
- `#E8E6F2` / `#1A1530` → `colorScheme.onBackground`
- `#4ECDC4` / `#EB845C` → `colorScheme.secondary` (atau pakai `DarkAccent`/`LightAccent` named)
- `#E84C5C` → `colorScheme.error`
- Feature colors (`FeatureJourney`, dst.) → pakai named constant dari `Color.kt`

### 6. Ganti ad-hoc card → `AppCard`
```kotlin
// SEBELUM
Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(...)) { ... }
// SESUDAH
AppCard(variant = AppCardVariant.Tonal) { ... }   // atau Outlined / Elevated / Brand
```
Pilih variant: `Tonal` (default konten), `Outlined` (dense list/settings),
`Elevated` (hero), `Brand` (gradient primary — welcome/Ikigai hero).

### 7. Ganti judul section manual → `SectionHeader`
```kotlin
// SEBELUM
Row(...) { Text("Mood minggu ini", fontWeight = Bold); TextButton({..}){ Text("Lihat semua") } }
// SESUDAH
SectionHeader(title = "Mood minggu ini", actionLabel = "Lihat semua", onActionClick = { .. })
```

### 8. Ganti empty list manual → `EmptyState`
```kotlin
// SEBELUM
if (items.isEmpty()) { Column { Icon(...); Text("Belum ada data"); Text("Mulai...") } }
// SESUDAH
if (items.isEmpty()) {
    EmptyState(icon = Icons.Filled.X, title = "Belum ada data", description = "Mulai...")
}
```

### 9. Build + verifikasi visual
```bash
# Pastikan JAVA_HOME set (Windows)
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

# Compile
./gradlew :app:compileDebugKotlin --console=plain

# Build APK + install ke emulator
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
# adb = C:\Users\lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe
```
Buka screen yang dimigrasi, bandingkan dengan screenshot SEBELUM (ambil dulu!).
**Tidak boleh ada layout regression.**

---

## Acceptance criteria (tiap tiket)

- [ ] 0 hardcoded `dp` baru (kecuali ukuran tetap: ikon, ilustrasi, stroke)
- [ ] 0 hardcoded `.sp` — semua via `MaterialTheme.typography`
- [ ] 0 hardcoded `Color(0x...)` — semua via `colorScheme` / named color
- [ ] Root dibungkus `AppScaffold` + `Modifier.screenEdge()` / `screenEdgePadded()`
- [ ] Semua kartu pakai `AppCard`
- [ ] Semua judul section pakai `SectionHeader`
- [ ] Empty state pakai `EmptyState`
- [ ] `./gradlew :app:compileDebugKotlin` sukses, 0 error
- [ ] Visual parity — screenshot before/after, tidak ada regression
- [ ] WCAG AA contrast tetap (tidak ada teks low-contrast baru)

---

## Aturan aman

1. **Satu screen per PR/tiket** — jangan campur screen.
2. **Jangan ubah behavior/logic** — hanya presentation. ViewModel/Repository tidak disentuh.
3. **Jangan hapus komponen yang dipakai screen lain** — kalau butuh komponen baru, ekstrak dulu ke foundation (tiket terpisah).
4. **Ambil screenshot SEBELUM** sebelum mulai edit (referensi visual parity).
5. **Kalau ragu token mana**, cek `DesignSystemShowcaseScreen` (deep link `mindrest://designsystem`).
