package com.example.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * MindRest's standard screen scaffold.
 *
 * Thin wrapper over Material 3 [Scaffold] that:
 *  - enforces consistent [contentWindowInsets] (status bar / nav bar handling).
 *
 * Screens consume the inner padding from [content] and then apply
 * [Modifier.screenEdge] for horizontal breathing room.
 *
 * @param contentWindowInsets defaults to [ScaffoldDefaults.contentWindowInsets]
 *        (status + navigation bars). Pass [WindowInsets] to override, e.g. for
 *        edge-to-edge hero screens.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (innerPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

// Usage reminder (see KDoc above):
//   AppScaffold { inner -> Column(Modifier.padding(inner).screenEdge()) { ... } }
