package com.example.features.home.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.DisplayFontFamily
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.PrimaryButton

// Quick-mood emoji picker (1→😢 ... 5→😁), reuse pattern dari MoodTrackingScreen.
private val moodEmojis = listOf(
    1 to "😢", // Sangat Buruk
    2 to "🙁", // Buruk
    3 to "😐", // Biasa
    4 to "🙂", // Baik
    5 to "😁"  // Hebat
)

private val moodLabels = mapOf(
    1 to "Sangat Buruk",
    2 to "Buruk",
    3 to "Biasa",
    4 to "Baik",
    5 to "Hebat"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (moodScore: Int) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    // Skor mood yang dipilih (1-5), null = belum ada pilihan.
    var selectedMood by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.testTag("daily_checkin_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER
            Text(
                text = "Check-in Hari Ini",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("checkin_header_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bagaimana perasaanmu hari ini?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. MOOD EMOJI PICKER (1-5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mood_emoji_row"),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodEmojis.forEach { (moodValue, emoji) ->
                    val isSelected = selectedMood == moodValue
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedMood = moodValue }
                                .testTag("mood_emoji_$moodValue"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 32.sp
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = moodLabels[moodValue].orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 3. FOOTER: PRIMARY BUTTON
            PrimaryButton(
                text = "Simpan Mood",
                onClick = {
                    selectedMood?.let { onSave(it) }
                    onDismissRequest()
                },
                enabled = selectedMood != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_mood_button")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DailyCheckInBottomSheetPreview() {
    MindRestTheme {
        DailyCheckInBottomSheet(
            onDismissRequest = {},
            onSave = { }
        )
    }
}
