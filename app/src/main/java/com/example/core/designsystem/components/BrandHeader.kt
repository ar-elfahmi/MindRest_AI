package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.DisplayFontFamily
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme

/**
 * Large brand header for screen tops.
 *
 * Uses the serif display family ([DisplayFontFamily] = Lora) for a calm,
 * editorial feel (Calm / Reflectly reference). Pair with [AppScaffold] as the
 * first child of the content column.
 *
 *   BrandHeader(
 *       title = "Halo, Budi",
 *       subtitle = "Bagaimana perasaanmu hari ini?",
 *       trailingIcon = Icons.Filled.Notifications,
 *       onTrailingClick = { ... },
 *   )
 */
@Composable
fun BrandHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.space1),
                )
            }
        }
        if (trailingIcon != null && onTrailingClick != null) {
            IconButton(onClick = onTrailingClick) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview(name = "BrandHeader", showBackground = true)
@Composable
private fun BrandHeaderPreview() {
    MindRestTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BrandHeader(
                title = "Halo, Budi",
                subtitle = "Bagaimana perasaanmu hari ini?",
            )
        }
    }
}
