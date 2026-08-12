package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme

/**
 * Brand-styled modal bottom sheet.
 *
 * Wraps Material 3 [ModalBottomSheet] with MindRest shape (xl = 24dp top
 * corners), surface container colour, and standard inner padding. Provides a
 * stable seam so sheets like `DailyCheckInBottomSheet` share one look.
 *
 * Caller controls visibility:
 * ```
 * if (showSheet) {
 *     AppModalBottomSheet(onDismiss = { showSheet = false }) {
 *         // sheet content
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shapes = LocalShapes.current
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = shapes.xl,
        containerColor = cs.surface,
        contentColor = cs.onSurface,
        tonalElevation = androidx.compose.ui.unit.Dp(0f),
        scrimColor = cs.onSurface.copy(alpha = 0.5f),
        dragHandle = null,
        content = content,
    )
}

/** Returns a [SheetState] pre-set to expanded, with skip-partially-expanded off. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAppSheetState(): SheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
)

@Preview(name = "AppModalBottomSheet (collapsed)", showBackground = true)
@Composable
private fun AppBottomSheetPreview() {
    // Previews of modal sheets render as an empty surface in static preview;
    // the showcase screen exercises the live sheet interactively.
    MindRestTheme {
        androidx.compose.foundation.layout.Box {}
    }
}
