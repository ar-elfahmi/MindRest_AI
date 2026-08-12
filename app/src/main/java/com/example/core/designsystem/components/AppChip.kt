package com.example.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme

/**
 * Brand-styled filter chip.
 *
 * Wraps Material 3 [FilterChip] with the MindRest shape (lg = 20dp pill-ish) and
 * calm colour mapping. Use [AppChipGroup] when you have a list of options.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shapes = LocalShapes.current
    val cs = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        enabled = enabled,
        shape = shapes.lg,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = cs.surface,
            labelColor = cs.onSurfaceVariant,
            selectedContainerColor = cs.primary,
            selectedLabelColor = cs.onPrimary,
            disabledContainerColor = cs.surface.copy(alpha = 0.5f),
            disabledSelectedContainerColor = cs.primary.copy(alpha = 0.5f),
            disabledLeadingIconColor = Color.Unspecified,
            disabledTrailingIconColor = Color.Unspecified,
        ),
        border = null,
    )
}

/**
 * Horizontally-scrolling single-select chip row.
 *
 * For mood categories, lifestyle filters, time ranges. Replaces the ad-hoc
 * Row-of-Button pattern seen in Lifestyle & Mood screens.
 */
@Composable
fun AppChipGroup(
    items: List<String>,
    selectedItem: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
    ) {
        items(items) { item ->
            AppChip(
                label = item,
                selected = item == selectedItem,
                onClick = { onSelect(item) },
            )
        }
    }
}

/**
 * Flow-wrapping multi-select chip group (chips wrap to next line).
 *
 * Use when options don't fit one row (e.g. emotion tags).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppChipFlow(
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        items.forEach { item ->
            AppChip(
                label = item,
                selected = item in selectedItems,
                onClick = { onToggle(item) },
            )
        }
    }
}

@Preview(name = "AppChip", showBackground = true)
@Composable
private fun AppChipPreview() {
    MindRestTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppChipGroup(
                items = listOf("Hari ini", "Minggu", "Bulan", "Tahun"),
                selectedItem = "Minggu",
                onSelect = {},
            )
            AppChipFlow(
                items = listOf("Senang", "Tenang", "Lelah", "Cemas", "Bangga", "Sedih"),
                selectedItems = setOf("Tenang", "Bangga"),
                onToggle = {},
            )
        }
    }
}
