package com.example.core.designsystem.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.DisplayFontFamily
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.AppCard
import com.example.core.designsystem.components.AppCardVariant
import com.example.core.designsystem.components.AppChipFlow
import com.example.core.designsystem.components.AppChipGroup
import com.example.core.designsystem.components.AppModalBottomSheet
import com.example.core.designsystem.components.BrandHeader
import com.example.core.designsystem.components.EmptyState
import com.example.core.designsystem.components.PrimaryButton
import com.example.core.designsystem.components.SectionHeader
import com.example.core.designsystem.components.ShimmerBox
import com.example.core.designsystem.components.screenEdge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults

/**
 * Interactive gallery of the MindRest design system.
 *
 * Reachable via the deep link `mindrest://designsystem` (dev / QA only).
 * Every token + every core component is rendered here so the visual contract
 * can be reviewed on a device in one screen — light & dark.
 *
 * This screen is infrastructure, not a product feature: it ships in the app
 * but is only reachable through the deep link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemShowcaseScreen(
    onBack: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val spacing = LocalSpacing.current
    var showSheet by remember { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Design System", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = cs.background,
                    titleContentColor = cs.onBackground,
                ),
            )
        },
        containerColor = cs.background,
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .screenEdge(),
            verticalArrangement = Arrangement.spacedBy(spacing.space6),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = spacing.space2,
                bottom = spacing.space12,
            ),
        ) {
            // 1. Brand header (meta: uses the component itself)
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "BrandHeader") }
            item {
                BrandHeader(
                    title = "Halo, Budi",
                    subtitle = "Bagaimana perasaanmu hari ini?",
                    trailingIcon = Icons.Filled.Notifications,
                    onTrailingClick = {},
                )
            }

            // 2. Colours
            item { ShowcaseSection(icon = Icons.Filled.Palette, label = "Color tokens") }
            item { ColorSwatches() }

            // 3. Typography
            item { ShowcaseSection(icon = Icons.Filled.TextFields, label = "Typography") }
            item { TypographySamples() }

            // 4. AppCard variants
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "AppCard variants") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                    AppCardVariant.values().forEach { variant ->
                        AppCard(variant = variant) {
                            Text(
                                "$variant",
                                style = typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (variant == AppCardVariant.Brand) cs.onPrimary else cs.onSurface,
                            )
                            Text(
                                "Body copy untuk kartu $variant.",
                                style = typography.bodySmall,
                                color = if (variant == AppCardVariant.Brand)
                                    cs.onPrimary.copy(alpha = 0.85f) else cs.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // 5. SectionHeader
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "SectionHeader") }
            item {
                SectionHeader(
                    title = "Mood minggu ini",
                    subtitle = "7 hari terakhir",
                    actionLabel = "Lihat semua",
                    onActionClick = {},
                )
            }

            // 6. Chips
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "Chips") }
            item {
                var single by remember { mutableStateOf("Minggu") }
                AppChipGroup(
                    items = listOf("Hari ini", "Minggu", "Bulan", "Tahun"),
                    selectedItem = single,
                    onSelect = { single = it },
                )
            }
            item {
                val tags = remember { setOf("Tenang", "Bangga") }
                var multi by remember { mutableStateOf(tags) }
                AppChipFlow(
                    items = listOf("Senang", "Tenang", "Lelah", "Cemas", "Bangga", "Sedih"),
                    selectedItems = multi,
                    onToggle = {
                        multi = if (it in multi) multi - it else multi + it
                    },
                )
            }

            // 7. Buttons
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "Buttons") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                    PrimaryButton(text = "Primary action", onClick = {})
                    PrimaryButton(text = "Secondary", onClick = {}, small = true)
                }
            }

            // 8. EmptyState
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "EmptyState") }
            item {
                AppCard {
                    EmptyState(
                        icon = Icons.Filled.SentimentSatisfied,
                        title = "Belum ada jurnal",
                        description = "Mulai tulis refleksi pertamamu hari ini.",
                        primaryAction = { PrimaryButton(text = "Tulis sekarang", onClick = {}, small = true) },
                    )
                }
            }

            // 9. Loading shimmer
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "LoadingShimmer") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ShimmerBox(height = 28.dp)
                    ShimmerBox(height = 16.dp)
                    ShimmerBox(height = 16.dp, modifier = Modifier.fillMaxWidth(0.7f))
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(LocalShapes.current.lg))
                }
            }

            // 10. Bottom sheet trigger
            item { ShowcaseSection(icon = Icons.AutoMirrored.Filled.ViewQuilt, label = "AppModalBottomSheet") }
            item {
                PrimaryButton(
                    text = "Buka bottom sheet",
                    leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White) },
                    onClick = { showSheet = true },
                )
            }
        }
    }

    if (showSheet) {
        AppModalBottomSheet(onDismiss = { showSheet = false }) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Daily check-in", style = typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Contoh isi sheet bergaya MindRest — radius xl, drag handle off, padding 24dp.",
                    style = typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                )
                PrimaryButton(text = "Selesai", onClick = { showSheet = false })
            }
        }
    }
}

@Composable
private fun ShowcaseSection(icon: ImageVector, label: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = cs.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColorSwatches() {
    val cs = MaterialTheme.colorScheme
    val swatches = listOf(
        "primary" to cs.primary,
        "onPrimary" to cs.onPrimary,
        "primaryContainer" to cs.primaryContainer,
        "secondary" to cs.secondary,
        "background" to cs.background,
        "surface" to cs.surface,
        "surfaceVariant" to cs.surfaceVariant,
        "onBackground" to cs.onBackground,
        "onSurface" to cs.onSurface,
        "onSurfaceVariant" to cs.onSurfaceVariant,
        "outline" to cs.outline,
        "error" to cs.error,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        swatches.forEach { (name, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color),
                )
                Text(name, style = MaterialTheme.typography.labelMedium, color = cs.onBackground)
            }
        }
    }
}

@Composable
private fun TypographySamples() {
    val typography = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Display Small", style = typography.displaySmall, fontFamily = DisplayFontFamily, color = MaterialTheme.colorScheme.onBackground)
        Text("Headline Medium", style = typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Title Medium", style = typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Body Medium — teks isi untuk paragraf yang nyaman dibaca.", style = typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Label Large", style = typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Label Small", style = typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
