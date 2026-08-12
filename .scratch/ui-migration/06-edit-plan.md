# 06 Edit Plan — SleepHubScreen

**File:** `features/sleep/presentation/screen/SleepHubScreen.kt` (382 LOC)
**Start:** dp 23, sp 0, hex 1
**Baseline:** main `6f5089a` (includes tickets 01-05)

## Policy decisions (carry from pilot 01-03)
- **KEEP BaseCard** (not AppCard). Pilot: AppCard shifts border→elevation = visual diff.
  - `Shapes` tokens are `CornerBasedShape`, NOT Dp → BaseCard `radius: Dp` param CANNOT
    take a shape token. Radius literals (20/16) stay as INTRINSIC residuals.
- AppScaffold has no `containerColor` param; default = background = original value → drop (no-op).
- Wildcard imports already present (`designsystem.*` + `components.*`) → no new imports;
  only add `val spacing = LocalSpacing.current` per composable.

## Edits
- **G1 root:** Scaffold → AppScaffold (drop containerColor). Add `val spacing` to body.
- **G2 LazyColumn:** drop `.padding(horizontal=16, vertical=12)`; add `contentPadding = screenEdgeValues()`; `spacedBy(16)` → `space4`.
- **G3 MetricTile row:** `spacedBy(12)` → `componentGap`.
- **G4 Sleep Stage card:** BaseCard padding 16 → space4; Spacer h16→space4; Spacer w16→space4; legend spacedBy 8→space2.
- **G5 Weekly chart card:** BaseCard padding 16 → space4.
- **G6 Recent logs section:** Spacer h8→space2.
- **G7 states:** loading padding 32→space8; error padding 16→space4; empty padding 16→space4.
- **G8 RecentSleepLogCard:** add `val spacing`; Spacer w12→componentGap. (radius 16 + padding 14 stay residual.)
- **G9 StageLegendRow:** add `val spacing`; Spacer w8→space2.

## Residuals (acceptable, guide-excluded)
- BaseCard radius 20×2, 16×1 — intrinsic Dp param, no shape-Dp token.
- BaseCard padding 14×1 — no exact token (space3=12/space4=16).
- size(110) chart, size(10) legend dot — component dims.
- RoundedCornerShape(3) — no shape token (min xs=6).
- strokeWidth 2 — no stroke token.
- 1 hex `Color(0xFFEB845C)` (light sleep legend) — semantic data color, no scheme mapping.
