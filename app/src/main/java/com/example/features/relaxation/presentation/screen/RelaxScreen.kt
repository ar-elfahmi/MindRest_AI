package com.example.features.relaxation.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.relaxation.presentation.state.RelaxCategory
import com.example.features.relaxation.presentation.state.RelaxMediaItem
import com.example.features.relaxation.presentation.viewmodel.RelaxViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * RelaxScreen — T-009 (FR-017).
 *
 * Wire ke [RelaxViewModel] via `viewModel()` + `collectAsState()`.
 * List audio items sekarang diambil dari VM state (bukan hardcoded list).
 *
 * Lifecycle handling:
 * - `DisposableEffect(LocalLifecycleOwner)` pasang [LifecycleEventObserver]
 *   untuk `ON_PAUSE` → panggil `viewModel.onLifecyclePaused()` (pause tanpa
 *   release). [RelaxViewModel.onCleared] yang handle actual `release()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxScreen(
    viewModel: RelaxViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Group items by category
    val groupedItems = uiState.mediaItems.groupBy { it.category }

    // Lifecycle: pause on background, but keep player instance for fast resume.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onLifecyclePaused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Snackbars for events (playback ended / error).
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is com.example.features.relaxation.presentation.viewmodel.RelaxEvent.PlaybackEnded -> {
                    Toast.makeText(context, "Playback selesai", Toast.LENGTH_SHORT).show()
                }
                is com.example.features.relaxation.presentation.viewmodel.RelaxEvent.PlaybackError -> {
                    Toast.makeText(
                        context,
                        "Gagal memutar audio: ${event.codeName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relaxation") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Now-playing bar (visible kalau ada currentItem)
            uiState.currentItemId?.let { currentId ->
                val currentItem = uiState.mediaItems.firstOrNull { it.id == currentId }
                if (currentItem != null) {
                    item(key = "now_playing_$currentId") {
                        NowPlayingBar(
                            uiState = uiState,
                            currentTitle = currentItem.title,
                            onPause = viewModel::onPauseClicked,
                            onStop = viewModel::onStopClicked,
                            onSeek = viewModel::onSeekTo
                        )
                    }
                }
            }

            RelaxCategory.values().forEach { category ->
                val itemsInCategory = groupedItems[category]
                if (!itemsInCategory.isNullOrEmpty()) {
                    item(key = "header_${category.name}") {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(itemsInCategory, key = { it.id }) { mediaItem ->
                        RelaxMediaCard(
                            item = mediaItem,
                            isCurrentlyPlaying = uiState.isPlaying && uiState.currentItemId == mediaItem.id,
                            onPlayClicked = { viewModel.onPlayClicked(mediaItem) },
                            onPauseClicked = { viewModel.onPauseClicked() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingBar(
    uiState: com.example.features.relaxation.presentation.state.RelaxUiState,
    currentTitle: String,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sedang Diputar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onStop, modifier = Modifier.testTag("now_playing_stop")) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Progress bar
            val safeMax = if (uiState.durationMs > 0L) uiState.durationMs else 1L
            LinearProgressIndicator(
                progress = { (uiState.playbackPositionMs.toFloat() / safeMax).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMs(uiState.playbackPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = formatMs(uiState.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RelaxMediaCard(
    item: RelaxMediaItem,
    isCurrentlyPlaying: Boolean,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IMG",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = item.durationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play/Pause toggle (3-state: paused, playing this, playing other)
            IconButton(
                onClick = {
                    if (isCurrentlyPlaying) onPauseClicked() else onPlayClicked()
                },
                modifier = Modifier.testTag("play_btn_${item.id}")
            ) {
                Icon(
                    imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isCurrentlyPlaying) "Pause ${item.title}" else "Play ${item.title}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
