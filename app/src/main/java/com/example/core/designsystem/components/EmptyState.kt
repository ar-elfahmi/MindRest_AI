package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme

/**
 * Standard empty / zero-data state.
 *
 * Use whenever a list or screen has nothing to show yet (JournalHistory,
 * Reminder, Notifications). Replaces ad-hoc "no data" columns.
 *
 * Keep copy warm & calm ("Mulai journal pertamamu") — never scolding.
 *
 * @param primaryAction optional CTA slot (e.g. a [PrimaryButton]).
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    primaryAction: (@Composable () -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = spacing.space1),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (primaryAction != null) {
            Box(modifier = Modifier.padding(top = spacing.space2)) {
                primaryAction()
            }
        }
    }
}

@Preview(name = "EmptyState", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MindRestTheme {
        EmptyState(
            icon = Icons.Filled.SentimentSatisfied,
            title = "Belum ada jurnal",
            description = "Mulai tulis refleksi pertamamu hari ini.",
        )
    }
}
