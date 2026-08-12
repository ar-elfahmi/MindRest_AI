package com.example.features.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.FeatureJournaling
import com.example.core.designsystem.FeatureJourney
import com.example.core.designsystem.FeatureLifestyle
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.*
import com.example.features.profile.presentation.state.ProfileUiState
import com.example.features.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val state by profileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // T-007 (FR-003): muat profile user saat screen pertama tampil.
    LaunchedEffect(Unit) { profileViewModel.load() }

    // Snackbar untuk info (sukses) + error
    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            profileViewModel.onInfoMessageShown()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            profileViewModel.onErrorShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("profile_screen")
        ) {
            // 1. Non-scrolling TopBar with Settings Action Slot
            TopBar(
                title = "Profile",
                onBackClick = onBackClick,
                actionSlot = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .clickable(onClick = onNavigateToSettings)
                            .padding(8.dp)
                            .testTag("topbar_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                testTag = "profile_top_bar"
            )

            // 2. Scrollable Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("profile_scroll_content")
            ) {
                // Hero Profile Header — T-007: pakai state riil, fallback ke
                // hardcoded name kalau profile belum loaded (loading state).
                ProfileHeader(
                    name = state.profile?.displayName?.takeIf { it.isNotBlank() }
                        ?: state.draftFullName.takeIf { it.isNotBlank() }
                        ?: "User",
                    email = state.email.ifBlank { "— not signed in —" },
                    emoji = "🧘",
                    premium = true,
                    testTag = "profile_header"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                        .testTag("profile_content_section"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 3. StatsGrid (3 columns)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_stats_grid"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatTile(
                            value = "14",
                            label = "Sleep Streak days",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_tile_sleep_streak"
                        )
                        StatTile(
                            value = "Lv.3",
                            label = "Purpose Level ",
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_tile_purpose_level"
                        )
                        StatTile(
                            value = "42",
                            label = "Journal Entries total",
                            icon = Icons.Default.MenuBook,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_tile_journal_entries"
                        )
                    }

                    // 4. AchievementsCard
                    BaseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToAchievements)
                            .testTag("achievements_card"),
                        radius = 16.dp,
                        padding = 16.dp
                    ) {
                        SectionLabel(
                            text = "Achievements",
                            testTag = "achievements_section_label"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("achievements_grid"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AchievementBadge(
                                emoji = "🌙",
                                label = "7-Day Streak",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                testTag = "achievement_streak"
                            )
                            AchievementBadge(
                                emoji = "🧠",
                                label = "CBT Master",
                                color = FeatureJournaling,
                                modifier = Modifier.weight(1f),
                                testTag = "achievement_cbt"
                            )
                            AchievementBadge(
                                emoji = "💤",
                                label = "Deep Sleeper",
                                color = FeatureJourney,
                                modifier = Modifier.weight(1f),
                                testTag = "achievement_sleep"
                            )
                            AchievementBadge(
                                emoji = "✨",
                                label = "Ikigai Seeker",
                                color = FeatureLifestyle,
                                modifier = Modifier.weight(1f),
                                testTag = "achievement_ikigai"
                            )
                        }
                    }

                    // 5. EditProfileCard (T-007 / FR-003 — baru)
                    // Form untuk edit display_name (= "full name" di UI) +
                    // date_of_birth. Tombol Save enabled hanya kalau ada perubahan.
                    EditProfileCard(
                        state = state,
                        onFullNameChange = profileViewModel::onFullNameChange,
                        onDateOfBirthChange = profileViewModel::onDateOfBirthChange,
                        onEditToggle = profileViewModel::onEditModeChange,
                        onSave = profileViewModel::saveProfile,
                    )

                    // 6. Settings Rows (5 items)
                    SettingsRow(
                        label = "App Settings",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToSettings,
                        testTag = "settings_row_app_settings"
                    )
                    SettingsRow(
                        label = "Language",
                        icon = Icons.Default.Language,
                        onClick = {},
                        testTag = "settings_row_language"
                    )
                    SettingsRow(
                        label = "Privacy",
                        icon = Icons.Default.Security,
                        onClick = {},
                        testTag = "settings_row_privacy"
                    )
                    SettingsRow(
                        label = "Export Data",
                        icon = Icons.Default.GetApp,
                        onClick = {},
                        testTag = "settings_row_export"
                    )
                    SettingsRow(
                        label = "About MindRest",
                        icon = Icons.Default.Info,
                        onClick = {},
                        testTag = "settings_row_about"
                    )

                    // 7. SignOutButton — T-007: trigger dialog konfirmasi dulu
                    // sebelum panggil onSignOut (wired ke MainActivity).
                    SignOutRow(
                        onClick = { profileViewModel.onShowSignOutDialog() },
                        testTag = "sign_out_button"
                    )
                }
            }
        }

        // Snackbar host (overlay, tidak menggangu layout)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // T-007: Dialog konfirmasi sebelum logout.
    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { profileViewModel.onDismissSignOutDialog() },
            title = {
                Text(
                    text = "Yakin ingin keluar?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Anda akan keluar dari akun dan kembali ke halaman login. " +
                        "Data Anda tetap tersimpan dan bisa diakses setelah sign in kembali."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.onDismissSignOutDialog()
                        onSignOut()
                    },
                ) {
                    Text(
                        "Keluar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { profileViewModel.onDismissSignOutDialog() }) {
                    Text("Batal")
                }
            },
        )
    }
}

/**
 * Field read-only untuk display nilai profil (saat bukan edit mode).
 * Visual style mirip TextInputField tapi non-interaktif.
 */
@Composable
private fun ReadOnlyField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    testTag: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Card untuk edit profil (FR-003). Berisi:
 * - Tombol "Edit" / "Batal" toggle edit mode
 * - TextField full name (= display_name)
 * - Row tanggal lahir dengan DatePicker
 * - Tombol "Simpan" enabled hanya saat ada perubahan
 *
 * Pola disusun paralel dengan EditProfileCard dari auth flow MVP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileCard(
    state: ProfileUiState,
    onFullNameChange: (String) -> Unit,
    onDateOfBirthChange: (String?) -> Unit,
    onEditToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.draftDateOfBirth?.toEpochMillis()
    )

    BaseCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_profile_card"),
        radius = 16.dp,
        padding = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionLabel(
                text = "Profile Details",
                testTag = "edit_profile_section_label",
            )
            TextButton(
                onClick = { onEditToggle(!state.isEditMode) },
                modifier = Modifier.testTag("edit_profile_toggle"),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isEditMode) "Batal" else "Edit",
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full name field — read-only display saat tidak edit mode,
        // TextInputField interaktif saat edit mode.
        if (state.isEditMode) {
            TextInputField(
                value = state.draftFullName,
                onValueChange = onFullNameChange,
                placeholder = "Nama lengkap",
                leadingIcon = Icons.Default.Person,
                error = if (state.draftFullName.isBlank()) {
                    "Nama tidak boleh kosong"
                } else null,
                testTag = "profile_full_name_input",
            )
        } else {
            ReadOnlyField(
                icon = Icons.Default.Person,
                label = "Nama lengkap",
                value = state.draftFullName.ifBlank { "Belum diisi" },
                testTag = "profile_full_name_display",
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date of birth row (read-only display + tap to open DatePicker)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(14.dp),
                )
                .clickable(enabled = state.isEditMode) { showDatePicker = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("profile_date_of_birth_row"),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tanggal Lahir",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.draftDateOfBirth?.formatDisplayDate()
                            ?: "Belum diisi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.draftDateOfBirth == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
                if (state.isEditMode && state.draftDateOfBirth != null) {
                    TextButton(
                        onClick = { onDateOfBirthChange(null) },
                        modifier = Modifier.testTag("profile_clear_dob"),
                    ) {
                        Text("Hapus", fontSize = 12.sp)
                    }
                }
            }
        }

        if (state.isEditMode) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = if (state.isSaving) "Menyimpan..." else "Simpan",
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_save_button"),
                enabled = state.hasChanges && !state.isSaving &&
                    state.draftFullName.isNotBlank(),
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateOfBirthChange(millis.toIsoDateString())
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** "YYYY-MM-DD" → epoch millis di UTC midnight (untuk DatePicker). */
private fun String.toEpochMillis(): Long? = try {
    val parts = this.split("-")
    if (parts.size != 3) null
    else {
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        val d = parts[2].toInt()
        // Compose's DatePicker expects UTC midnight millis for the selected day
        java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(y, m - 1, d, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
} catch (e: Exception) {
    null
}

/** Epoch millis → "YYYY-MM-DD" (ISO date untuk kolom DATE di Postgres). */
private fun Long.toIsoDateString(): String {
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = this@toIsoDateString
    }
    val y = cal.get(java.util.Calendar.YEAR)
    val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$y-$m-$d"
}

/** "YYYY-MM-DD" → "DD MMM YYYY" untuk display di UI (locale-neutral). */
private fun String.formatDisplayDate(): String = try {
    val parts = this.split("-")
    if (parts.size != 3) this
    else {
        val months = listOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        val y = parts[0]
        val m = months.getOrNull(parts[1].toInt() - 1) ?: parts[1]
        val d = parts[2].toInt().toString()
        "$d $m $y"
    }
} catch (e: Exception) {
    this
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenLightPreview() {
    MindRestTheme(darkTheme = false) {
        ProfileScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenDarkPreview() {
    MindRestTheme(darkTheme = true) {
        ProfileScreen()
    }
}