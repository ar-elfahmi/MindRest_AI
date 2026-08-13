package com.example.features.achievements.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.FeatureJournaling
import com.example.core.designsystem.FeatureJourney
import com.example.core.designsystem.FeatureLifestyle
import com.example.core.designsystem.FeatureRelaxation
import com.example.core.designsystem.FeatureReminder
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.NumberL
import com.example.core.designsystem.NumberM
import com.example.core.designsystem.SuccessColor
import com.example.core.designsystem.components.Badge
import com.example.core.designsystem.components.BaseCard
import com.example.core.designsystem.components.SectionLabel
import com.example.core.designsystem.components.TopBar

// Model representing an Achievement Milestone
data class Achievement(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val xpReward: Int,
    val color: Color,
    val steps: List<String> = emptyList()
)

val sampleAchievements = listOf(
    Achievement(
        id = "streak_7",
        title = "7-Day Night Owl Streak",
        category = "Sleep",
        emoji = "🌙",
        description = "Maintain a bedtime routine and log sleep for 7 consecutive days.",
        currentProgress = 7,
        maxProgress = 7,
        isUnlocked = true,
        unlockedDate = "Unlocked Jul 20",
        xpReward = 200,
        color = Color(0xFF7C72F5), // residual: matches colorScheme.primary but in data class (cannot reference MaterialTheme)
        steps = listOf(
            "Log sleep timing daily",
            "Keep bedtime variance within 30 mins",
            "Reach 7 consecutive logged days"
        )
    ),
    Achievement(
        id = "cbt_master",
        title = "CBT-I Master",
        category = "Mindfulness",
        emoji = "🧠",
        description = "Complete 10 Cognitive Behavioral Therapy journal reflection sessions.",
        currentProgress = 10,
        maxProgress = 10,
        isUnlocked = true,
        unlockedDate = "Unlocked Jul 22",
        xpReward = 250,
        color = FeatureJournaling,
        steps = listOf(
            "Identify sleep anxiety triggers",
            "Complete automatic thought restructuring",
            "Finish 10 total CBT-I journal entries"
        )
    ),
    Achievement(
        id = "deep_sleeper",
        title = "Deep Rest Pioneer",
        category = "Sleep",
        emoji = "💤",
        description = "Achieve over 2 hours of deep sleep stage in a single night.",
        currentProgress = 2,
        maxProgress = 2,
        isUnlocked = true,
        unlockedDate = "Unlocked Jul 25",
        xpReward = 300,
        color = FeatureJourney,
        steps = listOf(
            "Prepare room lighting and temperature",
            "Avoid screens 45m before bed",
            "Log 2.0+ hours of deep sleep stage"
        )
    ),
    Achievement(
        id = "ikigai_seeker",
        title = "Ikigai Architect",
        category = "Ikigai",
        emoji = "✨",
        description = "Fill out all 4 Ikigai purpose pillars in your personal roadmap.",
        currentProgress = 4,
        maxProgress = 4,
        isUnlocked = true,
        unlockedDate = "Unlocked Jul 26",
        xpReward = 350,
        color = FeatureLifestyle,
        steps = listOf(
            "Define What You Love",
            "List What You're Good At",
            "Identify World Needs and Career Value"
        )
    ),
    Achievement(
        id = "early_bird",
        title = "Consistent Sunrise Rest",
        category = "Consistency",
        emoji = "🌅",
        description = "Wake up within 15 minutes of target alarm for 14 days.",
        currentProgress = 11,
        maxProgress = 14,
        isUnlocked = false,
        xpReward = 400,
        color = FeatureRelaxation,
        steps = listOf(
            "Set target wake time at 07:00 AM",
            "Dismiss alarm on first prompt",
            "Maintain 14 consecutive mornings (11/14 complete)"
        )
    ),
    Achievement(
        id = "digital_detox",
        title = "Digital Detox Champion",
        category = "Mindfulness",
        emoji = "📱",
        description = "Turn off screen interaction 1 hour before bedtime 5 times.",
        currentProgress = 3,
        maxProgress = 5,
        isUnlocked = false,
        xpReward = 150,
        color = FeatureReminder,
        steps = listOf(
            "Enable Wind Down mode",
            "Avoid night time social media",
            "Reach 5 successful detox evenings (3/5 complete)"
        )
    ),
    Achievement(
        id = "marathon_sleeper",
        title = "30-Day Rest Guardian",
        category = "Consistency",
        emoji = "🛌",
        description = "Log sleep quality continuously for 30 full days.",
        currentProgress = 18,
        maxProgress = 30,
        isUnlocked = false,
        xpReward = 500,
        color = Color(0xFFE8845C), // residual: achievement coral color, no token
        steps = listOf(
            "Maintain daily sleep logs",
            "Synchronize sleep stats without missing a day",
            "Complete 30 consecutive days (18/30 complete)"
        )
    ),
    Achievement(
        id = "zen_meditator",
        title = "Zen Audio Explorer",
        category = "Mindfulness",
        emoji = "🧘",
        description = "Listen to over 100 minutes of relaxation ambient sounds.",
        currentProgress = 85,
        maxProgress = 100,
        isUnlocked = false,
        xpReward = 200,
        color = FeatureJournaling,
        steps = listOf(
            "Explore soundscapes and breathing tracks",
            "Complete audio listening sessions",
            "Accumulate 100 total minutes (85/100 mins complete)"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedAchievementForDetail by remember { mutableStateOf<Achievement?>(null) }

    val categories = listOf("All", "Sleep", "Consistency", "Mindfulness", "Ikigai")

    val filteredAchievements = remember(selectedCategory) {
        if (selectedCategory == "All") sampleAchievements
        else sampleAchievements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val unlockedCount = remember { sampleAchievements.count { it.isUnlocked } }
    val totalCount = sampleAchievements.size
    val totalXp = remember { sampleAchievements.filter { it.isUnlocked }.sumOf { it.xpReward } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("achievements_screen")
    ) {
        // 1. TopBar
        TopBar(
            title = "Milestones & Badges",
            onBackClick = onNavigateBack,
            actionSlot = {
                Badge(
                    text = "$unlockedCount / $totalCount Unlocked",
                    color = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            },
            testTag = "achievements_top_bar"
        )

        // 2. Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal)
                .padding(bottom = spacing.space6),
            verticalArrangement = Arrangement.spacedBy(spacing.space4)
        ) {
            Spacer(modifier = Modifier.height(spacing.space1))

            // Hero Rest XP & Level Header Card
            AchievementHeroCard(
                unlockedCount = unlockedCount,
                totalCount = totalCount,
                totalXp = totalXp
            )

            // Category Filter Pills
            SectionLabel(text = "Categories", testTag = "category_section_label")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievement_category_pills")
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = spacing.space4, vertical = spacing.space2)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Achievements Grid / List
            SectionLabel(text = "Badges ($unlockedCount Unlocked)", testTag = "badges_section_label")
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.space3),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredAchievements.forEach { achievement ->
                    AchievementItemCard(
                        achievement = achievement,
                        onClick = { selectedAchievementForDetail = achievement }
                    )
                }
            }
        }
    }

    // Detail BottomSheet when user taps an achievement
    selectedAchievementForDetail?.let { achievement ->
        AchievementDetailModal(
            achievement = achievement,
            onDismiss = { selectedAchievementForDetail = null }
        )
    }
}

@Composable
private fun AchievementHeroCard(
    unlockedCount: Int,
    totalCount: Int,
    totalXp: Int
) {
    val spacing = LocalSpacing.current
    val progress = unlockedCount.toFloat() / totalCount.coerceAtLeast(1)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "hero_progress"
    )

    BaseCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_hero_card"),
        radius = 20.dp, // residual: BaseCard intrinsic radius
        padding = 18.dp // residual: no token for 18
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space3)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // residual: icon container size
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFFEB845C) // residual: achievement coral color, no token
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp) // residual: icon size
                        )
                    }

                    Column {
                        Text(
                            text = "Rest Master • Level 4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalXp Rest XP Earned",
                            style = NumberM,
                            fontSize = 13.sp, // residual: no typography token
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp)) // residual: no shape token for 12
                        .background(SuccessColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp) // residual: no token for 10/6
                ) {
                    Text(
                        text = "Top 5% Rest",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.space4))

            // Overall Milestone Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Milestone Completion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp)) // residual: no token for 6

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp) // residual: progress bar height
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(10.dp)) // residual: no token for 10

            Text(
                text = "Keep consistent bedtime habits to unlock the Golden Owl badge!",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp, // residual: no typography token
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementItemCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val isUnlocked = achievement.isUnlocked
    val pct = achievement.currentProgress.toFloat() / achievement.maxProgress.coerceAtLeast(1)

    BaseCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("achievement_item_${achievement.id}"),
        radius = 16.dp, // residual: BaseCard intrinsic radius
        padding = 14.dp // residual: no token for 14
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp) // residual: no token for 14
        ) {
            // Emoji Box Badge Icon
            Box(
                modifier = Modifier
                    .size(56.dp) // residual: icon container size
                    .clip(RoundedCornerShape(16.dp)) // residual: shape
                    .background(
                        if (isUnlocked) achievement.color.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp, // residual: border stroke
                        if (isUnlocked) achievement.color.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp) // residual: shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.emoji,
                    fontSize = 28.sp, // residual: no typography token
                    modifier = Modifier.padding(2.dp) // residual: no token for 2
                )
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp) // residual: icon size
                        )
                    }
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp)) // residual: no token for 6

                    if (isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space1)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessColor,
                                modifier = Modifier.size(14.dp) // residual: icon size
                            )
                            Text(
                                text = "Unlocked",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: no typography token
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                    } else {
                        Text(
                            text = "+${achievement.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp, // residual: no typography token
                            fontWeight = FontWeight.Bold,
                            color = achievement.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp)) // residual: no token for 2

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp, // residual: no typography token
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp // residual: no typography token
                )

                Spacer(modifier = Modifier.height(spacing.space2))

                // Progress Bar or Unlocked Timestamp
                if (isUnlocked) {
                    Text(
                        text = achievement.unlockedDate ?: "Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp, // residual: no typography token
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: no typography token
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${achievement.currentProgress} / ${achievement.maxProgress}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp, // residual: no typography token
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.space1))
                        LinearProgressIndicator(
                            progress = { pct.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp) // residual: progress bar height
                                .clip(CircleShape),
                            color = achievement.color,
                            trackColor = achievement.color.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AchievementDetailModal(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space6)
                .testTag("achievement_detail_modal"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Animated Visual Badge
            Box(
                modifier = Modifier
                    .size(80.dp) // residual: icon container size
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                achievement.color,
                                achievement.color.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.emoji, fontSize = 40.sp) // residual: no typography token
            }

            Spacer(modifier = Modifier.height(spacing.space4))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.space1))

            Badge(
                text = achievement.category.uppercase(),
                color = achievement.color,
                backgroundColor = achievement.color.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(spacing.space3))

            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.space5))

            // Checklist Steps
            BaseCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 16.dp, // residual: BaseCard intrinsic radius
                padding = 16.dp // residual: BaseCard intrinsic padding
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { // residual: no token for 10
                    Text(
                        text = "Milestone Criteria",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    achievement.steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp) // residual: no token for 10
                        ) {
                            val isStepDone = achievement.isUnlocked || index < achievement.currentProgress
                            Icon(
                                imageVector = if (isStepDone) Icons.Default.CheckCircle else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isStepDone) SuccessColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp) // residual: icon size
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isStepDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.space6))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.space3)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp) // residual: no shape token for 14
                ) {
                    Text("Close")
                }

                Button(
                    onClick = { /* Share functionality mock */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp), // residual: no shape token for 14
                    colors = ButtonDefaults.buttonColors(containerColor = achievement.color)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp) // residual: icon size
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // residual: no token for 6
                    Text("Share Badge")
                }
            }

            Spacer(modifier = Modifier.height(spacing.space4))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AchievementsScreenLightPreview() {
    MindRestTheme(darkTheme = false) {
        AchievementsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AchievementsScreenDarkPreview() {
    MindRestTheme(darkTheme = true) {
        AchievementsScreen()
    }
}
