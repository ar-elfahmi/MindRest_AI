package com.example.features.sleep.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.*
import com.example.core.designsystem.components.*
import com.example.core.network.dto.SleepLogRow
import com.example.features.sleep.presentation.viewmodel.SleepViewModel

private data class RecommendationItem(
    val category: String,
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val body: String,
    val tags: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepHubScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SleepViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Auto-load history ketika screen pertama kali dibuka.
    LaunchedEffect(Unit) { viewModel.onLoadHistory() }

    var selectedPeriod by remember { mutableStateOf("Weekly") }

    val recommendations = listOf(
        RecommendationItem(
            category = "Sleep",
            icon = Icons.Default.Lightbulb,
            color = FeatureJourney,
            title = "Contextual Sleep Tip",
            body = "Your deep sleep was 18% lower on Wednesday following an irregular bedtime. Shifting bedtime 20 minutes earlier on weeknights improves sleep consistency.",
            tags = listOf("Consistency", "Deep Sleep")
        ),
        RecommendationItem(
            category = "Sleep",
            icon = Icons.Default.NightsStay,
            color = FeatureJourney,
            title = "Optimize Your Sleep Window",
            body = "Based on your chronotype, your ideal sleep window is 10:30 PM – 6:30 AM. Shifting bedtime 30 minutes earlier could improve deep sleep by 20%.",
            tags = listOf("Sleep Hygiene", "Circadian Rhythm")
        )
    )

    val sampleWeeklyScores = listOf(78, 82, 70, 85, 88, 75, 90)

    Scaffold(
        topBar = {
            TopBar(
                title = "Sleep & Insights",
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP BAR & CONTROLS: Segmented button for "Weekly | Monthly | Yearly"
            item {
                PeriodToggle(
                    selectedOption = selectedPeriod,
                    onOptionSelected = { selectedPeriod = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SECTION 1: LAST NIGHT'S OVERVIEW
            item {
                SleepScoreCard(
                    score = 88,
                    hours = 7,
                    minutes = 42,
                    subtitle = "Last Night's Sleep Quality",
                    large = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        label = "Bedtime",
                        value = "11:15 PM",
                        icon = Icons.Default.Bedtime,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Wake Time",
                        value = "06:57 AM",
                        icon = Icons.Default.WbSunny,
                        iconColor = FeatureLifestyle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Efficiency",
                        value = "94%",
                        icon = Icons.Default.Speed,
                        iconColor = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SECTION 2: SLEEP ARCHITECTURE
            item {
                BaseCard(
                    radius = 20.dp,
                    padding = 16.dp
                ) {
                    SectionLabel(text = "SLEEP STAGE DISTRIBUTION")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SleepStageRadialChart(
                            lightValue = 0.48f,
                            deepValue = 0.24f,
                            remValue = 0.28f,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StageLegendRow(
                                label = "Light Sleep",
                                duration = "3h 42m (48%)",
                                color = Color(0xFFEB845C)
                            )
                            StageLegendRow(
                                label = "Deep Sleep",
                                duration = "1h 52m (24%)",
                                color = FeatureJourney
                            )
                            StageLegendRow(
                                label = "REM Sleep",
                                duration = "2h 08m (28%)",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // SECTION 3: TRENDS & AI RECOMMENDATIONS
            item {
                SectionLabel(text = "SLEEP QUALITY TRENDS")
            }

            item {
                BaseCard(
                    radius = 20.dp,
                    padding = 16.dp
                ) {
                    WeeklySleepBarChart(
                        scores = sampleWeeklyScores,
                        averageScore = 81f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(recommendations) { item ->
                RecommendationCard(
                    category = item.category,
                    title = item.title,
                    body = item.body,
                    icon = item.icon,
                    color = item.color,
                    tags = item.tags
                )
            }

            // SECTION 4: RECENT SLEEP LOGS (data real dari Supabase)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionLabel(text = "RIWAYAT TIDUR TERAKHIR")
            }

            when {
                state.isLoadingHistory && state.recentSleepLogs.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                state.historyError != null && state.recentSleepLogs.isEmpty() -> item {
                    Text(
                        text = state.historyError!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                state.recentSleepLogs.isEmpty() -> item {
                    Text(
                        text = "Belum ada riwayat tidur. Simpan log pertamamu!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                else -> items(state.recentSleepLogs.take(7)) { row ->
                    RecentSleepLogCard(row)
                }
            }
        }
    }
}

@Composable
private fun RecentSleepLogCard(row: SleepLogRow) {
    BaseCard(
        radius = 16.dp,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = null,
                tint = FeatureJourney
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${formatTimeShort(row.bedTime)} → ${formatTimeShort(row.wakeUpTime)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Kualitas: ${row.sleepQuality} · ${formatIsoDateShort(row.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Potong ISO timestamp "2025-08-09T22:00:00Z" menjadi "22:00". */
private fun formatTimeShort(iso: String): String {
    return try {
        iso.substringAfter('T').take(5)
    } catch (e: Exception) {
        iso
    }
}

/** Potong ISO timestamp menjadi "09 Agu". */
private fun formatIsoDateShort(iso: String): String {
    return try {
        val datePart = iso.substringBefore('T')
        val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val day = parts[2].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull()?.let { months.getOrNull(it - 1) } ?: parts[1]
            "%02d %s".format(day, month)
        } else iso
    } catch (e: Exception) {
        iso
    }
}

@Composable
private fun StageLegendRow(
    label: String,
    duration: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = duration,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SleepHubScreenPreview() {
    MindRestTheme {
        SleepHubScreen(onNavigateBack = {})
    }
}
