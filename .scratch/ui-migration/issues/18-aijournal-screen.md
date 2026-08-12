# 18 — Migrate AiJournalScreen to Design Tokens

**Wave:** 6 · **Effort:** M ~1.5h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/journal/presentation/screen/AiJournalScreen.kt`
**LOC:** 422 · **Hardcoded dp:** 42 · **Hardcoded sp:** 0 · **Hardcoded hex:** 0

> Ikuti resep 9 langkah di [`../MIGRATION_GUIDE.md`](../MIGRATION_GUIDE.md).
> Pilot (`01-home-screen.md`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- Sudah punya Scaffold -> ganti ke `AppScaffold`.
- **Lazy list** -> `contentPadding = screenEdgeValues()`.
- Chat hardcoded messages (mock, belum panggil Gemini - biarkan). Lazy list chat bubbles -> AppCard(Outlined) untuk user/bot. Input bar -> screenEdge bottom. SendButton sudah tokenized.

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: `AppScaffold` + `screenEdge()` / `screenEdgePadded()`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> `MaterialTheme.typography` (0 terpaksa pindah)
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
- Setelah selesai: update status board (`README.md` 18 -> ✅), commit `refactor(ui): migrate AiJournalScreen to design tokens`.
