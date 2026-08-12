# 09 — Migrate AdvancedRelaxationScreen to Design Tokens

**Wave:** 3 · **Effort:** M ~1.5h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt`
**LOC:** 336 · **Hardcoded dp:** 15 · **Hardcoded sp:** 0 · **Hardcoded hex:** 8

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- Sudah punya Scaffold -> ganti ke `AppScaffold`.

- 8 hex (!!) -> map semua ke colorScheme/feature colors. Modes (Gerak/Napas/Suara) -> AppChipGroup. Video placeholder (biarkan).

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (0 terpaksa pindah)
- [ ] Color hex -> `colorScheme` / named color (8 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 09 -> ✅), commit `refactor(ui): migrate AdvancedRelaxationScreen to design tokens`.
