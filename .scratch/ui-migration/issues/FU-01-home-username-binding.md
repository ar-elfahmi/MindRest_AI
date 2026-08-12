# FU-01 — HomeScreen: bind username to logged-in user

**Category:** data-wiring (BUKAN UI-migration 20-screen) · **Effort:** S ~1h · **Status:** ⬜ todo
**Branch:** `fix/home-username-binding` (1 tiket = 1 branch = 1 PR)

## Masalah
Header HomeScreen menampilkan nama user **hardcoded** `"Aria Kusuma"`, bukan
nama user yang sedang login.

## Bukti (file:line)
- `app/src/main/java/com/example/features/home/presentation/screen/HomeScreen.kt:197`
  ```kotlin
  HeaderSection(
      userName = "Aria Kusuma",          // ← hardcoded placeholder
      greeting = jakartaGreeting(),
      ...
  )
  ```

## Sumber data yang benar (sudah ada)
- `ProfileViewModel` sudah load `profiles.display_name` dari Supabase
  (`app/.../profile/presentation/viewmodel/ProfileViewModel.kt`, method `load()`).
- `ProfileUiState.profile?.displayName` adalah field canonical.
- Pola pemakaaiannya sudah ada di `ProfileScreen.kt:120`:
  `name = state.profile?.displayName?.takeIf { it.isNotBlank() } ?: "Guest"`.

## Scope kerjaan (presentation + VM wiring)
1. Tambah inject `ProfileViewModel` di `HomeScreen` (atau expose display name via
   `HomeUiState` — pilih salah satu, konsisten dengan arsitektur single-VM-per-screen
   yang sudah dipakai HomeScreen untuk Mood/Sleep/Ikigai).
2. Bind `userName = profileDisplayName` dengan fallback `"Guest"` saat null/blank
   atau user belum login.
3. **Jangan sentuh** `ProfileViewModel.load()` / repository / tabel `profiles`
   (sudah benar).

## Out of scope
- Greeting (sudah dinamis by Jakarta time — done di tiket 01).
- Avatar URL / email (tiket terpisah kalau perlu).

## Acceptance
- [ ] Username berubah sesuai user login (bukan "Aria Kusuma").
- [ ] Fallback "Guest" saat belum login / display_name kosong.
- [ ] `compileDebugKotlin` sukses, no new warnings.
- [ ] Signature publik `HomeScreen(...)` boleh berubah (ini bukan tiket
      presentation-only — kecuali di-bind via VM yang sudah ada).
- [ ] Reviewer subagent.

## Catatan
HomeScreen sudah konsumsi 3 VM paralel (Mood/Sleep/Home). Tambah ProfileViewModel
sebagai VM ke-4, atau pindahkan `displayName` ke `HomeUiState` (repo query di
`HomeViewModel`). Pilih yang lebih ringan — ProfileViewModel sudah ada & tested.
