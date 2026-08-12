# 02 — Migrate ProfileScreen to Design Tokens

**Wave:** 2 · **Effort:** L ~2h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/profile/presentation/screen/ProfileScreen.kt`
**LOC:** 601 · **Hardcoded dp:** 33 · **Hardcoded sp:** 6 · **Hardcoded hex:** 0

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- **NO Scaffold (audit) -> TAMBAHKAN `AppScaffold`.**
- **4 kartu** ad-hoc -> `AppCard` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient).
- No Scaffold (add one). Profile header = BrandHeader. Settings-ish rows = AppCard(Outlined). Edit-profile belum wired (biarkan).

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (6 terpaksa pindah)
- [ ] Color hex -> `colorScheme` / named color (0 terpaksa pindah)
- [ ] Card -> `AppCard`
- [ ] Section title -> `SectionHeader`
- [ ] Empty state -> `EmptyState` (jika ada)
- [ ] `./gradlew :app:compileDebugKotlin` sukses, 0 error
- [ ] Screenshot SESUDAH + visual parity (no regression)
- [ ] Reviewer subagent

## Acceptance
Lihat 10 kriteria di `MIGRATION_GUIDE.md`.

## Catatan
- **Jangan ubah signature composable / ViewModel / Repository.** Hanya presentation.
- Komponen di `components/` (Charts, Chat, dll.) **jangan migrasi** di tiket ini (tiket terpisah).
- Setelah selesai: update status board (`README.md` 02 -> ✅), commit `refactor(ui): migrate ProfileScreen to design tokens`.
