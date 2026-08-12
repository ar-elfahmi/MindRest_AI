package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.MindRestTheme

/**
 * A section title row with an optional trailing action.
 *
 * Use at the top of every content grouping so section rhythm is uniform:
 *   "Mood minggu ini"  ............  "Lihat semua →"
 *
 * Two trailing flavours:
 *  - [actionLabel]      → compact TextButton (e.g. "Lihat semua")
 *  - [actionIcon]       → icon-only IconButton (e.g. settings gear)
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when {
            actionLabel != null && onActionClick != null -> {
                TextButton(onClick = onActionClick) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            actionIcon != null && onActionClick != null -> {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionLabel,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Preview(name = "SectionHeader", showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    MindRestTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(title = "Mood minggu ini")
            SectionHeader(
                title = "Jurnal terbaru",
                actionLabel = "Lihat semua",
                onActionClick = {},
            )
            SectionHeader(
                title = "Statistik",
                subtitle = "7 hari terakhir",
                actionLabel = "Detail",
                onActionClick = {},
            )
        }
    }
}
