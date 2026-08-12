package com.example.features.journal.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.AppCard
import com.example.core.designsystem.components.AppCardVariant
import com.example.core.designsystem.components.AppScaffold
import com.example.core.designsystem.components.EmptyState
import com.example.core.designsystem.components.SectionHeader
import com.example.core.designsystem.components.screenEdge
import com.example.core.designsystem.components.screenEdgePadded
import com.example.core.designsystem.components.screenEdgeValues
import com.example.core.network.dto.JournalEntryRow
import com.example.features.journal.presentation.viewmodel.JournalViewModel
import com.example.features.mood.presentation.viewmodel.MoodViewModel
import java.time.LocalDate
import java.time.DayOfWeek

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalHistoryScreen(
    onNavigateBack: () -> Unit,
    onStartNewSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel(),
    // TASK 2C: sumber data riil untuk WeeklyMoodTimeline.
    moodViewModel: MoodViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val moodState by moodViewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current

    // Auto-load history + mood mingguan setiap kali screen dibuka.
    LaunchedEffect(Unit) {
        viewModel.onLoadHistory()
        moodViewModel.onLoadWeeklyScores()
    }

    val entries = state.recentEntries.map { it.toJournalEntryData() }

    AppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Riwayat Jurnal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartNewSessionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Start AI Reflection") },
                text = { Text("Mulai Refleksi", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = spacing.space20)
        ) {
            item {
                WeeklyMoodTimeline(
                    scores = moodState.weeklyMoodScores,
                    isLoading = moodState.isLoadingWeekly,
                    todayIndex = todayDayIndex(),
                    modifier = Modifier.screenEdgePadded()
                )
                Spacer(modifier = Modifier.height(spacing.space4))
                SectionHeader(
                    title = "Sesi Sebelumnya",
                    modifier = Modifier.screenEdge()
                )
            }
            
            when {
                state.isLoadingHistory && entries.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.space8),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                state.historyError != null && entries.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.space8),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.historyError!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                entries.isEmpty() -> item {
                    EmptyState(
                        icon = Icons.Filled.Edit,
                        title = "Belum ada entri jurnal",
                        description = "Mulai sesi pertama kamu!",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> items(entries) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { /* TODO: Navigate to journal detail */ },
                        modifier = Modifier
                            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.space2)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Konversi row Supabase ke model UI — format tanggal sederhana untuk MVP. */
private fun JournalEntryRow.toJournalEntryData(): JournalEntryData {
    val displayDate = formatIsoTimestamp(createdAt)
    val snippet = content.take(140).let { if (content.length > 140) "$it…" else it }
    return JournalEntryData(
        date = displayDate,
        moodEmoji = "📝",
        moodText = "Refleksi",
        summary = snippet.ifBlank { "(kosong)" }
    )
}

private fun formatIsoTimestamp(iso: String): String {
    // Format PostgREST default: "2025-08-09T14:23:11.123456+00:00" atau "2025-08-09T14:23:11Z"
    // Kita potong ke "09 Aug 2025 · 14:23" — cukup informatif tanpa library tambahan.
    return try {
        val datePart = iso.substringBefore('T')
        val timePart = iso.substringAfter('T').substringBeforeLast(':').take(5)
        val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val day = parts[2].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull()?.let { months.getOrNull(it - 1) } ?: parts[1]
            val year = parts[0]
            "%02d %s %s · %s".format(day, month, year, timePart)
        } else iso
    } catch (e: Exception) {
        iso
    }
}

/**
 * Timeline mood 7 hari (Senin..Minggu).
 *
 * TASK 2C: emoji diturunkan dari skor 0–100 (Mon=index 0, Sun=index 6) yang
 * di-load oleh [MoodViewModel.onLoadWeeklyScores]. Hari tanpa data (skor 0)
 * ditampilkan sebagai placeholder "-" dengan latar diredupkan.
 *
 * @param scores 7 elemen, range 0..100, urutan Senin..Minggu.
 * @param isLoading tampilkan spinner kecil menggantikan emoji bila true.
 * @param todayIndex 0..6 untuk highlight border hari ini. Default Sabtu (5)
 *   dipertahankan dari versi lama supaya preview tidak kosong.
 */
@Composable
private fun WeeklyMoodTimeline(
    scores: List<Int>,
    isLoading: Boolean = false,
    todayIndex: Int = 5,
    modifier: Modifier = Modifier
) {
    val days = listOf("S", "S", "R", "K", "J", "S", "M")
    val spacing = LocalSpacing.current
    val padded = remember(scores) {
        // Pastikan selalu 7 elemen untuk layout stabil.
        List(7) { idx -> scores.getOrNull(idx) ?: 0 }
    }
    val emojis = remember(padded) { padded.map { scoreToEmoji(it) } }
    val hasAnyData = padded.any { it > 0 }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mood Minggu Ini",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space3)
        )
        if (isLoading) {
            // Loading skeleton: spinner di tengah, layout tetap.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.space2),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    val isToday = index == todayIndex
                    val hasScore = padded[index] > 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(spacing.space2))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(
                                    if (!hasScore) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasScore) {
                                Text(text = emojis[index], style = MaterialTheme.typography.bodyLarge)
                            } else {
                                Text(
                                    text = "·",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            if (!hasAnyData) {
                Spacer(modifier = Modifier.height(spacing.space2))
                Text(
                    text = "Belum ada data mood minggu ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Map skor 0–100 → emoji. Skor 0 = placeholder (ditangani caller dengan `-`/`·`). */
private fun scoreToEmoji(score: Int): String = when {
    score <= 0 -> "·"
    score <= 20 -> "😔"
    score <= 40 -> "😕"
    score <= 60 -> "😐"
    score <= 80 -> "🙂"
    else -> "😊"
}

/** Index hari ini (0=Senin .. 6=Minggu) untuk highlight border pada timeline. */
private fun todayDayIndex(): Int {
    val dow = LocalDate.now().dayOfWeek
    return when (dow) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
        else -> 5
    }
}

data class JournalEntryData(
    val date: String,
    val moodEmoji: String,
    val moodText: String,
    val summary: String
)

@Composable
private fun JournalEntryCard(
    entry: JournalEntryData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    AppCard(
        modifier = modifier,
        onClick = onClick,
        variant = AppCardVariant.Elevated,
        contentPadding = spacing.space4,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = spacing.space2, vertical = spacing.space1)
                ) {
                    Text(text = entry.moodEmoji)
                    Spacer(modifier = Modifier.width(spacing.space1))
                    Text(
                        text = entry.moodText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(spacing.space3))
            
            Text(
                text = "AI Summary:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(spacing.space1))
            
            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(spacing.space3))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Read Full Chat",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(spacing.space1))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Read Full Chat",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JournalHistoryScreenPreview() {
    MindRestTheme {
        JournalHistoryScreen(
            onNavigateBack = {},
            onStartNewSessionClick = {}
        )
    }
}
