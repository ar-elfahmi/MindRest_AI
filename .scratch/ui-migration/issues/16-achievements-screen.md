# 16 — Migrate AchievementsScreen to Design Tokens

**Wave:** 5 · **Effort:** L ~2.5h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/achievements/presentation/screen/AchievementsScreen.kt`
**LOC:** 768 · **Hardcoded dp:** 55 · **Hardcoded sp:** 11 · **Hardcoded hex:** 0

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- **NO Scaffold (audit) -> TAMBAHKAN `AppScaffold`.**
- **7 kartu** ad-hoc -> `AppCard` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient).- **Lazy list** -> `contentPadding = screenEdgeValues()`.
- NO Scaffold (add). 11 sp -> typography. sampleAchievements + steps mock (biarkan). 7 cards -> AppCard. Lazy list.

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (11 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 16 -> ✅), commit `refactor(ui): migrate AchievementsScreen to design tokens`.
