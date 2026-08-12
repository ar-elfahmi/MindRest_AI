package com.example.features.home.presentation.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.core.designsystem.DarkAccent
import com.example.core.designsystem.DarkBackground
import com.example.core.designsystem.DisplayFontFamily
import com.example.core.designsystem.FeatureJournaling
import com.example.core.designsystem.FeatureJourney
import com.example.core.designsystem.FeatureRelaxation
import com.example.core.designsystem.LightAccent
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.NumberM
import com.example.core.designsystem.NumberS
import com.example.core.designsystem.NumberXl
import com.example.core.designsystem.SleepHeroDarkEnd
import com.example.core.designsystem.SleepHeroDarkStart
import com.example.core.designsystem.SleepHeroLightEnd
import com.example.core.designsystem.SleepHeroLightStart
import com.example.core.designsystem.SuccessColor
import com.example.core.designsystem.components.*
import com.example.features.home.presentation.viewmodel.HomeViewModel
import com.example.features.mood.presentation.viewmodel.MoodViewModel
import com.example.features.sleep.presentation.viewmodel.SleepViewModel

// Mock data model for local reminders state
data class ReminderItemData(
    val title: String,
    val timeText: String,
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToJournal: () -> Unit = {},
    onNavigateToAiJournal: () -> Unit = {},
    onNavigateToRelaxation: () -> Unit = {},
    onNavigateToSleepTracking: () -> Unit = {},
    onNavigateToIkigai: () -> Unit = {},
    onNavigateToMoodTracking: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {},
    onLogSleepClick: () -> Unit = {},
    onMoodSelected: (String) -> Unit = {},
    onNavigateToLifestyle: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    // T-004 (FR-015): callback baru untuk widget Ikigai Progress.
    // Empty state → onNavigateToIkigaiAssessment.
    // Filled state → onNavigateToIkigaiReport.
    onNavigateToIkigaiAssessment: () -> Unit = {},
    onNavigateToIkigaiReport: () -> Unit = {},
    modifier: Modifier = Modifier,
    moodViewModel: MoodViewModel = viewModel(),
    sleepViewModel: SleepViewModel = viewModel(),
    // T-004 (FR-015): VM baru khusus widget Ikigai Progress + Sleep Insight preview.
    // Mood/sleep weekly aggregation TETAP di MoodViewModel/SleepViewModel (sesuai
    // scope T-2A/T-2B + "DON'T Touch" T-004).
    homeViewModel: HomeViewModel = viewModel(),
) {
    // Local state for bottom sheet
    var showCheckInSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val moodUiState by moodViewModel.uiState.collectAsState()

    // T-004 (FR-015): state untuk widget Ikigai Progress + Sleep Insight preview.
    val homeUiState by homeViewModel.uiState.collectAsState()

    // "Sudah check-in hari ini" ditentukan oleh DB via MoodUiState.todayMoodScore
    // (single source of truth — survive recompose/restart/tab switch).
    val hasCheckedInToday = moodUiState.todayMoodScore != null

    // Auto-load weekly mood scores (TASK 2A) + today check-in state.
    LaunchedEffect(Unit) {
        moodViewModel.onLoadWeeklyScores()
        moodViewModel.onLoadTodayMood()
    }

    // T-004 (FR-015): load Ikigai Progress count + Sleep Insight preview.
    // onLoadSleepInsightPreview() saat ini hanya reset state (placeholder);
    // real Gemini-generated preview menyusul T-005 / FR-014.
    LaunchedEffect(Unit) {
        homeViewModel.onLoadIkigaiAssessmentCount()
        homeViewModel.onLoadSleepInsightPreview()
    }

    // Tampilkan error snackbar bila insert mood/tidur gagal.
    LaunchedEffect(Unit) {
        moodViewModel.uiState.collect { state ->
            state.errorMessage?.let {
                snackbarHostState.showSnackbar("Mood: $it")
                moodViewModel.onMessageShown()
            }
        }
    }
    LaunchedEffect(Unit) {
        sleepViewModel.uiState.collect { state ->
            state.errorMessage?.let {
                snackbarHostState.showSnackbar("Tidur: $it")
                sleepViewModel.onMessageShown()
            }
        }
    }
    // Tampilkan error snackbar untuk weekly aggregation (TASK 2A).
    LaunchedEffect(Unit) {
        moodViewModel.uiState.collect { state ->
            state.weeklyError?.let {
                snackbarHostState.showSnackbar("Mood mingguan: $it")
                moodViewModel.onWeeklyErrorShown()
            }
        }
    }
    // T-004 (FR-015): snackbar untuk error Ikigai Progress / Sleep Insight preview.
    LaunchedEffect(Unit) {
        homeViewModel.uiState.collect { state ->
            state.ikigaiError?.let {
                snackbarHostState.showSnackbar("Ikigai: $it")
                homeViewModel.onIkigaiErrorShown()
            }
            state.sleepInsightError?.let {
                snackbarHostState.showSnackbar("Sleep Insight: $it")
                homeViewModel.onSleepInsightErrorShown()
            }
        }
    }

    // Local reminders state
    var remindersState by remember {
        mutableStateOf(
            listOf(
                ReminderItemData("Bedtime Routine", "10:00 PM", false),
                ReminderItemData("Drink Water", "8:00 AM", true)
            )
        )
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val spacing = LocalSpacing.current

    AppScaffold(modifier = modifier) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .testTag("home_screen_scrollable")
        ) {
            // 1. CUSTOM HEADER (Top Area)
        HeaderSection(
            userName = "Aria Kusuma",
            greeting = "GOOD EVENING 🌙",
            onNotificationClick = onNavigateToNotifications,
            onSettingsClick = onNavigateToSettings,
            isDark = isDark
        )

        // Main scrollable content container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .screenEdge()
                .padding(bottom = spacing.space6),
            verticalArrangement = Arrangement.spacedBy(spacing.space4),
            horizontalAlignment = Alignment.Start
        ) {
            // 2. SLEEP SCORE HERO CARD & SLEEP INPUT
            SleepScoreHeroCard(
                score = 84,
                sleptText = "7h 23m",
                trendText = "+12% from last week",
                onClick = onLogSleepClick,
                onLogSleepClick = onLogSleepClick,
                isDark = isDark
            )

            // 3. DAILY CHECK-IN TRIGGER CARD
            if (!hasCheckedInToday) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCheckInSheet = true }
                        .testTag("daily_checkin_trigger_card"),
                    shape = RoundedCornerShape(spacing.space4),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.space4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📝 Mulai Check-in Hari Ini",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "Terima kasih sudah check-in hari ini ✨",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = spacing.space2)
                )
            }

            // 3b. MOOD DETAIL ENTRY (TASK 1.2: route ke MoodTrackingScreen)
            // Diposisikan setelah check-in supaya langsung visible tanpa scroll.
            SectionLabel(text = "MOOD LOG")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMoodTracking() }
                    .testTag("mood_detail_card"),
                shape = RoundedCornerShape(spacing.space4),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.space4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lihat Mood Detail",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. QUICK ACTIONS ROW
            QuickActionsRow(
                onNavigateToJournal = onNavigateToJournal,
                onNavigateToRelax = onNavigateToRelaxation,
                onNavigateToSleep = onNavigateToSleepTracking,
                onNavigateToIkigai = onNavigateToIkigai
            )

            // 4b. IKIGAI PROGRESS WIDGET (T-004 / FR-015)
            // Sumber data: HomeUiState.ikigaiAssessmentCount (query count via
            // IkigaiRepository.getAssessmentCount). Empty state saat count == 0
            // menampilkan CTA "Mulai Ikigai Assessment" → onNavigateToIkigaiAssessment.
            // Filled state menampilkan "Lihat Laporan" + badge "X assessment"
            // → onNavigateToIkigaiReport.
            IkigaiProgressCard(
                assessmentCount = homeUiState.ikigaiAssessmentCount,
                isLoading = homeUiState.isLoadingIkigai,
                onStartAssessment = onNavigateToIkigaiAssessment,
                onViewReport = onNavigateToIkigaiReport,
            )

            // 5. WEEKLY SLEEP CHART CARD (TASK 2A: skor di-drive oleh mood mingguan
            //    dari MoodViewModel; rename kartu dan sumber data akan dirapikan
            //    saat TASK 3A dieksekusi.)
            WeeklySleepChartCard(
                avgText = "Avg 7.4h this week",
                weeklyScores = moodUiState.weeklyMoodScores,
                onClick = onNavigateToStatistics
            )

            // 6. AI SLEEP INSIGHTS CARD (T-004 / FR-015)
            // State binding: insightText dari HomeUiState.sleepInsightPreview.
            // null = belum ada insight (T-005 / FR-014 akan isi teks riil via
            // Gemini Edge Function). Saat ini placeholder null → empty state.
            AISleepInsightsCard(
                insightText = homeUiState.sleepInsightPreview,
                onSeeAllClick = onNavigateToRecommendations,
            )

            // 7. TODAY'S REMINDERS CARD
            TodaysRemindersCard(
                reminders = remindersState,
                onToggleReminder = { index ->
                    remindersState = remindersState.toMutableList().apply {
                        this[index] = this[index].copy(isCompleted = !this[index].isCompleted)
                    }
                },
                onViewAllClick = onNavigateToReminder
            )
        }
    }
        
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = spacing.space20)
    )
}
    }

if (showCheckInSheet) {
        DailyCheckInBottomSheet(
            onDismissRequest = { showCheckInSheet = false },
            onSave = { moodScore ->
                showCheckInSheet = false

                // TASK 1.1: simpan MOOD SAJA. Input tidur via SleepTrackingScreen.
                // Tidak ada lagi deriveSleepFromDuration → sleep_logs bersih dari noise.
                // hasCheckedInToday auto-update lewat MoodViewModel.onLoadTodayMood()
                // di-trigger setelah save sukses — single source of truth dari DB.
                moodViewModel.saveMoodScore(moodScore)

                // Feedback UX berbasis skor mood (<= 2 = berat → tawarkan refleksi).
                val needsHelp = moodScore <= 2

                coroutineScope.launch {
                    if (needsHelp) {
                        val result = snackbarHostState.showSnackbar(
                            message = "Check-in tersimpan! Pikiranmu berat — mau mengurainya?",
                            actionLabel = "Cerita Yuk",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onNavigateToAiJournal()
                        }
                    } else {
                        snackbarHostState.showSnackbar(
                            message = "Check-in tersimpan! ✨",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }
}

/**
 * 1. CUSTOM HEADER Component
 */
@Composable
private fun HeaderSection(
    userName: String,
    greeting: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val headerGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(DarkBackground, Color.Transparent))
    } else {
        Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGradient)
            .screenEdge()
            .padding(top = spacing.space5, bottom = spacing.componentGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification Button with red unread badge dot
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(spacing.space3))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .clickable(onClick = onNotificationClick)
                        .testTag("notification_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    // Small red unread badge dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = spacing.space2, end = spacing.space2)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .testTag("notification_unread_dot")
                    )
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(spacing.space3))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .clickable(onClick = onSettingsClick)
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2. SLEEP SCORE HERO CARD Component
 */
@Composable
private fun SleepScoreHeroCard(
    score: Int,
    sleptText: String,
    trendText: String,
    onClick: () -> Unit,
    onLogSleepClick: () -> Unit = onClick,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val sleepCardGradient = if (isDark) {
        Brush.linearGradient(colors = listOf(SleepHeroDarkStart, SleepHeroDarkEnd))
    } else {
        Brush.linearGradient(colors = listOf(SleepHeroLightStart, SleepHeroLightEnd))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.space6))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(spacing.space6))
            .background(sleepCardGradient)
            .clickable(onClick = onLogSleepClick)
            .padding(spacing.space5)
            .testTag("sleep_score_hero_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SLEEP SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.32.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(spacing.space1))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "$score",
                        style = NumberXl.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = " / 100",
                        style = NumberM.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(bottom = spacing.space2)
                    )
                }
                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = buildAnnotatedString {
                        append("You slept ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)) {
                            append(sleptText)
                        }
                        append(" last night")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(spacing.space2))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space1)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isDark) DarkAccent else LightAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "↗ $trendText",
                        style = NumberS.copy(
                            color = if (isDark) DarkAccent else LightAccent
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(spacing.space4))
            ProgressRing(
                progress = score / 100f,
                size = 88.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


/**
 * 4. QUICK ACTIONS ROW Component
 */
@Composable
private fun QuickActionsRow(
    onNavigateToJournal: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToIkigai: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val spacing = LocalSpacing.current

        SectionLabel(
            text = "QUICK ACTIONS",
            modifier = Modifier.padding(bottom = spacing.space2)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.componentGap)
        ) {
            QuickActionTile(
                label = "Journal",
                icon = Icons.Default.Book,
                onClick = onNavigateToJournal,
                color = FeatureJournaling,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Relax",
                icon = Icons.Default.MusicNote,
                onClick = onNavigateToRelax,
                color = FeatureRelaxation,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Sleep",
                icon = Icons.Default.Brightness3,
                onClick = onNavigateToSleep,
                color = FeatureJourney,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Ikigai",
                icon = Icons.Default.Explore,
                onClick = onNavigateToIkigai,
                color = FeatureJourney,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 5. WEEKLY SLEEP CHART CARD Component
 */
@Composable
private fun WeeklySleepChartCard(
    avgText: String,
    weeklyScores: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    BaseCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        testTag = "weekly_sleep_chart_card"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SectionLabel(text = "WEEKLY SLEEP")
                Text(
                    text = avgText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View Stats →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = spacing.space2)
                )
                Badge(
                    text = "↑ Good",
                    color = SuccessColor,
                    backgroundColor = SuccessColor.copy(alpha = 0.15f)
                )
            }
        }
        WeeklySleepBarChartCanvas(
            scores = weeklyScores,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Native Compose Canvas implementation for 7-day Weekly Sleep Bar Chart
 */
@Composable
private fun WeeklySleepBarChartCanvas(
    scores: List<Int>,
    modifier: Modifier = Modifier
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    val spacing = LocalSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("weekly_sleep_canvas_chart")
        ) {
            val count = scores.size.coerceAtLeast(1)
            val barWidth = 22.dp.toPx()
            val maxScore = 100f
            val availableWidth = size.width
            val availableHeight = size.height
            val spacing = (availableWidth - (barWidth * count)) / (count + 1)
            val maxVal = scores.maxOrNull() ?: 100

            scores.forEachIndexed { index, score ->
                val barHeight = (score / maxScore) * availableHeight
                val x = spacing + index * (barWidth + spacing)
                val y = availableHeight - barHeight

                val isPeak = score == maxVal
                val barColor = if (isPeak) primaryColor else mutedColor

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.space2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.take(scores.size).forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = labelColor
                )
            }
        }
    }
}

/**
 * 6. AI SLEEP INSIGHTS CARD Component
 *
 * T-004 (FR-015): `insightText` nullable — null = belum ada insight
 * (T-005 / FR-014 akan isi dari pola tidur 7 hari via Gemini Edge Function).
 * Saat null, tampilkan empty state hint + CTA "Lihat tidur 7 hari" (route
 * ke Statistik atau SleepHub).
 */
@Composable
private fun AISleepInsightsCard(
    insightText: String?,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExplanationExpanded by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        radius = spacing.space5,
        padding = spacing.space4,
        testTag = "ai_sleep_insights_card"
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(spacing.space2))
                .clickable { isExplanationExpanded = !isExplanationExpanded }
                .padding(vertical = spacing.space1)
                .testTag("ai_sleep_insights_header")
        ) {
            MoonLogo(size = 32.dp)
            Spacer(modifier = Modifier.width(spacing.componentGap))
            Text(
                text = "AI Sleep Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(spacing.space2))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Calculation info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = isExplanationExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.space2)
                    .clip(RoundedCornerShape(spacing.space2))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    .padding(spacing.space2)
            ) {
                Text(
                    text = "How sleep trends are calculated: AI models integrate total sleep duration, bedtime consistency, deep/REM sleep ratios, and nocturnal heart rate variation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.componentGap))
        // T-004: render real insight text, atau empty state "Belum ada".
        if (insightText.isNullOrBlank()) {
            Text(
                text = "Insight belum tersedia. Tambah log tidur 7 hari untuk mendapat insight personal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(spacing.space4))
        PrimaryButton(
            text = "See All AI Recommendations",
            onClick = onSeeAllClick,
            modifier = Modifier.fillMaxWidth(),
            testTag = "see_all_ai_recommendations_btn"
        )
    }
}

/**
 * 4b. IKIGAI PROGRESS CARD (T-004 / FR-015).
 *
 * Menampilkan status Ikigai user:
 * - Empty state (assessmentCount == 0): CTA "Mulai Ikigai Assessment"
 *   → onStartAssessment (navigate ke Screen.IkigaiAssessment).
 * - Filled state (assessmentCount >= 1): ringkasan "X assessment selesai"
 *   + badge "X assessment" + CTA "Lihat Laporan" → onViewReport (navigate
 *   ke Screen.IkigaiReport atau Screen.Ikigai dashboard).
 *
 * Sumber data: HomeUiState.ikigaiAssessmentCount (state-only, query count
 * dilakukan di HomeViewModel agar UI tetap simpel).
 */
@Composable
private fun IkigaiProgressCard(
    assessmentCount: Int,
    isLoading: Boolean,
    onStartAssessment: () -> Unit,
    onViewReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "ikigai_progress_card",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = spacing.componentGap),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                SectionLabel(text = "IKIGAI PROGRESS", modifier = Modifier.padding(bottom = spacing.space1))
                Text(
                    text = when {
                        isLoading -> "Memuat..."
                        assessmentCount == 0 -> "Kamu belum pernah assessment Ikigai."
                        assessmentCount == 1 -> "1 assessment selesai."
                        else -> "$assessmentCount assessment selesai."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (assessmentCount > 0) {
                Badge(
                    text = if (assessmentCount == 1) "1 assessment" else "$assessmentCount assessments",
                    color = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.componentGap))
        if (assessmentCount == 0) {
            PrimaryButton(
                text = "Mulai Ikigai Assessment",
                onClick = onStartAssessment,
                modifier = Modifier.fillMaxWidth(),
                testTag = "ikigai_start_assessment_btn",
            )
        } else {
            PrimaryButton(
                text = "Lihat Laporan",
                onClick = onViewReport,
                modifier = Modifier.fillMaxWidth(),
                testTag = "ikigai_view_report_btn",
            )
        }
    }
}

/**
 * 7. TODAY'S REMINDERS CARD Component
 */
@Composable
private fun TodaysRemindersCard(
    reminders: List<ReminderItemData>,
    onToggleReminder: (Int) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "todays_reminders_card"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.componentGap),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = "TODAY'S REMINDERS")
            Text(
                text = "View all",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable(onClick = onViewAllClick)
                    .testTag("view_all_reminders")
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            reminders.forEachIndexed { index, reminder ->
                val alpha = if (reminder.isCompleted) 0.6f else 1.0f
                val textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .alpha(alpha)
                        .clickable { onToggleReminder(index) }
                        .testTag("reminder_item_$index"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Small colored dot indicator on the left
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.primary
                                )
                                .testTag("reminder_dot_$index")
                        )
                        Spacer(modifier = Modifier.width(spacing.componentGap))
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            textDecoration = textDecoration,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = reminder.timeText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MindRestTheme {
        HomeScreen()
    }
}



