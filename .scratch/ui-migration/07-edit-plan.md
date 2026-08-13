# 07 Edit Plan — SleepTrackingScreen

**File:** `features/sleep/presentation/screen/SleepTrackingScreen.kt` (181 LOC)
**Start:** dp 8, sp 0, hex 0
**Baseline:** main `605db57` (includes tickets 01-06)

## Policy decisions
- **USE AppCard** (per ticket spec). This screen has a RAW M3 `Card(colors=secondaryContainer)` —
  NOT BaseCard. Pilot "keep BaseCard" rule is about existing BaseCard screens (01-03); it does
  NOT block raw-Card->AppCard migration (that's the design-system standardization itself).
- Variant = Tonal (default). The "Total Sleep Duration" card is a read-only summary.
- NOTE color shift: secondaryContainer -> surface (AppCard Tonal). Inherent to design-system
  standardization; flag to reviewer. (No hex involved — secondaryContainer is already colorScheme.)
- AppScaffold has no containerColor param; topBar here uses custom primaryContainer colors via
  TopAppBar.colors (preserved as-is). containerColor drop not needed (no Scaffold containerColor set).
- Need to ADD designsystem imports (screen has none currently).

## Edits
- **G0 imports:** add LocalSpacing, AppScaffold, screenEdgePadded, AppCard. Add `val spacing`.
- **G1 root:** Scaffold -> AppScaffold (snackbarHost + topBar preserved).
- **G2 Column root:** padding(16) -> screenEdgePadded; spacedBy(24) -> space6.
- **G3 duration card:** Card -> AppCard(modifier=fillMaxWidth, contentPadding=space4);
  inner Column.padding(16) -> drop (AppCard handles). Spacer height(8) -> space2.
- **G4 save button:** contentPadding 16 -> space4; Spacer width(8) -> space2.

## Residuals (2 dp, guide-excluded)
- size(24) CircularProgressIndicator: component dim.
- strokeWidth 2: no stroke token.
