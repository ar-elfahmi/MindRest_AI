package com.example.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.LocalMotion
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import kotlinx.coroutines.delay

/**
 * Loading shimmer primitive for MindRest.
 *
 * A soft, slow horizontal sweep — tuned to the "calm & breathing" motion band
 * (LocalMotion.durationLong, ease-out). Never flickers; always settles.
 *
 * Two entry points:
 *  - [Modifier.shimmer]        → apply to any composable as a skeleton base
 *  - [ShimmerBox]              → drop-in placeholder box of a given height
 *
 * The animation uses a [remember]ed state advanced via LaunchedEffect so it
 * runs on a coroutine, not a composition thrash.
 */
fun Modifier.shimmer(
    cornerRadius: Dp = 8.dp,
): Modifier = composed {
    val motion = LocalMotion.current
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val highlight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)

    val progress = remember { androidx.compose.runtime.mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            // Sweep across [0,1] then wrap — gentle, not a hard loop.
            val steps = 40
            for (i in 0..steps) {
                progress.value = i.toFloat() / steps
                delay((motion.durationLong.toLong()) / steps)
            }
        }
    }

    this
        .clip(RoundedCornerShape(cornerRadius))
        .drawWithCache {
            val p = progress.value
            val width = size.width
            val sweepWidth = width * 0.5f
            val start = -sweepWidth + (width + sweepWidth * 2) * p
            val brush = Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(start, 0f),
                end = Offset(start + sweepWidth, size.height),
            )
            onDrawBehind { drawRect(brush) }
        }
}

/**
 * Drop-in skeleton box. Height defaults to a text line (~16dp) — pass [height]
 * for title/card skeletons.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
) {
    Box(modifier = modifier.height(height).fillMaxWidth().shimmer())
}

@Preview(name = "LoadingShimmer", showBackground = true)
@Composable
private fun LoadingShimmerPreview() {
    MindRestTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(height = 28.dp)
            ShimmerBox(height = 16.dp)
            ShimmerBox(height = 16.dp, modifier = Modifier.fillMaxWidth(0.7f))
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).shimmer(cornerRadius = 20.dp))
        }
    }
}
