package com.example.features.lifestyle.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.components.AppScaffold
import com.example.core.designsystem.components.BaseCard
import com.example.core.designsystem.components.GoalCard
import com.example.core.designsystem.components.PrimaryButton
import com.example.core.designsystem.components.ProgressSummaryCard
import com.example.core.designsystem.components.SectionLabel
import com.example.core.designsystem.components.TopBar
import com.example.features.lifestyle.data.dto.SleepInsightData
import com.example.features.lifestyle.data.dto.SleepInsightItem
import com.example.features.lifestyle.presentation.state.LifestyleUiState
import com.example.features.lifestyle.presentation.viewmodel.LifestyleViewModel
import kotlin.random.Random

/**
 * Data model for daily lifestyle habit tracking item.
 */
data class LifestyleGoalItem(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val target: Float,
    val done: Float,
    val color: Color,
    val unit: String
)

/**
 * Module-level mock data list as specified in design-page-lifestyle.md.
 */
val initialLifestyleGoals = listOf(
    LifestyleGoalItem(
        id = "water",
        icon = Icons.Default.WaterDrop,
        label = "Drink 8 Glasses",
        target = 8f,
        done = 5f,
        color = Color(0xFF60A5FA), // Blue (#60A5FA) // residual: lifestyle category palette
        unit = "glasses"
    ),
    LifestyleGoalItem(
        id = "caffeine",
        icon = Icons.Default.Coffee,
        label = "Reduce Caffeine",
        target = 2f,
        done = 1f,
        color = Color(0xFFF5A940), // Amber (#F5A940) // residual: lifestyle category palette
        unit = "cups"
    ),
    LifestyleGoalItem(
        id = "sunlight",
        icon = Icons.Default.WbSunny,
        label = "Morning Sunlight",
        target = 1f,
        done = 1f,
        color = Color(0xFFEB845C), // Salmon (#EB845C) // residual: lifestyle category palette
        unit = "session"
    ),
    LifestyleGoalItem(
        id = "exercise",
        icon = Icons.Default.FitnessCenter,
        label = "Exercise 30min",
        target = 1f,
        done = 0f,
        color = Color(0xFF34D399), // Green (#34D399) // residual: lifestyle category palette
        unit = "session"
    ),
    LifestyleGoalItem(
        id = "meal",
        icon = Icons.Default.Restaurant,
        label = "Healthy Meal",
        target = 3f,
        done = 2f,
        color = Color(0xFFFB923C), // Orange (#FB923C) // residual: lifestyle category palette
        unit = "meals"
    ),
    LifestyleGoalItem(
        id = "screentime",
        icon = Icons.Default.Smartphone,
        label = "Screen Time <3h",
        target = 3f,
        done = 2.5f,
        color = Color(0xFFA78BFA), // Purple (#A78BFA) // residual: lifestyle category palette
        unit = "hours"
    )
)

/**
 * LifestyleScreen implements the daily habit tracking interface.
 */
@Composable
fun LifestyleScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRelaxation: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    // T-005 / FR-014: ViewModel untuk Sleep Insight pipeline (Gemini).
    // Default via viewModel() supaya MainActivity tidak perlu manual instantiate.
    lifestyleViewModel: LifestyleViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var goals by remember { mutableStateOf(initialLifestyleGoals) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val lifestyleUiState by lifestyleViewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current

    val completedCount = remember(goals) { goals.count { it.done >= it.target } }
    val totalCount = goals.size

    val fabRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 45f else 0f,
        label = "FabRotation"
    )

    AppScaffold(
        topBar = {
            TopBar(
                title = "Lifestyle",
                onBackClick = onNavigateBack,
                actionSlot = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFF5A940).copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🔥", fontSize = 12.sp) // residual: emoji size
                            Text(
                                text = "7 Days",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Relaxation Audio Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                            modifier = Modifier
                                .clickable {
                                    isMenuExpanded = false
                                    onNavigateToRelaxation()
                                }
                                .testTag("lifestyle_fab_relaxation")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "Relaxation Audio",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    isMenuExpanded = false
                                    onNavigateToRelaxation()
                                },
                                containerColor = Color(0xFF34D399), // Relaxation emerald
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Relaxation Audio"
                                )
                            }
                        }

                        // 2. New Journal Entry Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                            modifier = Modifier
                                .clickable {
                                    isMenuExpanded = false
                                    onNavigateToJournal()
                                }
                                .testTag("lifestyle_fab_journal")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "New Journal Entry",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    isMenuExpanded = false
                                    onNavigateToJournal()
                                },
                                containerColor = Color(0xFF60A5FA), // Journaling blue
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "New Journal Entry"
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .rotate(fabRotation)
                        .testTag("lifestyle_fab_main")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isMenuExpanded) "Close quick actions menu" else "Open quick actions menu"
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 1. Mindfulness Streak Header
                MindfulnessStreakCard(
                    currentStreak = 7,
                    targetMilestone = 10,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. ProgressSummaryCard
                ProgressSummaryCard(
                    completed = completedCount,
                    total = totalCount,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. DailyGoalsSection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionLabel(text = "Daily Goals")

                    goals.forEach { goal ->
                        val isComplete = goal.done >= goal.target
                        val progressRatio = (goal.done / goal.target).coerceIn(0f, 1f)
                        val doneText = if (goal.done % 1f == 0f) goal.done.toInt().toString() else goal.done.toString()
                        val targetText = if (goal.target % 1f == 0f) goal.target.toInt().toString() else goal.target.toString()
                        val progressText = "$doneText/$targetText ${goal.unit}"

                        GoalCard(
                            title = goal.label,
                            progress = progressRatio,
                            progressText = progressText,
                            icon = goal.icon,
                            color = goal.color,
                            isComplete = isComplete,
                            onBadgeClick = {
                                if (!isComplete) {
                                    goals = goals.map { item ->
                                        if (item.id == goal.id) {
                                            val step = if (item.unit == "hours") 0.5f else 1f
                                            val nextDone = (item.done + step).coerceAtMost(item.target)
                                            item.copy(done = nextDone)
                                        } else {
                                            item
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. SLEEP INSIGHT SECTION (T-005 / FR-014)
                // Generate rekomendasi personal (activities/foods/music) dari
                // riwayat sleep_logs via Edge Function `generate-sleep-insight`.
                SleepInsightCard(
                    uiState = lifestyleUiState,
                    onGenerate = { periodDays ->
                        lifestyleViewModel.onGenerateInsight(periodDays)
                    },
                    onInfoMessageShown = lifestyleViewModel::onInfoMessageShown,
                    onErrorShown = lifestyleViewModel::onErrorShown,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (isMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable { isMenuExpanded = false }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LifestylePreview() {
    MindRestTheme {
        LifestyleScreen()
    }
}

// =============================================================================
// SLEEP INSIGHT CARD (T-005 / FR-014)
// =============================================================================
// Rekomendasi personal (activities/foods/music) hasil Edge Function
// `generate-sleep-insight` yang membaca `sleep_logs` 7-30 hari terakhir.
//
// State machine (lihat LifestyleUiState):
//   - Idle       → CTA "Generate Insight" + helper text
//   - Loading    → progress + "Menganalisis pola tidur..."
//   - EmptyLogs  → CTA "Tambah log tidur dulu"
//   - Loaded     → summary + 3 section (activities/foods/music)
//   - Error      → pesan error + tombol coba lagi
// =============================================================================

/**
 * Card utama Sleep Insight. Menerima state dari ViewModel + callback generate.
 */
@Composable
private fun SleepInsightCard(
    uiState: LifestyleUiState,
    onGenerate: (Int) -> Unit,
    onInfoMessageShown: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    // Snackbar handler untuk info & error message.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.infoMessage) {
        val msg = uiState.infoMessage ?: return@LaunchedEffect
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
            onInfoMessageShown()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
            onErrorShown()
        }
    }

    Box(modifier = modifier) {
        BaseCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 20.dp, // residual: BaseCard radius
            padding = 16.dp, // residual: BaseCard padding
            testTag = "sleep_insight_card",
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp), // residual: icon intrinsic size
                    )
                    Text(
                        text = "Sleep Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = "Rekomendasi aktivitas, makanan & musik dari pola tidur Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(spacing.space4))

                when {
                    // Loading state
                    uiState.isGeneratingInsight -> {
                        SleepInsightLoadingState()
                    }
                    // User belum punya log tidur
                    uiState.showEmptyLogs -> {
                        SleepInsightEmptyLogsState(
                            message = uiState.emptyLogsMessage.orEmpty(),
                        )
                    }
                    // Insight sudah ada
                    uiState.insight != null -> {
                        SleepInsightContent(
                            insight = uiState.insight,
                            onRefresh = { onGenerate(DEFAULT_PERIOD_DAYS) },
                        )
                    }
                    // Error
                    uiState.errorMessage != null -> {
                        SleepInsightErrorState(
                            message = uiState.errorMessage,
                            onRetry = { onGenerate(DEFAULT_PERIOD_DAYS) },
                        )
                    }
                    // Idle / empty state
                    else -> {
                        SleepInsightIdleState(
                            onGenerate = { onGenerate(DEFAULT_PERIOD_DAYS) },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Window analisis default (hari). */
private const val DEFAULT_PERIOD_DAYS = 7

/** Idle state: CTA "Generate Insight" + helper text. */
@Composable
private fun SleepInsightIdleState(
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp), // residual: icon intrinsic size
        )
        Text(
            text = "Belum ada insight. Generate rekomendasi personal dari 7 hari tidur terakhir Anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        PrimaryButton(
            text = "Generate Insight",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            testTag = "generate_insight_btn",
        )
    }
}

/** Loading state: progress indicator + helpful message. */
@Composable
private fun SleepInsightLoadingState(
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp, // residual: stroke
        )
        Text(
            text = "Menganalisis pola tidur Anda...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Proses ini bisa 5-10 detik (AI sedang menyusun rekomendasi).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Empty logs state: CTA to add sleep logs first. */
@Composable
private fun SleepInsightEmptyLogsState(
    message: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp), // residual: icon intrinsic size
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Error state: message + retry button. */
@Composable
private fun SleepInsightErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp), // residual: no shape token for 12
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = spacing.space3, vertical = 10.dp), // residual: no token for 10
            )
        }
        PrimaryButton(
            text = "Coba Lagi",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            testTag = "retry_insight_btn",
        )
    }
}

/** Loaded state: summary + 3 recommendation sections + refresh. */
@Composable
private fun SleepInsightContent(
    insight: SleepInsightData,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        // Summary banner
        Surface(
            shape = RoundedCornerShape(12.dp), // residual: no shape token for 12
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.space3, vertical = 10.dp), // residual: no token for 10
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp), // residual: icon intrinsic size
                )
                Spacer(modifier = Modifier.width(spacing.space2))
                Text(
                    text = insight.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp, // residual: no token for 20sp
                )
            }
        }

        // 3 recommendation sections
        SleepInsightSection(
            title = "Aktivitas",
            icon = Icons.Default.DirectionsRun,
            iconTint = Color(0xFF34D399), // Emerald
            items = insight.recommendations.activities,
            testTag = "insight_activities_section",
        )
        SleepInsightSection(
            title = "Makanan & Minuman",
            icon = Icons.Default.Restaurant,
            iconTint = Color(0xFFFB923C), // Orange
            items = insight.recommendations.foods,
            testTag = "insight_foods_section",
        )
        SleepInsightSection(
            title = "Musik Relaksasi",
            icon = Icons.Default.MusicNote,
            iconTint = Color(0xFFA78BFA), // Purple
            items = insight.recommendations.music,
            testTag = "insight_music_section",
        )

        // Period + refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Berdasarkan ${insight.periodDays} hari terakhir",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onRefresh,
                contentPadding = PaddingValues(horizontal = spacing.space2, vertical = spacing.space1),
                modifier = Modifier.testTag("refresh_insight_btn"),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(16.dp), // residual: icon intrinsic size
                )
                Spacer(modifier = Modifier.width(spacing.space1))
                Text(text = "Refresh", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Single recommendation section: icon + title + bullet list of items. */
@Composable
private fun SleepInsightSection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    items: List<SleepInsightItem>,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    if (items.isEmpty()) return // defensive — schema guarantee 3-5, tapi guard saja
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp), // residual: icon intrinsic size
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconTint,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(16.dp), // residual: bullet point width
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp, // residual: no token for 20sp
                )
            }
        }
    }
}


/**
 * Data model for a single confetti particle in the milestone celebration effect.
 */
private data class ConfettiParticle(
    val xRatio: Float,
    val startYRatio: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val sizePx: Float,
    val isCircle: Boolean,
    val rotationStart: Float,
    val rotationSpeed: Float
)

/**
 * Subtle particle confetti celebration animation overlaid over the streak header.
 */
@Composable
fun ConfettiEffect(
    triggerKey: Any,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val animatable = remember { Animatable(0f) }
    val particles = remember(triggerKey) {
        val colors = listOf(
            Color(0xFFFB923C), // Orange
            Color(0xFFF5A940), // Amber
            Color(0xFF34D399), // Emerald
            Color(0xFF60A5FA), // Blue
            Color(0xFFA78BFA), // Purple
            Color(0xFFEB845C)  // Coral
        )
        val random = Random(triggerKey.hashCode())
        List(35) {
            ConfettiParticle(
                xRatio = 0.2f + random.nextFloat() * 0.6f,
                startYRatio = 0.15f + random.nextFloat() * 0.25f,
                vx = (random.nextFloat() - 0.5f) * 350f,
                vy = -200f - random.nextFloat() * 250f,
                color = colors[random.nextInt(colors.size)],
                sizePx = 10f + random.nextFloat() * 12f,
                isCircle = random.nextBoolean(),
                rotationStart = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey != 0) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2200, easing = LinearOutSlowInEasing)
            )
        }
    }

    val progress = animatable.value
    if (progress in 0.001f..0.999f) {
        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val gravity = 850f

            particles.forEach { p ->
                val currentX = p.xRatio * canvasWidth + p.vx * progress
                val currentY = p.startYRatio * canvasHeight + p.vy * progress + 0.5f * gravity * progress * progress
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val rotation = p.rotationStart + p.rotationSpeed * progress

                if (currentY <= canvasHeight && currentX in 0f..canvasWidth) {
                    withTransform({
                        rotate(rotation, pivot = Offset(currentX, currentY))
                    }) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color,
                                radius = p.sizePx / 2f,
                                center = Offset(currentX, currentY),
                                alpha = alpha
                            )
                        } else {
                            drawRoundRect(
                                color = p.color,
                                topLeft = Offset(currentX - p.sizePx / 2f, currentY - p.sizePx / 4f),
                                size = Size(p.sizePx, p.sizePx / 2f),
                                cornerRadius = CornerRadius(4f, 4f),
                                alpha = alpha
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * MindfulnessStreakCard displays the user's current active streak, weekly progress nodes,
 * and a gamified progress bar toward the next streak milestone with celebratory confetti.
 */
@Composable
fun MindfulnessStreakCard(
    currentStreak: Int = 7,
    targetMilestone: Int = 10,
    modifier: Modifier = Modifier,
    testTag: String = "mindfulness_streak_card"
) {
    val spacing = LocalSpacing.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var confettiTrigger by remember { mutableStateOf(1) }

    val progress = (currentStreak.toFloat() / targetMilestone.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "StreakProgressAnim"
    )

    val weekDays = listOf(
        Pair("M", true),
        Pair("T", true),
        Pair("W", true),
        Pair("T", true),
        Pair("F", true),
        Pair("S", true),
        Pair("S", false) // Today
    )

    val daysRemaining = (targetMilestone - currentStreak).coerceAtLeast(0)

    Box(modifier = modifier.testTag(testTag)) {
        Surface(
            shape = RoundedCornerShape(24.dp), // residual: no shape token for 24
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), // residual: border stroke
            shadowElevation = 0.dp, // residual: explicit elevation
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp), // residual: no token for 20
                verticalArrangement = Arrangement.spacedBy(14.dp) // residual: no token for 14
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.space3)
                    ) {
                        // Circular Progress Ring visual around streak icon
                        Box(
                            modifier = Modifier
                                .size(46.dp) // residual: streak ring size
                                .clickable { confettiTrigger++ },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = animatedProgress,
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFFB923C),
                                trackColor = Color(0xFFFB923C).copy(alpha = 0.2f),
                                strokeWidth = 3.5.dp // residual: stroke
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp) // residual: icon intrinsic size
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF3E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🔥", fontSize = 18.sp) // residual: emoji size
                            }
                        }

                        Column {
                            Text(
                                text = "MINDFULNESS STREAK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp // residual: letter spacing
                            )
                            Spacer(modifier = Modifier.height(2.dp)) // residual: no token for 2
                            Text(
                                text = "$currentStreak Days Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp) // residual: no token for 6
                    ) {
                        // "Days to goal" badge / Milestone celebration trigger
                        Surface(
                            shape = RoundedCornerShape(12.dp), // residual: no shape token for 12
                            color = Color(0xFFFB923C).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFB923C).copy(alpha = 0.3f)), // residual: border stroke
                            modifier = Modifier.clickable { confettiTrigger++ }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space1),
                                modifier = Modifier.padding(horizontal = spacing.space2, vertical = 6.dp) // residual: no token for 6
                            ) {
                                Text(
                                    text = "🎯",
                                    fontSize = 11.sp // residual: emoji size
                                )
                                Text(
                                    text = if (daysRemaining > 0) "$daysRemaining days to goal" else "Goal Reached! 🎉",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEA580C)
                                )
                            }
                        }

                        IconButton(
                            onClick = { notificationsEnabled = !notificationsEnabled },
                            modifier = Modifier
                                .size(32.dp) // residual: icon button size
                                .testTag("streak_notification_toggle")
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = if (notificationsEnabled) "Disable streak reminders" else "Enable streak reminders",
                                tint = if (notificationsEnabled) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp) // residual: icon intrinsic size
                            )
                        }
                    }
                }

                // Weekly day nodes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEachIndexed { index, (dayLabel, isDone) ->
                        val isToday = index == 6
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp) // residual: no token for 6
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp) // residual: day node size
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> Color(0xFFFB923C)
                                            isToday -> Color(0xFFFB923C).copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else 0.dp, // residual: no token for 2
                                        color = if (isToday) Color(0xFFFB923C) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { if (isDone || isToday) confettiTrigger++ },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed $dayLabel",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp) // residual: icon intrinsic size
                                    )
                                } else if (isToday) {
                                    Text(
                                        text = "🔥",
                                        fontSize = 14.sp // residual: emoji size
                                    )
                                }
                            }
                        }
                    }
                }

                // Gamified Progress towards next milestone
                Column(
                    modifier = Modifier.clickable { confettiTrigger++ },
                    verticalArrangement = Arrangement.spacedBy(6.dp) // residual: no token for 6
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milestone: 10-Day Streak",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentStreak / $targetMilestone days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp) // residual: progress bar height
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFB923C),
                                            Color(0xFFF5A940)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Overlay Confetti effect
        ConfettiEffect(
            triggerKey = confettiTrigger,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp)) // residual: no shape token for 24
        )
    }
}

