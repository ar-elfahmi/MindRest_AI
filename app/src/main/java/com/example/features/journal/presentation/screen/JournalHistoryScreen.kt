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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.MindRestTheme
import com.example.core.network.dto.JournalEntryRow
import com.example.features.journal.presentation.viewmodel.JournalViewModel

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalHistoryScreen(
    onNavigateBack: () -> Unit,
    onStartNewSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Auto-load history setiap kali screen dibuka.
    LaunchedEffect(Unit) { viewModel.onLoadHistory() }

    val entries = state.recentEntries.map { it.toJournalEntryData() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                WeeklyMoodTimeline(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sesi Sebelumnya",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                )
            }
            
            when {
                state.isLoadingHistory && entries.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                state.historyError != null && entries.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada entri jurnal. Mulai sesi pertama kamu!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                else -> items(entries) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { /* TODO: Navigate to journal detail */ },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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

@Composable
private fun WeeklyMoodTimeline(modifier: Modifier = Modifier) {
    val days = listOf("S", "S", "R", "K", "J", "S", "M")
    val emojis = listOf("😊", "😐", "😔", "😫", "😊", "😊", "")
    val todayIndex = 5 // Suppose today is Saturday

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
                .padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val isToday = index == todayIndex
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
                    Spacer(modifier = Modifier.height(8.dp))
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
                                if (emojis[index].isEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (emojis[index].isNotEmpty()) {
                            Text(text = emojis[index], style = MaterialTheme.typography.bodyLarge)
                        } else {
                            Text(text = "-", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
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
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = entry.moodEmoji)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.moodText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "AI Summary:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Read Full Chat",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
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
