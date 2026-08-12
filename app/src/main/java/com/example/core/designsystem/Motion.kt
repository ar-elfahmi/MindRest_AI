package com.example.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset

/**
 * Motion tokens for MindRest.
 *
 * Philosophy: "slow & breathing" — calm-premium feel inspired by Headspace / Calm.
 * Transitions settle in (ease-out) rather than pop. Durations sit in the
 * 300–500ms band so motion is felt but never delays a quick micro-session.
 *
 * Scale (ms):
 *   instant 100  — opacity flickers, selection states
 *   short   200  — small component enter/exit
 *   medium  300  — default for most transitions  ← DEFAULT
 *   long    450  — "breathing" screen-level fades
 *   xlong   600  — hero/illustration entrances
 *
 * WCAG note: motion never carries information; reduced-motion users see the
 * same end state via [MotionSpec] defaults which respect accessibility when
 * wired through animate*AsState.
 */
@Immutable
data class Motion(
    val durationInstant: Int = 100,
    val durationShort: Int = 200,
    val durationMedium: Int = 300,
    val durationLong: Int = 450,
    val durationXLong: Int = 600,
    val easingStandard: Easing = MotionEasing.Standard,
    val easingEmphasized: Easing = MotionEasing.Emphasized,
    val easingDecelerate: Easing = MotionEasing.Decelerate,
)

/** Curve presets — Material 3 standard/emphasized easings, all ease-out dominant. */
object MotionEasing {
    val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
}

val LocalMotion = staticCompositionLocalOf { Motion() }

/**
 * Convenience factory: build a [FiniteAnimationSpec] for [IntOffset] (slide)
 * using the medium breathing cadence. Use for screen transitions & sheets.
 */
fun Motion.slideSpec(): FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = durationMedium, easing = easingEmphasized)

/**
 * Gentle settle spring used for small interactive feedback (selected chips,
 * card press). Stiffness is dialed down so it feels soft, not snappy.
 */
fun Motion.settleSpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)
