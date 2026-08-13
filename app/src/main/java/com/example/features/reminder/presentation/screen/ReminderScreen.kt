package com.example.features.reminder.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.*
import com.example.core.designsystem.components.*
import com.example.features.reminder.BedtimeNotificationHelper
import com.example.features.reminder.presentation.viewmodel.ReminderViewModel
import kotlin.math.max
import kotlin.math.min

/**
 * ReminderScreen — T-009 (FR-016).
 *
 * Scoping per task T-009:
 *  - **Bedtime reminder card** (top): TimePicker (jam + menit) + Save/Cancel
 *    buttons + current state display. Wiring ke [ReminderViewModel]
 *    → DataStore → [BedtimeNotificationHelper].
 *  - **Permission flow**: POST_NOTIFICATIONS launcher (API 33+).
 *  - **Routine reminders card** (bottom): toggle UI-only, JANGAN di-schedule —
 *    di luar scope T-009 (saat ini hanya render hardcoded copy, fungsionalitas
 *    scheduling belum jadi FR terpisah).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = viewModel()
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val state by viewModel.uiState.collectAsState()

    // Existing daily-routine toggles tetap `remember`-only (offline,
    // belum terhubung ke alarm — di luar scope T-009).
    var isMorningCheckInEnabled by remember { mutableStateOf(true) }
    var isMiddayResetEnabled by remember { mutableStateOf(false) }
    var isEveningJournalEnabled by remember { mutableStateOf(true) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
            BedtimeNotificationHelper.scheduleBedtimeNotification(context)
        } else {
            Toast.makeText(context, "Permission denied. Notifications will not show.", Toast.LENGTH_LONG).show()
        }
    }

    AppScaffold(
        topBar = {
            TopBar(
                title = "Reminder Settings",
                onBackClick = onNavigateBack,
                testTag = "reminder_top_bar"
            )
        },
        modifier = modifier.fillMaxSize().testTag("reminder_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space5)
        ) {
            // Permission Banner (Android 13+ only)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                BaseCard(
                    radius = 16.dp, // residual: BaseCard intrinsic radius
                    padding = 16.dp, // residual: BaseCard intrinsic padding
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp) // residual: shape
                    ),
                    testTag = "permission_card"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(spacing.space2))
                            Text(
                                text = "Notification Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = "To receive wind-down prompts 30 minutes before bedtime, please allow notifications for MindRest.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        PrimaryButton(
                            text = "Grant Permission",
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth().height(36.dp) // residual: button height
                        )
                    }
                }
            }

            // === 1. Bedtime Wind-Down Notification Card ===
            BaseCard(
                radius = 20.dp, // residual: BaseCard intrinsic radius
                padding = 16.dp, // residual: BaseCard intrinsic padding
                testTag = "bedtime_scheduler_card"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { // residual: no token for 14
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp) // residual: icon container size
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp) // residual: shape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(spacing.space3))
                            Column {
                                Text(
                                    text = "Bedtime Wind-Down Prompt",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "30 mins before ideal bedtime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status badge
                        if (state.isEnabled) {
                            Badge(
                                text = "Aktif",
                                color = SuccessColor,
                                backgroundColor = SuccessColor.copy(alpha = 0.15f)
                            )
                        } else {
                            Badge(
                                text = "Nonaktif",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Time picker (jam + menit)
                    Text(
                        text = "Waktu Tidur Ideal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TimeStepperRow(
                        hour = state.hour,
                        minute = state.minute,
                        onHourChange = viewModel::onHourChange,
                        onMinuteChange = viewModel::onMinuteChange,
                        modifier = Modifier.fillMaxWidth().testTag("time_stepper_row")
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space3)
                    ) {
                        PrimaryButton(
                            text = "Simpan Pengingat",
                            onClick = {
                                if (!hasNotificationPermission &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setReminderTime(state.hour, state.minute)
                                    Toast.makeText(context, "Pengingat disimpan.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("save_reminder_btn")
                        )
                        if (state.isEnabled) {
                            SecondaryButton(
                                text = "Matikan",
                                onClick = {
                                    viewModel.cancelReminder()
                                    Toast.makeText(context, "Pengingat dimatikan.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("cancel_reminder_btn")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Schedule breakdown (read-only)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ideal Bedtime Target",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(state.hour, state.minute),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Notification Scheduled",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.isEnabled) {
                                    "${formatTime(state.hour, state.minute, subtractMinutes = 30)} Daily"
                                } else {
                                    "Disabled"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isEnabled) SuccessColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Chip (when enabled)
                    if (state.isEnabled) {
                        Badge(
                            text = "⏰ Active: Alarm scheduled ${formatTime(state.hour, state.minute, subtractMinutes = 30)} daily",
                            color = SuccessColor,
                            backgroundColor = SuccessColor.copy(alpha = 0.15f)
                        )
                    }

                    // Test Action Button
                    SecondaryButton(
                        text = "Trigger Test Notification Now",
                        onClick = {
                            if (!hasNotificationPermission &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                BedtimeNotificationHelper.triggerTestNotification(context)
                                Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("test_notification_btn")
                    )
                }
            }

            // === 2. Additional Mindful Routine Reminders (out of scope T-009) ===
            BaseCard(
                radius = 20.dp, // residual: BaseCard intrinsic radius
                padding = 16.dp, // residual: BaseCard intrinsic padding
                testTag = "routine_reminders_card"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                    SectionLabel(text = "Daily Routine Reminders")

                    ToggleRow(
                        title = "Morning Mindful Check-in (07:30 AM)",
                        subtitle = "Gentle alert to record mood and intentions upon waking",
                        checked = isMorningCheckInEnabled,
                        onCheckedChange = { isMorningCheckInEnabled = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ToggleRow(
                        title = "Midday Reset & Breathing (01:00 PM)",
                        subtitle = "Prompt for a 3-minute diaphragmatic breathing pause",
                        checked = isMiddayResetEnabled,
                        onCheckedChange = { isMiddayResetEnabled = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ToggleRow(
                        title = "Evening Reflection & Journal (09:00 PM)",
                        subtitle = "CBT-based journaling prompt to clear thoughts before sleep",
                        checked = isEveningJournalEnabled,
                        onCheckedChange = { isEveningJournalEnabled = it }
                    )
                }
            }
        }
    }
}

/**
 * Compact hour + minute stepper (Material3).
 * Menggunakan IconButton +/- (instead of full TimePicker wheel) supaya
 * card tidak terlalu tinggi (inline-scrollable). Pola paralel dengan
 * existing design system.
 */
@Composable
private fun TimeStepperRow(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jam
        TimeStepperColumn(
            label = "Jam",
            value = hour,
            onIncrement = { onHourChange((hour + 1) % 24) },
            onDecrement = { onHourChange((hour + 23) % 24) },
            modifier = Modifier.weight(1f).testTag("hour_stepper")
        )

        // Colon separator
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Menit
        TimeStepperColumn(
            label = "Menit",
            value = minute,
            onIncrement = { onMinuteChange(min(minute + 1, 59)) },
            onDecrement = { onMinuteChange(max(minute - 1, 0)) },
            modifier = Modifier.weight(1f).testTag("minute_stepper")
        )
    }
}

@Composable
private fun TimeStepperColumn(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space1)
    ) {
        // Up button
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(36.dp) // residual: icon button size
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                )
                .testTag("${label}_up")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Tambah $label"
            )
        }
        // Value display
        Surface(
            shape = RoundedCornerShape(12.dp), // residual: shape
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().height(48.dp) // residual: value display height
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "%02d".format(value),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        // Down button
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(36.dp) // residual: icon button size
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                )
                .testTag("${label}_down")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Kurangi $label"
            )
        }
    }
}

/** Format integer hour:minute ke "22:15" (24h). */
private fun formatTime(hour: Int, minute: Int, subtractMinutes: Int = 0): String {
    var h = hour
    var m = minute - subtractMinutes
    if (m < 0) {
        m += 60
        h = (h - 1 + 24) % 24
    }
    return "%02d:%02d".format(h, m)
}

@Preview(showBackground = true)
@Composable
fun ReminderPreview() {
    MindRestTheme {
        ReminderScreen(onNavigateBack = {})
    }
}
