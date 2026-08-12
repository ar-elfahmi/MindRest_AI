# FU-03 — App theme (dark/light) by Jakarta time

**Category:** cross-cutting (BUKAN UI-migration 20-screen) · **Effort:** M ~2h · **Status:** ⬜ todo
**Branch:** `feat/theme-by-time-jakarta` (1 tiket = 1 branch = 1 PR)

## Masalah
Tema dark/light di-set **hardcoded** saat app start, tidak mengikuti waktu Jakarta:
```kotlin
var isDark by remember { mutableStateOf(true) }   // selalu dark saat launch
```
Sehingga siang hari app tetap dark (tidak nyaman), atau malam hari light (silau).

## Bukti (file:line)
- `app/src/main/java/com/example/MainActivity.kt:71`:
  ```kotlin
  setContent {
      var isDark by remember { mutableStateOf(true) }   // ← hardcoded
      MindRestTheme(darkTheme = isDark) {
          MainApp(isDark = isDark, onToggleDark = { isDark = !isDark })
      }
  }
  ```

## Scope kerjaan
1. Hitung `isDark` awal dari waktu Jakarta:
   - Malam (18:00-04:59) → dark = true
   - Pagi/siang/sore (05:00-17:59) → dark = false
   - Pakai `java.time.LocalTime.now(ZoneId.of("Asia/Jakarta")).hour`.
2. Inisialisasi `isDark` dengan nilai waktu itu (ganti `mutableStateOf(true)`).
3. Pertahankan toggle manual (`onToggleDark`) — user override tetap berlaku
   sampai app di-restart (atau simpan preference, pilih sesuai effort).
4. Konsisten dengan logika greeting di HomeScreen (`jakartaGreeting()`,
   tiket 01) — idealnya ekstrak helper bersama `jakartaIsNight()` supaya
   tidak duplikat timezone logic.

## Catatan scope (PENTING)
- Ini **cross-cutting** — menentukan theme SELURUH app (20 screen), bukan hanya
  HomeScreen. Berbeda dari 20-tiket UI-migration (1 screen per tiket).
- Pertimbangkan: apakah sebaiknya ikut `isSystemInDarkTheme()` (Android system
  dark mode, sudah handle timezone via OS) DARIPADA hardcode Jakarta? Diskusi
  dengan user — system dark mode lebih native & respect user OS setting.
  Kalau user insist "waktu Jakarta", pakai timezone manual seperti di atas.

## Decision point (tanyakan user sebelum mulai)
- **Opsi 1:** Timezone manual Jakarta (18-05 dark) — sesuai permintaan awal.
- **Opsi 2:** `isSystemInDarkTheme()` — ikut setting OS (lebih native,
  user bisa atur via Android "Bedtime"/"Dark theme" schedule).
- **Opsi 3:** Hybrid — default system, tapi fallback Jakarta saat system off.

## Acceptance
- [ ] Tema awal app sesuai waktu Jakarta (atau pilihan user di decision point).
- [ ] Toggle manual tetap berfungsi.
- [ ] Semua 20 screen ter-render benar di kedua mode (spot-check HomeScreen +
      1-2 screen lain, karena theme app-wide).
- [ ] `compileDebugKotlin` sukses.
- [ ] Reviewer subagent.

## Out of scope
- Per-screen theme override (tidak ada requirement).
- Animasi transisi dark↔light (tiket terpisah kalau perlu).
