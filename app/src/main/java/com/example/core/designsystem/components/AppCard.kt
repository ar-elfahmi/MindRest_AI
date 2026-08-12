package com.example.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.LocalElevation
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme

/** Visual variants for [AppCard]. Pick by intent, not by mood. */
enum class AppCardVariant {
    /** Filled surface — default content container. */
    Tonal,

    /** Hairline border, no shadow — for dense lists / settings rows. */
    Outlined,

    /** Elevated surface with soft shadow — for primary / hero content. */
    Elevated,

    /** Subtle primary gradient — for brand moments (welcome, Ikigai hero). */
    Brand,
}

/**
 * The canonical card container for MindRest.
 *
 * Contract:
 *  - radius        = shapes.lg (20.dp)
 *  - inner padding = spacing.cardPadding (20.dp)
 *  - elevation     = LocalElevation.xs by default (calm, not floating)
 *
 * Pass [onClick] to make it interactive (ripple + Role.Button). Otherwise it
 * renders as a static surface.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AppCardVariant = AppCardVariant.Tonal,
    contentPadding: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shapes = LocalShapes.current
    val spacing = LocalSpacing.current
    val elevation = LocalElevation.current
    val cs = MaterialTheme.colorScheme

    val shape = shapes.lg
    val isBrand = variant == AppCardVariant.Brand
    val contentColor = if (isBrand) cs.onPrimary else cs.onSurface

    val border: BorderStroke? = when (variant) {
        AppCardVariant.Outlined -> BorderStroke(1.dp, cs.outline.copy(alpha = 0.3f))
        else -> null
    }
    val shadowElevation: Dp = when (variant) {
        AppCardVariant.Tonal -> elevation.xs
        AppCardVariant.Elevated, AppCardVariant.Brand -> elevation.sm
        AppCardVariant.Outlined -> 0.dp
    }
    val background: Brush = if (isBrand) {
        Brush.linearGradient(listOf(cs.primary, cs.primary.copy(alpha = 0.82f)))
    } else {
        Brush.linearGradient(listOf(cs.surface, cs.surface))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val clickable = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            role = Role.Button,
            onClick = onClick,
        )
    } else Modifier

    Column(
        modifier = modifier
            .clip(shape)
            .shadow(shadowElevation, shape, clip = false)
            .then(clickable)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(Modifier.background(background, shape))
            .padding(contentPadding ?: spacing.cardPadding),
        content = content,
    )
}

// region Previews
@Preview(name = "AppCard — Tonal", showBackground = true)
@Composable
private fun AppCardTonalPreview() = AppCardPreviewTemplate(AppCardVariant.Tonal)

@Preview(name = "AppCard — Outlined", showBackground = true)
@Composable
private fun AppCardOutlinedPreview() = AppCardPreviewTemplate(AppCardVariant.Outlined)

@Preview(name = "AppCard — Elevated", showBackground = true)
@Composable
private fun AppCardElevatedPreview() = AppCardPreviewTemplate(AppCardVariant.Elevated)

@Preview(name = "AppCard — Brand", showBackground = true)
@Composable
private fun AppCardBrandPreview() = AppCardPreviewTemplate(AppCardVariant.Brand)

@Composable
private fun AppCardPreviewTemplate(variant: AppCardVariant) {
    MindRestTheme {
        AppCard(variant = variant) {
            Text(
                text = "$variant card",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Body copy describing the card content.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
// endregion
