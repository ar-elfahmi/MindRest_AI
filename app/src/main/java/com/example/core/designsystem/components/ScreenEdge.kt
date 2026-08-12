package com.example.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.example.core.designsystem.LocalSpacing

/**
 * Single source of truth for screen-edge content padding.
 *
 * Replaces the ~120 scattered `padding(horizontal = 20.dp)` calls across the
 * 21 screens (audit found `screenHorizontal` token used only 1×).
 *
 * Three flavours so callers pick the right contract:
 *  - [screenEdge]            → horizontal only (for scrollable roots)
 *  - [screenEdgePadded]      → horizontal + screenTop + screenBottom
 *  - [screenEdgeValues]      → returns PaddingValues for Scaffold contentPadding
 */
fun Modifier.screenEdge(): Modifier = composed {
    val spacing = LocalSpacing.current
    padding(horizontal = spacing.screenHorizontal)
}

/** Horizontal edge padding + standard top & bottom breathing room. */
fun Modifier.screenEdgePadded(): Modifier = composed {
    val spacing = LocalSpacing.current
    padding(
        horizontal = spacing.screenHorizontal,
        vertical = spacing.screenTop,
    )
}

/** PaddingValues flavour for Scaffold/LazyColumn contentPadding. */
@Composable
@ReadOnlyComposable
fun screenEdgeValues(): PaddingValues {
    val spacing = LocalSpacing.current
    return PaddingValues(
        horizontal = spacing.screenHorizontal,
        vertical = spacing.screenTop,
    )
}
