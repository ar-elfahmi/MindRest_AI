package com.example.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSecondary,
    onPrimaryContainer = DarkForeground,
    secondary = DarkSecondary,
    onSecondary = DarkForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    error = DarkDestructive,
    onError = Color.White,
    surfaceVariant = DarkMuted,
    onSurfaceVariant = DarkMutedForeground
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSecondary,
    onPrimaryContainer = LightForeground,
    secondary = LightSecondary,
    onSecondary = LightForeground,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightCard,
    onSurface = LightForeground,
    error = LightDestructive,
    onError = Color.White,
    surfaceVariant = LightMuted,
    onSurfaceVariant = LightMutedForeground
)

@Composable
fun MindRestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val spacing = Spacing()
    val elevation = Elevation()
    val shapes = Shapes()
    val motion = Motion()

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalElevation provides elevation,
        LocalShapes provides shapes,
        LocalMotion provides motion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MindRestTypography,
            content = content
        )
    }
}
