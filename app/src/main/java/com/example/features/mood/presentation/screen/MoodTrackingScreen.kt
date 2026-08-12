package com.example.features.mood.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.LocalElevation
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.components.AppScaffold
import com.example.core.designsystem.components.screenEdgePadded
import com.example.core.network.dto.MoodLogRow
import com.example.features.mood.presentation.viewmodel.MoodViewModel

private val moodEmojis = listOf(
    1 to "😢", // Terrible
    2 to "🙁", // Bad
    3 to "😐", // Okay
    4 to "🙂", // Good
    5 to "😁"  // Awesome
)

private val moodLabels = mapOf(
    1 to "Terrible",
    2 to "Bad",
    3 to "Okay",
    4 to "Good",
    5 to "Awesome"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MoodViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-load history ketika screen pertama kali dibuka.
    LaunchedEffect(Unit) { viewModel.onLoadHistory() }

    LaunchedEffect(uiState.errorMessage, uiState.isSuccess) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage!!,
                actionLabel = "OK"
            )
            viewModel.onMessageShown()
        } else if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Jurnal mood berhasil disimpan!"
            )
            viewModel.onMessageShown()
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mood Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .screenEdgePadded(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.space6))

            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(spacing.space12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodEmojis.forEach { (moodValue, emoji) ->
                    val isSelected = uiState.selectedMood == moodValue
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.onMoodSelected(moodValue) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 32.sp
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(spacing.space1))
                            Text(
                                text = moodLabels[moodValue].orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.space12))

            Button(
                onClick = { viewModel.onSaveMoodClicked() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(spacing.space4),
                enabled = uiState.selectedMood != null && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(spacing.space2))
                }
                Text(if (uiState.isSaving) "Saving..." else "Save Mood")
            }

            Spacer(modifier = Modifier.height(spacing.space8))

            // ---------------- Recent Moods ----------------
            RecentMoodsSection(uiState)
        }
    }
}

@Composable
private fun RecentMoodsSection(uiState: com.example.features.mood.presentation.state.MoodUiState) {
    val spacing = LocalSpacing.current
    Text(
        text = "Riwayat Mood Terakhir",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(spacing.space2))

    when {
        uiState.isLoadingHistory && uiState.recentMoods.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.space6),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        }

        uiState.historyError != null && uiState.recentMoods.isEmpty() -> {
            Text(
                text = uiState.historyError,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.space4)
            )
        }

        uiState.recentMoods.isEmpty() -> {
            Text(
                text = "Belum ada riwayat mood. Catat mood pertamamu di atas!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.space4)
            )
        }

        else -> {
            uiState.recentMoods.take(10).forEach { row ->
                MoodHistoryRowItem(row)
                Spacer(modifier = Modifier.height(spacing.space2))
            }
        }
    }
}

@Composable
private fun MoodHistoryRowItem(row: MoodLogRow) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = LocalElevation.current.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = moodEmojis.firstOrNull { it.first == row.moodScore.coerceIn(1, 5) }?.second
                    ?: "😐",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(spacing.componentGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moodLabels[row.moodScore.coerceIn(1, 5)] ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatIsoTimestampShort(row.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatIsoTimestampShort(iso: String): String {
    return try {
        val datePart = iso.substringBefore('T')
        val timePart = iso.substringAfter('T').take(5)
        val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val day = parts[2].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull()?.let { months.getOrNull(it - 1) } ?: parts[1]
            "%02d %s · %s".format(day, month, timePart)
        } else iso
    } catch (e: Exception) {
        iso
    }
}
