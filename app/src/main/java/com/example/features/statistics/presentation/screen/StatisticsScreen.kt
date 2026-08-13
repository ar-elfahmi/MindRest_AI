package com.example.features.statistics.presentation.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.FeatureJourney
import com.example.core.designsystem.FeatureLifestyle
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.NumberS
import com.example.core.designsystem.NumberXl
import com.example.core.designsystem.SuccessColor
import com.example.core.designsystem.components.AreaTrendChart
import com.example.core.designsystem.components.Badge
import com.example.core.designsystem.components.BaseCard
import com.example.core.designsystem.components.ChartCallout
import com.example.core.designsystem.components.PeriodToggle
import com.example.core.designsystem.components.TopBar

data class TrendPoint(
    val day: String,
    val sleep: Float,
    val mood: Float,
    val purpose: Float,
    val lifestyle: Float
)

val weeklyTrendData = listOf(
    TrendPoint(day = "Mon", sleep = 68f, mood = 70f, purpose = 72f, lifestyle = 65f),
    TrendPoint(day = "Wed", sleep = 74f, mood = 72f, purpose = 73f, lifestyle = 70f),
    TrendPoint(day = "Fri", sleep = 78f, mood = 75f, purpose = 74f, lifestyle = 74f),
    TrendPoint(day = "Sun", sleep = 84f, mood = 78f, purpose = 75f, lifestyle = 78f)
)

val previousWeeklyTrendData = listOf(
    TrendPoint(day = "Mon", sleep = 60f, mood = 65f, purpose = 68f, lifestyle = 60f),
    TrendPoint(day = "Wed", sleep = 65f, mood = 68f, purpose = 70f, lifestyle = 64f),
    TrendPoint(day = "Fri", sleep = 70f, mood = 70f, purpose = 72f, lifestyle = 68f),
    TrendPoint(day = "Sun", sleep = 75f, mood = 72f, purpose = 73f, lifestyle = 70f)
)

val monthlyTrendData = listOf(
    TrendPoint(day = "W1", sleep = 72f, mood = 65f, purpose = 58f, lifestyle = 60f),
    TrendPoint(day = "W2", sleep = 75f, mood = 70f, purpose = 62f, lifestyle = 65f),
    TrendPoint(day = "W3", sleep = 80f, mood = 68f, purpose = 70f, lifestyle = 72f),
    TrendPoint(day = "W4", sleep = 84f, mood = 78f, purpose = 75f, lifestyle = 78f)
)

val previousMonthlyTrendData = listOf(
    TrendPoint(day = "W1", sleep = 65f, mood = 60f, purpose = 52f, lifestyle = 55f),
    TrendPoint(day = "W2", sleep = 68f, mood = 62f, purpose = 56f, lifestyle = 58f),
    TrendPoint(day = "W3", sleep = 72f, mood = 65f, purpose = 62f, lifestyle = 65f),
    TrendPoint(day = "W4", sleep = 76f, mood = 70f, purpose = 68f, lifestyle = 70f)
)

val yearlyTrendData = listOf(
    TrendPoint(day = "Q1", sleep = 65f, mood = 60f, purpose = 55f, lifestyle = 58f),
    TrendPoint(day = "Q2", sleep = 70f, mood = 68f, purpose = 65f, lifestyle = 64f),
    TrendPoint(day = "Q3", sleep = 78f, mood = 72f, purpose = 70f, lifestyle = 71f),
    TrendPoint(day = "Q4", sleep = 84f, mood = 78f, purpose = 75f, lifestyle = 78f)
)

val previousYearlyTrendData = listOf(
    TrendPoint(day = "Q1", sleep = 58f, mood = 55f, purpose = 50f, lifestyle = 52f),
    TrendPoint(day = "Q2", sleep = 62f, mood = 60f, purpose = 58f, lifestyle = 58f),
    TrendPoint(day = "Q3", sleep = 68f, mood = 65f, purpose = 64f, lifestyle = 65f),
    TrendPoint(day = "Q4", sleep = 72f, mood = 70f, purpose = 68f, lifestyle = 70f)
)

val trendData = monthlyTrendData

private data class StatCardItem(
    val label: String,
    val value: String,
    val unit: String,
    val color: Color,
    val icon: ImageVector
)

private data class TrendChartItem(
    val label: String,
    val key: String,
    val values: List<Float>,
    val overlayValues: List<Float>? = null,
    val color: Color
)

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var period by remember { mutableStateOf("Monthly") }
    var showShareDialog by remember { mutableStateOf(false) }
    var isComparativeOverlayEnabled by remember { mutableStateOf(false) }

    val activeTrendData = remember(period) {
        when (period) {
            "Weekly" -> weeklyTrendData
            "Yearly" -> yearlyTrendData
            else -> monthlyTrendData
        }
    }

    val activePreviousTrendData = remember(period) {
        when (period) {
            "Weekly" -> previousWeeklyTrendData
            "Yearly" -> previousYearlyTrendData
            else -> previousMonthlyTrendData
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val statCardItems = remember(primaryColor, period) {
        val sleepVal = if (period == "Weekly") "81" else if (period == "Yearly") "78" else "84"
        val moodVal = if (period == "Weekly") "75" else if (period == "Yearly") "72" else "78"
        val purposeVal = if (period == "Weekly") "73" else if (period == "Yearly") "68" else "75"
        val lifestyleVal = if (period == "Weekly") "70" else if (period == "Yearly") "67" else "72"

        listOf(
            StatCardItem(
                label = "Avg Sleep Score",
                value = sleepVal,
                unit = "/100",
                color = primaryColor,
                icon = Icons.Default.NightsStay
            ),
            StatCardItem(
                label = "Mood Average",
                value = moodVal,
                unit = "/100",
                color = Color(0xFFF472B6), // residual: pink mood color, design-spec specific
                icon = Icons.Default.SentimentSatisfied
            ),
            StatCardItem(
                label = "Purpose Score",
                value = purposeVal,
                unit = "/100",
                color = FeatureJourney,
                icon = Icons.Default.Explore
            ),
            StatCardItem(
                label = "Lifestyle",
                value = lifestyleVal,
                unit = "/100",
                color = FeatureLifestyle,
                icon = Icons.Default.DirectionsRun
            )
        )
    }

    val trendChartItems = remember(primaryColor, activeTrendData, activePreviousTrendData, isComparativeOverlayEnabled) {
        // Render order as specified: Mood -> Sleep -> Purpose -> Lifestyle
        listOf(
            TrendChartItem(
                label = "Mood Trend",
                key = "mood",
                values = activeTrendData.map { it.mood },
                overlayValues = if (isComparativeOverlayEnabled) activePreviousTrendData.map { it.mood } else null,
                color = Color(0xFFF472B6) // residual: pink mood color, design-spec specific
            ),
            TrendChartItem(
                label = "Sleep Trend",
                key = "sleep",
                values = activeTrendData.map { it.sleep },
                overlayValues = if (isComparativeOverlayEnabled) activePreviousTrendData.map { it.sleep } else null,
                color = primaryColor
            ),
            TrendChartItem(
                label = "Purpose Trend",
                key = "purpose",
                values = activeTrendData.map { it.purpose },
                overlayValues = if (isComparativeOverlayEnabled) activePreviousTrendData.map { it.purpose } else null,
                color = FeatureJourney
            ),
            TrendChartItem(
                label = "Lifestyle Trend",
                key = "lifestyle",
                values = activeTrendData.map { it.lifestyle },
                overlayValues = if (isComparativeOverlayEnabled) activePreviousTrendData.map { it.lifestyle } else null,
                color = FeatureLifestyle
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopBar (Non-scrolling)
        TopBar(
            title = "Statistics",
            onBackClick = onNavigateBack,
            actionSlot = {
                IconButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier.testTag("share_statistics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Report",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            testTag = "statistics_top_bar"
        )

        if (showShareDialog) {
            val context = LocalContext.current
            val reportText = remember(period, statCardItems) {
                """
                📊 MindRest Wellness Report ($period)
                -------------------------------------
                • Avg Sleep Score: ${statCardItems[0].value}/100
                • Mood Average: ${statCardItems[1].value}/100
                • Purpose Level: ${statCardItems[2].value}/100
                • Lifestyle Score: ${statCardItems[3].value}/100
                
                💡 Insight: Sleep quality improved consistently over this $period view with steady circadian rhythm control.
                """.trimIndent()
            }

            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = {
                    Text(
                        text = "Export Sleep Report",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                        Text(
                            text = "Share your formatted $period wellness summary as a text report or image export.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp), // residual: RoundedCornerShape
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = reportText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(spacing.space3)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showShareDialog = false
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Sleep Report")
                            context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp) // residual: icon intrinsic size
                        )
                        Spacer(modifier = Modifier.width(6.dp)) // residual: no token for 6
                        Text("Share")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp) // residual: RoundedCornerShape
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space5)
        ) {
            // 1. Period Toggle (Weekly, Monthly, Yearly)
            PeriodToggle(
                selectedOption = period,
                onOptionSelected = { period = it },
                options = listOf("Weekly", "Monthly", "Yearly"),
                modifier = Modifier.fillMaxWidth(),
                testTag = "statistics_period_toggle"
            )

            // 1b. Comparative Overlay Toggle
            BaseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comparative_overlay_toggle_card"),
                radius = 16.dp, // residual: BaseCard drawing spec
                padding = 12.dp // residual: BaseCard drawing spec
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prevLabel = when (period) {
                        "Weekly" -> "Last Week's Data"
                        "Yearly" -> "Last Year's Data"
                        else -> "Last Month's Data"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp) // residual: no token for 10
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp), // residual: RoundedCornerShape
                            color = if (isComparativeOverlayEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = if (isComparativeOverlayEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(spacing.space2)
                                    .size(20.dp) // residual: icon intrinsic size
                            )
                        }
                        Column {
                            Text(
                                text = "Overlay $prevLabel",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isComparativeOverlayEnabled) "Historical baseline visible as dashed line" else "Compare progress against previous $period",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isComparativeOverlayEnabled,
                        onCheckedChange = { isComparativeOverlayEnabled = it },
                        modifier = Modifier.testTag("comparative_toggle_switch")
                    )
                }
            }

            // 2. Summary Section: Sleep Quality Improvement Trend
            BaseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sleep_summary_section"),
                radius = 20.dp, // residual: BaseCard drawing spec
                padding = 18.dp // residual: no token for 18
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                    val summaryTitle = "$period Sleep Summary"
                    val summarySubtitle = when (period) {
                        "Weekly" -> "7-Day Sleep Quality Trend"
                        "Yearly" -> "12-Month Sleep Quality Trend"
                        else -> "4-Week Sleep Quality Improvement"
                    }
                    val growthBadgeText = when (period) {
                        "Weekly" -> "+5% This Week"
                        "Yearly" -> "+22% YoY"
                        else -> "+12% Growth"
                    }
                    val insightText = when (period) {
                        "Weekly" -> "Consistent bedtime schedule this week yielded higher sleep efficiency towards Sunday."
                        "Yearly" -> "Long-term sleep quality showed a sustained 22% upward trajectory across all four quarters."
                        else -> "Sleep efficiency increased consistently over 4 weeks due to regular bedtimes and CBT relaxation."
                    }

                    // Title & Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = summaryTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = summarySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Badge(
                            text = growthBadgeText,
                            color = SuccessColor,
                            backgroundColor = SuccessColor.copy(alpha = 0.15f),
                            testTag = "summary_growth_badge"
                        )
                    }

                    if (isComparativeOverlayEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space4)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp) // residual: no token for 6
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp) // residual: legend swatch width
                                        .height(3.dp) // residual: legend swatch height
                                        .background(primaryColor, RoundedCornerShape(2.dp)) // residual: RoundedCornerShape
                                )
                                Text(
                                    text = "Current $period",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp, // residual: chart text size
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp) // residual: no token for 6
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp) // residual: legend swatch width
                                        .height(2.dp) // residual: legend swatch height
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                                )
                                Text(
                                    text = "Previous $period",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp, // residual: chart text size
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Key metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "AVG SCORE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${statCardItems[0].value} / 100",
                                style = NumberXl,
                                fontSize = 22.sp, // residual: chart number size
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        Column {
                            Text(
                                text = "PEAK QUALITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (period == "Weekly") "86 pts" else if (period == "Yearly") "90 pts" else "88 pts",
                                style = NumberXl,
                                fontSize = 22.sp, // residual: chart number size
                                fontWeight = FontWeight.Bold,
                                color = FeatureJourney
                            )
                        }
                        Column {
                            Text(
                                text = "CONSISTENCY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (period == "Weekly") "89%" else if (period == "Yearly") "94%" else "92%",
                                style = NumberXl,
                                fontSize = 22.sp, // residual: chart number size
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                    }

                    // Smooth Sleep Quality Line Trend Chart
                    AreaTrendChart(
                        values = activeTrendData.map { it.sleep },
                        color = primaryColor,
                        labels = activeTrendData.map { it.day },
                        callouts = listOf(
                            ChartCallout(index = 0, text = "Start", isMilestone = false),
                            ChartCallout(index = activeTrendData.size - 1, text = "Peak ${statCardItems[0].value}", isMilestone = true)
                        ),
                        overlayValues = if (isComparativeOverlayEnabled) activePreviousTrendData.map { it.sleep } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp), // residual: chart container height
                        testTag = "summary_area_trend_chart"
                    )

                    // X-Axis Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        activeTrendData.forEach { point ->
                            Text(
                                text = point.day,
                                style = NumberS,
                                fontSize = 10.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Summary Insight Footer
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = primaryColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp) // residual: RoundedCornerShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = spacing.space3, vertical = spacing.space2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SuccessColor,
                                modifier = Modifier.size(16.dp) // residual: icon intrinsic size
                            )
                            Spacer(modifier = Modifier.width(spacing.space2))
                            Text(
                                text = insightText,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. Stats Grid (2x2)
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.space3),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_grid")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCardView(
                        item = statCardItems[0],
                        modifier = Modifier.weight(1f)
                    )
                    StatCardView(
                        item = statCardItems[1],
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCardView(
                        item = statCardItems[2],
                        modifier = Modifier.weight(1f)
                    )
                    StatCardView(
                        item = statCardItems[3],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Trend Chart Cards (Mood -> Sleep -> Purpose -> Lifestyle)
            trendChartItems.forEach { chartItem ->
                BaseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trend_chart_card_${chartItem.key}"),
                    radius = 16.dp, // residual: BaseCard drawing spec
                    padding = 16.dp // residual: BaseCard drawing spec
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.space3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chartItem.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 1.3.sp // residual: chart letterSpacing
                        )
                        Badge(
                            text = "+8% this month",
                            color = chartItem.color,
                            backgroundColor = chartItem.color.copy(alpha = 0.12f),
                            testTag = "badge_${chartItem.key}"
                        )
                    }

                    // Chart
                    AreaTrendChart(
                        values = chartItem.values,
                        color = chartItem.color,
                        labels = activeTrendData.map { it.day },
                        overlayValues = chartItem.overlayValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp), // residual: chart container height
                        testTag = "area_chart_${chartItem.key}"
                    )

                    // X-Axis Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.space1),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        activeTrendData.forEach { point ->
                            Text(
                                text = point.day,
                                style = NumberS,
                                fontSize = 10.sp, // residual: chart text size
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCardView(
    item: StatCardItem,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)) // residual: RoundedCornerShape
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)) // residual: border stroke + RoundedCornerShape
            .padding(spacing.space4)
            .testTag("stat_card_${item.label.lowercase().replace(" ", "_")}")
    ) {
        Column {
            // Top Row: Category Icon left, TrendingUp right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.space2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.color,
                    modifier = Modifier.size(18.dp) // residual: icon intrinsic size
                )
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Trending Up",
                    tint = SuccessColor,
                    modifier = Modifier.size(14.dp) // residual: icon intrinsic size
                )
            }

            // Score Row: Value + Unit (aligned by baseline)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp) // residual: no token for 2
            ) {
                Text(
                    text = item.value,
                    style = NumberXl,
                    fontSize = 30.sp, // residual: chart number size
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = item.unit,
                    style = NumberS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }

            // Label
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp, // residual: chart text size
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.space1)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    MindRestTheme {
        StatisticsScreen(onNavigateBack = {})
    }
}

