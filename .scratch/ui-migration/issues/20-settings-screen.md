# 20 — Migrate SettingsScreen to Design Tokens

**Wave:** 6 · **Effort:** M ~1h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/settings/presentation/screen/SettingsScreen.kt`
**LOC:** 266 · **Hardcoded dp:** 12 · **Hardcoded sp:** 3 · **Hardcoded hex:** 1

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- **NO Scaffold (audit) -> TAMBAHKAN `AppScaffold`.**
- **1 kartu** ad-hoc -> `AppCard` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient).
- NO Scaffold (add). 3 sp -> typography. 1 hex -> colorScheme. Settings rows -> AppCard(Outlined). Edit profile belum wired (biarkan).

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (3 terpaksa pindah)
- [ ] Color hex -> `colorScheme` / named color (1 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 20 -> ✅), commit `refactor(ui): migrate SettingsScreen to design tokens`.
