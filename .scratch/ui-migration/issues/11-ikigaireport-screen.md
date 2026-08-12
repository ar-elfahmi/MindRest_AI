# 11 — Migrate IkigaiReportScreen to Design Tokens

**Wave:** 4 · **Effort:** L ~2.5h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/ikigai/presentation/screen/IkigaiReportScreen.kt`
**LOC:** 747 · **Hardcoded dp:** 51 · **Hardcoded sp:** 3 · **Hardcoded hex:** 4

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- Sudah punya Scaffold -> ganti ke `AppScaffold`.
- **10 kartu** ad-hoc -> `AppCard` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient).
- READING MODE (Calm ref) -> tipografi serif penting, generous line height. 10 cards -> AppCard. 4 lingkaran visual (biarkan logic). 4 hex -> colorScheme. Body text panjang -> bodyLarge.

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (3 terpaksa pindah)
- [ ] Color hex -> `colorScheme` / named color (4 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 11 -> ✅), commit `refactor(ui): migrate IkigaiReportScreen to design tokens`.
