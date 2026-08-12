# T-009b — UX Integration RelaxScreen + AdvancedRelaxationScreen

## 🎯 Goal

Setelah T-009 (FR-017 audio player via RelaxScreen) dan orchestrator fix (NavHost route), saat ini `AdvancedRelaxationScreen` (breathing + movement + audio mixer) menjadi **orphan** — tidak reachable dari UI manapun.

T-009b mengembalikan akses ke `AdvancedRelaxationScreen` lewat tombol "Mode Lanjutan" di `RelaxScreen`.

**Bukan goal**: refactor salah satu screen atau menambah fitur baru.

---

## 📖 Context

### State existing

| Komponen | Status |
|---|---|
| `RelaxScreen` (T-009) | ✅ Now-Playing bar + ExoPlayer + 3 tracks + lifecycle handling |
| `AdvancedRelaxationScreen` (existing) | ✅ Gerak (movement) + Napas (breathing) + Suara (audio mixer) |
| `Screen.Relaxation.route` (NavHost) | ✅ Routing ke `RelaxScreen` (orchestrator fix `3001c72`) |
| `Screen.AdvancedRelaxation.route` (NavHost) | ✅ Ada (orchestrator fix), tapi belum dipanggil dari mana-mana |
| Akses dari UI | ❌ Tidak ada — `AdvancedRelaxationScreen` orphan |

### Design decision (sudah dibuat orchestrator)

**`RelaxScreen` jadi primary** (entry tab Relaxation):
- User tap tab Relaxation → langsung lihat audio player + 3 tracks ready to play
- Pemula yang cuma mau dengerin suara relaksasi → OK tanpa navigate lagi

**`AdvancedRelaxationScreen` jadi secondary**:
- Akses lewat tombol/icon "Mode Lanjutan" atau "Lainnya" di `RelaxScreen`
- Untuk user yang mau guided breathing, movement, atau audio mixer

---

## 📚 Read First (WAJIB)

1. `app/src/main/java/com/example/features/relaxation/presentation/screen/RelaxScreen.kt` (293 lines) — titik integrasi
2. `app/src/main/java/com/example/features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt` (337 lines) — destination screen
3. `app/src/main/java/com/example/MainActivity.kt` lines ~155-170 (Relaxation Tab + AdvancedRelaxationScreen composable) — NavHost
4. `app/src/main/java/com/example/core/navigation/Screen.kt` (cek `Screen.AdvancedRelaxation`)

**Wajib Context7** (kalau butuh):
- `resolve-library-id "jetpack compose"` → query "navigation Compose callback navigate route"

---

## ✅ Scope

### Wajib diubah

1. **RelaxScreen tambah callback parameter:**
   - Signature baru: `fun RelaxScreen(onNavigateToAdvanced: () -> Unit = {}, viewModel: RelaxViewModel = viewModel())`
   - Tambahkan `IconButton` atau `TextButton` di TopAppBar / header dengan label "Lainnya" atau icon `Icons.Filled.MoreVert`
   - OnClick → `onNavigateToAdvanced()`
   - Style: secondary (outlined button), bukan primary (filled), supaya user paham itu menu sekunder

2. **MainActivity pass callback ke RelaxScreen:**
   ```kotlin
   composable(Screen.Relaxation.route) {
       RelaxScreen(
           onNavigateToAdvanced = {
               navController.navigate(Screen.AdvancedRelaxation.route)
           }
       )
   }
   ```

3. **CHANGELOG update:**
   - Master Status FR-017 catatan tetap 🟢 (tidak berubah) — tambah note "Mode Lanjutan accessible via T-009b"
   - Timeline entry untuk T-009b
   - Update "Next blocker" line — runtime E2E phase atau T-006

### Optional (kalau waktu memungkinkan, NICE-TO-HAVE)

4. **AppBar back navigation di AdvancedRelaxationScreen** — pastikan tombol back navigate ke `Screen.Relaxation` (bukan pop ke root). Cek pattern `popBackStack()` atau explicit `navigate(Screen.Relaxation.route) { popUpTo(...) }`.

5. **Empty state hint di AdvancedRelaxationScreen** — kalau Gerak/Napas/Suara butuh data (mis. SleepInsight recommendation), tampilkan empty state.

---

## ❌ DON'T Touch

- ❌ File di luar `features/relaxation/` + `MainActivity.kt` + `Screen.kt` + `CHANGELOG.md`
- ❌ `RelaxViewModel.kt` (logic sudah lengkap dari T-009)
- ❌ `RelaxUiState.kt` (tidak perlu field baru untuk tombol navigation)
- ❌ ExoPlayer / media3 deps (T-009 sudah selesai)
- ❌ Database / migrations / Edge Functions (T-009b purely UI)
- ❌ File di `features/{authentication,profile,home,ikigai,journal,lifestyle,sleep,mood,statistics,achievements,notification,settings,reminder}/`
- ❌ Jangan rename `Screen.AdvancedRelaxation.route` (orchestrator fix `3001c72` sudah commit `"relaxation/advanced"`)
- ❌ Jangan tambah rel dependency baru (cukup IconButton atau TextButton dari Material3)

---

## 🛠️ Implementation Steps

### Step 1: Diagnosa (2 menit)

```bash
# Cek current RelaxScreen TopAppBar / header
grep -E "(TopAppBar|IconButton|TextButton|@Composable)" features/relaxation/presentation/screen/RelaxScreen.kt | head -10
# Cek AdvancedRelaxationScreen onNavigateBack pattern
grep -E "onNavigateBack" features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt
```

### Step 2: Edit RelaxScreen signature

Buka `RelaxScreen.kt`. Cari function definition:
```kotlin
fun RelaxScreen(
    viewModel: RelaxViewModel = viewModel()
)
```

Tambah callback parameter di awal:
```kotlin
fun RelaxScreen(
    onNavigateToAdvanced: () -> Unit = {},
    viewModel: RelaxViewModel = viewModel()
)
```

**Catatan penting**: callback HARUS parameter pertama (default value), supaya `RelaxScreen()` existing call tanpa args tetap work (untuk preview/test).

### Step 3: Tambahkan tombol di header

Cari composable TopAppBar atau header di dalam RelaxScreen. Tambah:

```kotlin
IconButton(onClick = onNavigateToAdvanced) {
    Icon(
        imageVector = Icons.Filled.MoreVert,
        contentDescription = "Mode Lanjutan",
    )
}
```

Atau kalau lebih suka text button:
```kotlin
TextButton(onClick = onNavigateToAdvanced) {
    Text("Lainnya")
}
```

### Step 4: Edit MainActivity

Cari `composable(Screen.Relaxation.route)` block. Update:
```kotlin
composable(Screen.Relaxation.route) {
    RelaxScreen(
        onNavigateToAdvanced = {
            navController.navigate(Screen.AdvancedRelaxation.route)
        }
    )
}
```

### Step 5: Verify AdvancedRelaxationScreen back navigation (opsional)

Kalau `AdvancedRelaxationScreen.onNavigateBack` saat ini pakai `navController.popBackStack()` (generic), biasanya OK. Kalau mau explicit:
```kotlin
// Di MainActivity:
composable(Screen.AdvancedRelaxation.route) {
    AdvancedRelaxationScreen(
        onNavigateBack = {
            navController.navigate(Screen.Relaxation.route) {
                popUpTo(Screen.Relaxation.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    )
}
```

Tapi ini opsional — kalau existing `popBackStack()` works, jangan diubah.

### Step 6: Build verify

```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew :app:assembleDebug --no-daemon
```

### Step 7: Update CHANGELOG

```markdown
### [YYYY-MM-DD HH:MM] T-009b | agent: <id> | FR: FR-017 (UX polish)
**Goal**: Restore akses ke AdvancedRelaxationScreen (breathing + movement + audio mixer) via tombol "Lainnya" / IconButton MoreVert di RelaxScreen header.
**Files changed**: 2 file:
- `features/relaxation/presentation/screen/RelaxScreen.kt` (+10 ins) — tambah `onNavigateToAdvanced: () -> Unit = {}` callback parameter + IconButton MoreVert di TopAppBar
- `MainActivity.kt` (+4 ins) — pass `onNavigateToAdvanced = { navController.navigate(Screen.AdvancedRelaxation.route) }` ke RelaxScreen
**Acceptance**:
- [✅] Tombol accessible dari RelaxScreen header
- [✅] Tap → navigate ke AdvancedRelaxationScreen
- [✅] Back dari AdvancedRelaxationScreen → kembali ke RelaxScreen
- [✅] Build sukses (`./gradlew :app:assembleDebug`)
- [⚠️] Runtime navigation test deferred (butuh APK install + manual tap)
**Build**: ✅ sukses | ❌ <error>
**Risks/Notes**: ...
**Next blocker**: Runtime E2E test phase (FR-009/011/014) atau T-006 Statistics rewrite
```

---

## 🧪 Acceptance Criteria

- [ ] `RelaxScreen` signature punya `onNavigateToAdvanced: () -> Unit = {}` parameter
- [ ] Header `RelaxScreen` ada IconButton/TextButton "Lainnya" atau "Mode Lanjutan"
- [ ] Tap tombol → `onNavigateToAdvanced()` dipanggil
- [ ] `MainActivity` pass `onNavigateToAdvanced = { navController.navigate(Screen.AdvancedRelaxation.route) }`
- [ ] `Screen.AdvancedRelaxation.route` di NavHost pointing ke `AdvancedRelaxationScreen`
- [ ] Back dari `AdvancedRelaxationScreen` kembali ke `Screen.Relaxation` (existing `popBackStack()` biasanya cukup)
- [ ] Build sukses
- [ ] CHANGELOG entry + Master Status note updated

---

## ⚠️ Gotchas

1. **Parameter ordering** — `onNavigateToAdvanced` HARUS sebelum `viewModel: RelaxViewModel = viewModel()` karena Kotlin default args hanya bekerja kalau param dengan default ada di akhir atau semua param setelahnya juga default. Kalau taruh di akhir, existing call `RelaxScreen()` masih work, tapi `RelaxScreen(viewModel = ...)` saja akan error kalau ada positional call `RelaxScreen(SomeVM)`.
2. **Icon import** — kalau pakai `Icons.Filled.MoreVert`, import `androidx.compose.material.icons.filled.MoreVert` (cek versi Material Icons library di project — mungkin Extended Icons).
3. **TopAppBar presence** — cek apakah RelaxScreen sudah ada TopAppBar. Kalau belum, cukup taruh IconButton di header biasa atau di LazyColumn item pertama.
4. **Accessibility** — `contentDescription` WAJIB untuk IconButton (supaya TalkBack bisa baca).
5. **NavHost route conflict** — `Screen.AdvancedRelaxation.route = "relaxation/advanced"`. Pastikan tidak conflict dengan route lain. Cek `Screen.kt`.
6. **popBackStack vs navigate** — `popBackStack()` menghapus AdvancedRelaxationScreen dari back stack. `navigate(...)` menambah route baru di stack. Untuk "back to Relaxation", `popBackStack()` sudah benar.

---

## 📤 Output

Buat **1 commit atomic**:
- `feat(relax): add Mode Lanjutan button to access AdvancedRelaxationScreen (T-009b)`

Plus docs commit:
- `docs(changelog): T-009b entry`

Total **2 commit**.

---

## 🛑 Stop Conditions

Tulis di CHANGELOG dengan `❌` lalu **jangan commit code**, kalau:
- Build gagal dan error tidak bisa fix dalam 10 menit
- TopAppBar / IconButton pattern konflik dengan theme project
- NavHost route `Screen.AdvancedRelaxation.route` sudah dipakai untuk hal lain (unlikely, cek dulu di Step 1)

---

## 🎯 Definition of Done

- ✅ 1 commit code + 1 commit docs
- ✅ `RelaxScreen` punya tombol "Lainnya" / IconButton
- ✅ Tap tombol → navigate ke `AdvancedRelaxationScreen`
- ✅ Back dari `AdvancedRelaxationScreen` → kembali ke `RelaxScreen`
- ✅ Build sukses
- ✅ CHANGELOG entry + Master Status updated
- ✅ Tidak edit file di luar Scope

---

## 📞 Reporting

```
T-009b DONE
- Files: <list>
- Commits: <hash1>, <hash2>
- Build: ✅/❌
- Notes: <any>
```
