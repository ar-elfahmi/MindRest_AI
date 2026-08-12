# Tiket Template — Migrasi Screen ke Design Token

> Salin format ini untuk tiket screen baru. Isi field di `[...]`.

```markdown
# [NN] — Migrate <ScreenName> to Design Tokens

**Wave:** [1-6] · **Effort:** [S/M/L/XL] ~[X]h · **Status:** ⬜ todo
**File:** `app/src/main/java/com/example/features/<feature>/presentation/screen/<Screen>.kt`
**LOC:** [N] · **Hardcoded dp:** [N] · **Hardcoded sp:** [N] · **Hardcoded hex:** [N]

## Blocking
- Foundation siap ✅
- [tulis komponen baru yang perlu diekstrak dulu, atau "none"]

## Spesifik screen ini
- [poin-poin unik: punya Scaffold / tidak, jumlah card, lazy list, mock data, dst.]

## Checklist (ikuti MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Wrap root di AppScaffold (+ screenEdge)
- [ ] Migrasi dp → spacing token
- [ ] Migrasi sp → typography
- [ ] Migrasi Color hex → colorScheme
- [ ] Card → AppCard (variant: ...)
- [ ] Section title → SectionHeader
- [ ] Empty state → EmptyState (jika ada)
- [ ] compileDebugKotlin sukses
- [ ] Screenshot SESUDAH + bandingkan (visual parity)
- [ ] Reviewer subagent

## Acceptance
[Lihat 10 kriteria di MIGRATION_GUIDE.md]

## Catatan
[hal-hal khusus, risiko, keputusan desain]
```
