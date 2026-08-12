# 04 — JournalHistoryScreen Edit Plan

**File:** `app/src/main/java/com/example/features/journal/presentation/screen/JournalHistoryScreen.kt`
**Baseline:** main (85dd58d, includes tickets 01-03) · **Branch:** ui/migrate-journal-history

## Token facts (verified)
- Spacing: space1=4, space2=8, space3=12, space4=16, space6=24, space8=32, space12=48, space16=64, space20=80, screenHorizontal=20, componentGap=12
- Elevation: LocalElevation.current.xs (1.dp), sm/md/lg exist
- AppCard(modifier, onClick?, variant=Tonal, contentPadding?, content) — supports surface color via Elevated variant
- EmptyState(icon: ImageVector, title: String, description?, primaryAction?)
- SectionHeader(title, subtitle?, actionLabel?, actionIcon?, onActionClick?)
- screenEdgeValues() returns PaddingValues(h=20, v=16)

## Edit groups

### G0 — imports + spacing val
- Add: LocalSpacing, LocalElevation, AppCard, AppCardVariant, EmptyState, SectionHeader, screenEdge, screenEdgeValues, Icons.Filled.SentimentSatisfied (or Notebook) for empty state
- Add `val spacing = LocalSpacing.current` in JournalHistoryScreen + JournalEntryCard

### G1 — root (Scaffold → AppScaffold)
- `Scaffold(` → `AppScaffold(`
- LazyColumn contentPadding `PaddingValues(bottom=80.dp)` → `PaddingValues(bottom = spacing.space20)` (FAB clearance)

### G2 — WeeklyMoodTimeline + section title (main item)
- `.padding(horizontal=16.dp, vertical=16.dp)` → `.screenEdgePadded()`
- `Spacer height(16.dp)` → `space4`
- "Sesi Sebelumnya" Text → `SectionHeader(title="Sesi Sebelumnya", modifier=screenEdge())`
- error/empty Box `.padding(32.dp)` ×3 → `space8`

### G3 — JournalEntryCard list items
- `.padding(horizontal=16.dp, vertical=8.dp)` → `.padding(horizontal = spacing.screenHorizontal, vertical = spacing.space2)` (or screenEdge)

### G4 — WeeklyMoodTimeline (private composable)
- `.padding(bottom=12.dp)` → `space3`
- `.padding(vertical=8.dp)` → `space2`
- `Spacer height(8.dp)` → `space2` (×2)
- `Spacer height(8.dp)` (no-data) → `space2`
- `size(36.dp)` → keep (component dim)
- `width=2.dp` border → keep (no stroke token) OR LocalElevation? No, it's a border width. keep residual.
- strokeWidth 2.dp → keep residual

### G5 — JournalEntryCard body (Card → AppCard)
- `Card(shape=RoundedCornerShape(16), containerColor=surface, elevation=2)` → `AppCard(variant=Elevated, onClick, contentPadding=space4)`
- `.padding(16.dp)` → drop (AppCard contentPadding handles it)
- `RoundedCornerShape(12.dp)` badge → keep (no shape token)
- `padding(horizontal=10, vertical=4)` badge → space? 10 has no token. Use space2(8) horizontal — minor 2dp shift, OR keep residual. Keep residual (badge micro-sizing).
- `Spacer width(4.dp)` ×2 → `space1`
- `Spacer height(12.dp)` ×2 → `space3`
- `Spacer height(4.dp)` → `space1`
- `size(16.dp)` arrow icon → keep (component dim)

### G6 — Empty state upgrade
- `Box { Text("Belum ada entri jurnal...") }` → `EmptyState(icon=Icons.Filled.Notebook, title="Belum ada entri jurnal", description="Mulai sesi pertama kamu!")` with modifier padding space8

## Residuals (justified)
- size(36.dp) timeline emoji box, size(24.dp) spinner, size(16.dp) arrow: component dims
- strokeWidth=2.dp ×2: no stroke token
- RoundedCornerShape(12.dp) badge: no shape token
- width=2.dp border (today highlight): no stroke token
- padding(10,4) badge: micro-sizing, no exact token

## Expected result
dp 33→~10, sp 0→0, hex 0, Scaffold 0→1
