# 15 — Migrate LifestyleScreen to Design Tokens

**Wave:** 5 · **Effort:** XL ~4h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/lifestyle/presentation/screen/LifestyleScreen.kt`
**LOC:** 1167 · **Hardcoded dp:** 78 · **Hardcoded sp:** 7 · **Hardcoded hex:** 32

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- Sudah punya Scaffold -> ganti ke `AppScaffold`.
- **14 kartu** ad-hoc -> `AppCard` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient).
- HEAVIEST. 32 hex (!!) -> map semua ke colorScheme/feature. 14 cards -> AppCard. 14 Column (sangat dense, pertimbangkan split tapi out-of-scope). initialLifestyleGoals + weekDays mock (biarkan). Pertimbangkan ekstrak sub-composable tapi jangan over-engineer.

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (7 terpaksa pindah)
- [ ] Color hex -> `colorScheme` / named color (32 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 15 -> ✅), commit `refactor(ui): migrate LifestyleScreen to design tokens`.
