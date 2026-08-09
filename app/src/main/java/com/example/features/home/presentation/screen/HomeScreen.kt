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
import com.example.core.designsystem.DisplayFontFamily
import com.example.core.designsystem.FeatureJournaling
import com.example.core.designsystem.FeatureJourney
import com.example.core.designsystem.FeatureRelaxation
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.NumberM
import com.example.core.designsystem.NumberS
import com.example.core.designsystem.NumberXl
import com.example.core.designsystem.components.*
import com.example.features.mood.presentation.viewmodel.MoodViewModel
import com.example.features.sleep.presentation.state.SleepQuality
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {},
    onLogSleepClick: () -> Unit = {},
    onMoodSelected: (String) -> Unit = {},
    onNavigateToLifestyle: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier,
    moodViewModel: MoodViewModel = viewModel(),
    sleepViewModel: SleepViewModel = viewModel(),
) {
    // Local state for bottom sheet
    var showCheckInSheet by remember { mutableStateOf(false) }
    var hasCheckedInToday by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

    Box(modifier = modifier.fillMaxSize()) {
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
                )
            }

            // 4. QUICK ACTIONS ROW
            QuickActionsRow(
                onNavigateToJournal = onNavigateToJournal,
                onNavigateToRelax = onNavigateToRelaxation,
                onNavigateToSleep = onNavigateToSleepTracking,
                onNavigateToIkigai = onNavigateToIkigai
            )

            // 5. WEEKLY SLEEP CHART CARD
            WeeklySleepChartCard(
                avgText = "Avg 7.4h this week",
                weeklyScores = listOf(62, 68, 74, 71, 65, 80, 84),
                onClick = onNavigateToStatistics
            )

            // 6. AI SLEEP INSIGHTS CARD
            AISleepInsightsCard(
                insightText = "Your sleep quality improved by 12% this week. Try reducing screen time 1 hour before bed for even better results.",
                onSeeAllClick = onNavigateToRecommendations
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
            .padding(bottom = 80.dp)
    )
}

if (showCheckInSheet) {
        DailyCheckInBottomSheet(
            onDismissRequest = { showCheckInSheet = false },
            onSave = { emotions, sleepHours ->
                showCheckInSheet = false
                hasCheckedInToday = true

                // 1. Simpan mood: petakan emosi terpilih → skor 1-5.
                val moodScore = mapEmotionsToMoodScore(emotions)
                moodViewModel.saveMoodScore(moodScore)

                // 2. Simpan log tidur: turunkan bed/wake/quality dari durasi.
                val (bedTime, wakeTime, quality) = deriveSleepFromDuration(sleepHours)
                sleepViewModel.saveSleepLog(bedTime, wakeTime, quality)

                // 3. Feedback UX (existing logic).
                val negativeEmotions = setOf("Cemas", "Lelah", "Overthinking", "Kesepian")
                val needsHelp = emotions.any { it in negativeEmotions }

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
                            message = "Check-in tersimpan! ✨ (mood + tidur)",
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
    val headerGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF090C1A), Color.Transparent))
    } else {
        Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGradient)
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 12.dp)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification Button with red unread badge dot
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            .padding(top = 6.dp, end = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .testTag("notification_unread_dot")
                    )
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
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
    val sleepCardGradient = if (isDark) {
        Brush.linearGradient(colors = listOf(Color(0xFF1A1040), Color(0xFF0D1A2E)))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFFEDE9FD), Color(0xFFE0F2FE)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .background(sleepCardGradient)
            .clickable(onClick = onLogSleepClick)
            .padding(20.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
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
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF4ECDC4) else Color(0xFFEB845C),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "↗ $trendText",
                        style = NumberS.copy(
                            color = if (isDark) Color(0xFF4ECDC4) else Color(0xFFEB845C)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
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
        SectionLabel(
            text = "QUICK ACTIONS",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    BaseCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        radius = 16.dp,
        padding = 16.dp,
        testTag = "weekly_sleep_chart_card"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SectionLabel(text = "WEEKLY SLEEP", modifier = Modifier.padding(bottom = 0.dp))
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
                    modifier = Modifier.padding(end = 8.dp)
                )
                Badge(
                    text = "↑ Good",
                    color = Color(0xFF34C98A),
                    backgroundColor = Color(0xFF34C98A).copy(alpha = 0.15f)
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
        Spacer(modifier = Modifier.height(8.dp))
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
 */
@Composable
private fun AISleepInsightsCard(
    insightText: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExplanationExpanded by remember { mutableStateOf(false) }

    BaseCard(
        modifier = modifier.fillMaxWidth(),
        radius = 20.dp,
        padding = 16.dp,
        testTag = "ai_sleep_insights_card"
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExplanationExpanded = !isExplanationExpanded }
                .padding(vertical = 4.dp)
                .testTag("ai_sleep_insights_header")
        ) {
            MoonLogo(size = 32.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Sleep Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
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
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "How sleep trends are calculated: AI models integrate total sleep duration, bedtime consistency, deep/REM sleep ratios, and nocturnal heart rate variation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = insightText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "See All AI Recommendations",
            onClick = onSeeAllClick,
            modifier = Modifier.fillMaxWidth(),
            testTag = "see_all_ai_recommendations_btn"
        )
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
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        radius = 16.dp,
        padding = 16.dp,
        testTag = "todays_reminders_card"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = "TODAY'S REMINDERS", modifier = Modifier.padding(bottom = 0.dp))
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Spacer(modifier = Modifier.width(12.dp))
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

// ---------------------------------------------------------------------------
// Helper: petakan emosi Check-in → mood score 1-5 + turunkan log tidur.
// ---------------------------------------------------------------------------

/**
 * Petakan daftar emosi yang dipilih di DailyCheckInBottomSheet menjadi skor
 * mood 1-5 (1 = Terrible, 5 = Awesome).
 *
 * Strategi: hitung selisih emosi positif vs negatif (net), lalu bagi ke 5 tier.
 */
private fun mapEmotionsToMoodScore(emotions: List<String>): Int {
    val positive = setOf("Tenang", "Bersyukur", "Damai", "Fokus", "Bersemangat")
    val negative = setOf("Lelah", "Cemas", "Overthinking", "Kesepian")
    val net = emotions.count { it in positive } - emotions.count { it in negative }
    return when {
        net >= 2 -> 5  // Awesome
        net == 1 -> 4  // Good
        net == 0 -> 3  // Okay
        net == -1 -> 2 // Bad
        else -> 1      // Terrible
    }
}

/**
 * Turunkan bed_time / wake_up_time / sleep_quality dari durasi tidur (jam).
 *
 * Asumsi: check-in dilakukan pagi hari (“Simpan Jurnal Pagi”), jadi:
 *   • wakeTime ≈ waktu sekarang (user baru bangun)
 *   • bedTime  = sekarang − durasi tidur
 *   • quality  = diturunkan dari durasi (< 5h POOR, 5-7h FAIR, 7-9h GOOD, >9h EXCELLENT)
 */
private fun deriveSleepFromDuration(hours: Float): Triple<String, String, SleepQuality> {
    val now = LocalDateTime.now()
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val wakeTime = now.toLocalTime().format(fmt)
    val bedTime = now.minusMinutes((hours * 60).toLong()).toLocalTime().format(fmt)
    val quality = when {
        hours < 5f -> SleepQuality.POOR
        hours < 7f -> SleepQuality.FAIR
        hours <= 9f -> SleepQuality.GOOD
        else -> SleepQuality.EXCELLENT
    }
    return Triple(bedTime, wakeTime, quality)
}

