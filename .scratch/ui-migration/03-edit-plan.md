# 03 — MoodTrackingScreen Edit Plan

**File:** `app/src/main/java/com/example/features/mood/presentation/screen/MoodTrackingScreen.kt`
**Baseline:** f328c5e · **Branch:** ui/migrate-mood

## Token facts (verified)
- Spacing: space1=4, space2=8, space3=12, space4=16, space5=20, space6=24, **space8=32, space12=48, space16=64**, screenHorizontal=20, screenTop=16, componentGap=12
- Shape: xs=6, sm=10, md=16, lg=20 → **12 has NO token** (residual)
- Type: displaySmall=28.sp (but serif/lineHeight — wrong for emoji)

## Edit groups

### G0 — imports + spacing val
- Add: `LocalSpacing`, `AppScaffold`, `screenEdge`
- Add `val spacing = LocalSpacing.current` in MoodTrackingScreen, RecentMoodsSection, MoodHistoryRowItem

### G1 — root (Scaffold → AppScaffold)
- `Scaffold(` → `AppScaffold(` (topBar + snackbarHost params compatible)
- `.padding(16.dp)` → `.screenEdgePadded()` (h=20 standard, v=16 preserves scroll bottom)

### G2 — main content spacers (MoodTrackingScreen)
- `height(24.dp)` → `space6`
- `height(48.dp)` → `space12` ✓ token exists
- `height(4.dp)` → `space1`
- `width(8.dp)` (button) → `space2`
- `contentPadding = PaddingValues(16.dp)` → `space4`
- `height(32.dp)` → `space8` ✓ token exists

### G3 — RecentMoodsSection
- `height(8.dp)` → `space2`
- `padding(24.dp)` → `space6`
- `padding(16.dp)` ×2 → `space4`
- `height(8.dp)` (row gap) → `space2`

### G4 — MoodHistoryRowItem
- `padding(12.dp)` → `space3`
- `width(12.dp)` → `componentGap`

## Residuals (justified, 6 dp + 2 sp)
- `size(64.dp)` emoji circle — component/tap-target dimension (not spacing)
- `size(24.dp)` CircularProgressIndicator — component dimension
- `RoundedCornerShape(12.dp)` — no shape token (xs6/sm10/md16)
- `strokeWidth = 2.dp` ×2 — no stroke token
- `tonalElevation = 1.dp` — no elevation token
- `fontSize = 32.sp` / `28.sp` — decorative emoji (not typography; applying serif displaySmall adds lineHeight/family to emoji = wrong)

## Not done (pilot lessons)
- No AppCard conversion: MoodHistoryRowItem uses `Surface(color=surfaceVariant)` — AppCard has fixed container color (pilot limitation). Keep Surface, tokenize its padding.
- No SectionHeader: titleMedium+fillMaxWidth matches existing; SectionHeader is headlineMedium (visual shift). Keep as-is.

## Expected result
dp 20→6 (-70%), sp 2→2 (emoji), hex 0, Scaffold 0→1
