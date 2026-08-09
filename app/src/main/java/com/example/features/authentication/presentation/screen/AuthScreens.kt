package com.example.features.authentication.presentation.screen

import android.content.Context
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.MoonLogo
import com.example.core.designsystem.components.PrimaryButton
import com.example.core.designsystem.components.TextInputField
import com.example.core.designsystem.components.PasswordInputField
import com.example.features.authentication.presentation.viewmodel.LoginViewModel
import com.example.features.authentication.presentation.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MailOutline

// Helper class to represent a star particle
private data class StarParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val duration: Int,
    val delay: Int
)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Check if the user prefers reduced motion (system settings)
    val isReducedMotion = remember {
        try {
            val scale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    // Check dark mode based on background theme or system theme
    // We can also infer from material theme's active background color
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f // DarkBackground is very dark, LightBackground is warm ivory (red > 0.9)

    // Background Gradient Brushes
    val backgroundBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF090C1A), // DarkBackground
                    Color(0xFF1A1040), // Deep Purple midnight (page-unique)
                    Color(0xFF0D1A2E)  // Dark Navy (page-unique)
                ),
                start = Offset(0.9f, 0.1f), // Tilted linear gradient
                end = Offset(0.1f, 0.9f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFAF8F3), // LightBackground
                    Color(0xFFF0EDF9)  // LightSecondary (Light warm ivory bleeding into soft lavender)
                ),
                start = Offset(0f, 0f),
                end = Offset(1f, 1f)
            )
        }
    }

    // Generate 30 star particles for Dark mode only
    val stars = remember {
        val random = java.util.Random(42) // Constant seed for deterministic positioning across recompositions
        List(30) {
            StarParticle(
                x = random.nextFloat(),
                y = random.nextFloat() * 0.7f, // Limit to top 70% of the screen
                size = 1f + random.nextFloat() * 2f, // 1dp to 3dp
                duration = 1500 + random.nextInt(2000), // 1.5s to 3.5s
                delay = random.nextInt(2000)
            )
        }
    }

    // Twinkle Animation Factor
    val infiniteTransition = rememberInfiniteTransition(label = "stars_twinkle")
    val twinkleFactor by if (isReducedMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "twinkle"
        )
    }

    // Staggered Shimmer Animation for Progress Dots
    val dotTime by if (isReducedMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dotTime"
        )
    }

    // Dot Opacities
    val opacity1 = if (isReducedMotion) 1f else {
        val t1 = (dotTime / 1000f) * 2f * Math.PI
        0.8f + 0.2f * Math.cos(t1).toFloat()
    }
    val opacity2 = if (isReducedMotion) 0.4f else {
        val t2 = ((dotTime - 300f) / 1000f) * 2f * Math.PI
        0.5f + 0.2f * Math.cos(t2).toFloat()
    }
    val opacity3 = if (isReducedMotion) 0.25f else {
        val t3 = ((dotTime - 600f) / 1000f) * 2f * Math.PI
        0.35f + 0.15f * Math.cos(t3).toFloat()
    }

    // Entrance Animation State
    val contentAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val contentOffsetY = remember { Animatable(if (isReducedMotion) 0f else 8f) }

    LaunchedEffect(isReducedMotion) {
        if (!isReducedMotion) {
            launch {
                contentAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
            launch {
                contentOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Skip/Tap-to-skip & Auto-Advance logic
    var isTapEnabled by remember { mutableStateOf(false) }
    var isNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isTapEnabled = true
        delay(2300) // Remaining time to reach 2500ms
        if (!isNavigated) {
            isNavigated = true
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isTapEnabled && !isNavigated) {
                    isNavigated = true
                    onNavigateToOnboarding()
                }
            }
    ) {
        // 1. Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        )

        // Render ambient elements only in dark mode
        if (isDark) {
            // 2. Stars Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                stars.forEach { star ->
                    val opacity = if (isReducedMotion) 0.3f else {
                        val individualProgress = twinkleFactor * (3500f / star.duration) + (star.delay / 1000f)
                        0.3f + 0.6f * (Math.sin(individualProgress.toDouble()).toFloat() + 1f) / 2f
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = opacity),
                        radius = star.size.dp.toPx(),
                        center = Offset(star.x * size.width, star.y * size.height)
                    )
                }
            }

            // 3. Aurora Glow Blobs
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Aurora Blob 1 (Top-Right) - Primary Violet Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C72F5).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.15f),
                        radius = 200.dp.toPx()
                    ),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.9f, size.height * 0.15f)
                )

                // Aurora Blob 2 (Bottom-Left) - Accent Teal Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4ECDC4).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.85f),
                        radius = 160.dp.toPx()
                    ),
                    radius = 160.dp.toPx(),
                    center = Offset(size.width * 0.1f, size.height * 0.85f)
                )
            }
        }

        // 4. Center Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .graphicsLayer {
                    this.alpha = contentAlpha.value
                    this.translationY = contentOffsetY.value * density
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Container with soft background & halo glow
            val glowColor = if (isDark) Color(0x4D7C72F5) else Color(0x265850E7)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor,
                                    Color.Transparent
                                ),
                                center = center,
                                radius = if (isDark) 80.dp.toPx() else 70.dp.toPx()
                            ),
                            radius = if (isDark) 80.dp.toPx() else 70.dp.toPx(),
                            center = center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(
                                        Color(0x337C72F5), // 20% alpha
                                        Color(0x1A4ECDC4)  // 10% alpha
                                    )
                                } else {
                                    listOf(
                                        Color(0x1F5850E7), // 12% alpha
                                        Color(0x14EB845C)  // 8% alpha
                                    )
                                }
                            )
                        )
                        .padding(24.dp)
                ) {
                    MoonLogo(
                        size = 64.dp,
                        backgroundColor = Color.Transparent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // spacing.6 gap

            // App name + Tagline Text Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val wordmarkText = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = if (isDark) Color(0xFFE8E6F2) else Color(0xFF1A1530)
                        )
                    ) {
                        append("MindRest")
                    }
                    withStyle(
                        SpanStyle(
                            color = if (isDark) Color(0xFF7C72F5) else Color(0xFF5850E7)
                        )
                    ) {
                        append(" AI")
                    }
                }
                Text(
                    text = wordmarkText,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.9).sp,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = "Sleep Better. Live with Purpose.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFF8A9ABB) else Color(0xFF8A8A9A),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 8.dp) // spacing.2 gap
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // spacing.4 gap

            // Progress Dots (staggered shimmer)
            val primaryColor = MaterialTheme.colorScheme.primary
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot 1 (Pill)
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = opacity1))
                )
                // Dot 2 (Circle)
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = opacity2))
                )
                // Dot 3 (Circle)
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = opacity3))
                )
            }
        }

        // 5. Version Badge
        Text(
            text = "v1.0",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = (if (isDark) Color(0xFF8A9ABB) else Color(0xFF8A8A9A)).copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Check system setting for reduced motion
    val isReducedMotion = remember {
        try {
            val scale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    // Determine dark mode based on background theme or system theme
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    // Background Gradient Brush (similar to Splash in dark mode, solid warm ivory in light)
    val backgroundBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF090C1A), // DarkBackground
                    Color(0xFF1A1040), // Deep Purple midnight
                    Color(0xFF0D1A2E)  // Dark Navy
                ),
                start = Offset(0.9f, 0.1f),
                end = Offset(0.1f, 0.9f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFAF8F3), // LightBackground
                    Color(0xFFFAF8F3)  // Solid warm ivory in light mode
                )
            )
        }
    }

    // Dynamic brand colors depending on active theme
    val primaryColor = if (isDark) Color(0xFF7C72F5) else Color(0xFF5850E7)
    val accentColor = if (isDark) Color(0xFF4ECDC4) else Color(0xFFEB845C)
    val journalingColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF3B82F6)
    val relaxationColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
    val mutedColor = if (isDark) Color(0xFF6B7A99) else Color(0xFF7A7590)

    // Slides local page index state
    var page by remember { mutableStateOf(0) }

    // Intercept Back Gesture to go back slides
    BackHandler(enabled = page > 0) {
        page--
    }

    // Adapt layout padding & typography sizing for very small devices (under 360dp)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    
    val screenPadding = 24.dp
    val topBarPaddingTop = if (isSmallScreen) 12.dp else 20.dp
    val illustrationPadding = if (isSmallScreen) 24.dp else 32.dp
    val titleFontSize = if (isSmallScreen) 26.sp else 30.sp
    val bottomPadding = if (isSmallScreen) 32.dp else 40.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
    ) {
        // Full Column Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenPadding)
                .padding(bottom = bottomPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP BAR: Skip Button (right-aligned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topBarPaddingTop),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFF6B7A99) else Color(0xFF7A7590)
                    ),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onComplete
                        )
                        .padding(8.dp)
                )
            }

            // 2. MAIN CONTENT AREA: Illustration + Title + Subtitle
            // Detect Drag Gestures for fluid left/right swipe transitions
            var totalDragX by remember { mutableStateOf(0f) }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(page) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragX = 0f },
                            onDragEnd = {
                                if (totalDragX < -120f) { // swipe left -> next page
                                    if (page < 2) {
                                        page++
                                    }
                                } else if (totalDragX > 120f) { // swipe right -> previous page
                                    if (page > 0) {
                                        page--
                                    }
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDragX += dragAmount
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        if (isReducedMotion) {
                            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                        } else {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(300))).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(300))
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(300))).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(300))
                                )
                            }
                        }
                    },
                    label = "slide_content_transition",
                    modifier = Modifier.fillMaxWidth()
                ) { currentPage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // IllustrationBox with custom vector drawing inside
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    if (isDark) Color(0x147C72F5) else Color(0x0D5850E7)
                                )
                                .padding(illustrationPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(200.dp, 180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentPage == 0) {
                                    // Slide 1 Illustration: Crescent Moon floating in concentric glow halos
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val center = Offset(size.width / 2, size.height / 2)
                                        // Outer halo
                                        drawCircle(
                                            color = primaryColor.copy(alpha = if (isDark) 0.08f else 0.06f),
                                            radius = 70.dp.toPx(),
                                            center = center
                                        )
                                        // Inner halo
                                        drawCircle(
                                            color = primaryColor.copy(alpha = if (isDark) 0.12f else 0.10f),
                                            radius = 50.dp.toPx(),
                                            center = center
                                        )
                                        // Premium crisp right-facing crescent moon
                                        val moonPath = Path().apply {
                                            val cx = center.x + 5.dp.toPx()
                                            val cy = center.y
                                            val r = 36.dp.toPx()
                                            arcTo(
                                                rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                                                startAngleDegrees = -90f,
                                                sweepAngleDegrees = 180f,
                                                forceMoveTo = true
                                            )
                                            val innerR = r * 0.9f
                                            val offset = r * 0.45f
                                            arcTo(
                                                rect = androidx.compose.ui.geometry.Rect(cx - innerR - offset, cy - innerR, cx + innerR - offset, cy + innerR),
                                                startAngleDegrees = 90f,
                                                sweepAngleDegrees = -180f,
                                                forceMoveTo = false
                                            )
                                            close()
                                        }
                                        drawPath(
                                            path = moonPath,
                                            color = primaryColor.copy(alpha = 0.9f)
                                        )
                                        // Ambient stars
                                        drawCircle(color = accentColor, radius = 4.dp.toPx(), center = Offset(center.x + 30.dp.toPx(), center.y - 28.dp.toPx()))
                                        drawCircle(color = primaryColor.copy(alpha = 0.6f), radius = 2.5.dp.toPx(), center = Offset(center.x + 45.dp.toPx(), center.y - 15.dp.toPx()))
                                        drawCircle(color = accentColor.copy(alpha = 0.5f), radius = 2.dp.toPx(), center = Offset(center.x + 55.dp.toPx(), center.y - 30.dp.toPx()))
                                    }
                                } else if (currentPage == 1) {
                                    // Slide 2 Illustration: Abstract connected brain nodes
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val center = Offset(size.width / 2, size.height / 2 + 10.dp.toPx())
                                        // Background ellipse glow
                                        drawOval(
                                            color = journalingColor.copy(alpha = if (isDark) 0.08f else 0.06f),
                                            topLeft = Offset(center.x - 65.dp.toPx(), center.y - 55.dp.toPx()),
                                            size = Size(130.dp.toPx(), 110.dp.toPx())
                                        )
                                        // Brain shape background fill
                                        val brainPath = Path().apply {
                                            moveTo(center.x - 30.dp.toPx(), center.y - 10.dp.toPx())
                                            quadraticTo(center.x, center.y - 40.dp.toPx(), center.x + 30.dp.toPx(), center.y - 10.dp.toPx())
                                            quadraticTo(center.x + 40.dp.toPx(), center.y + 10.dp.toPx(), center.x, center.y + 30.dp.toPx())
                                            quadraticTo(center.x - 40.dp.toPx(), center.y + 10.dp.toPx(), center.x - 30.dp.toPx(), center.y - 10.dp.toPx())
                                        }
                                        drawPath(
                                            path = brainPath,
                                            color = journalingColor.copy(alpha = 0.15f)
                                        )
                                        // Smooth curved neural pathways
                                        val topArc = Path().apply {
                                            moveTo(center.x - 30.dp.toPx(), center.y - 10.dp.toPx())
                                            quadraticTo(center.x, center.y - 40.dp.toPx(), center.x + 30.dp.toPx(), center.y - 10.dp.toPx())
                                        }
                                        drawPath(
                                            path = topArc,
                                            color = journalingColor,
                                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        val leftArc = Path().apply {
                                            moveTo(center.x, center.y + 30.dp.toPx())
                                            quadraticTo(center.x - 40.dp.toPx(), center.y + 10.dp.toPx(), center.x - 30.dp.toPx(), center.y - 10.dp.toPx())
                                        }
                                        drawPath(
                                            path = leftArc,
                                            color = journalingColor,
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        val rightArc = Path().apply {
                                            moveTo(center.x, center.y + 30.dp.toPx())
                                            quadraticTo(center.x + 40.dp.toPx(), center.y + 10.dp.toPx(), center.x + 30.dp.toPx(), center.y - 10.dp.toPx())
                                        }
                                        drawPath(
                                            path = rightArc,
                                            color = journalingColor,
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        // Neural connection nodes
                                        drawCircle(color = primaryColor, radius = 8.dp.toPx(), center = Offset(center.x, center.y - 10.dp.toPx()))
                                        drawCircle(color = accentColor.copy(alpha = 0.8f), radius = 5.dp.toPx(), center = Offset(center.x - 30.dp.toPx(), center.y - 10.dp.toPx()))
                                        drawCircle(color = accentColor.copy(alpha = 0.8f), radius = 5.dp.toPx(), center = Offset(center.x + 30.dp.toPx(), center.y - 10.dp.toPx()))
                                        // Radiating spark lines
                                        drawLine(color = mutedColor.copy(alpha = 0.4f), start = Offset(center.x - 45.dp.toPx(), center.y - 30.dp.toPx()), end = Offset(center.x - 20.dp.toPx(), center.y - 45.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                        drawLine(color = mutedColor.copy(alpha = 0.4f), start = Offset(center.x + 20.dp.toPx(), center.y - 45.dp.toPx()), end = Offset(center.x + 45.dp.toPx(), center.y - 30.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                        drawLine(color = mutedColor.copy(alpha = 0.4f), start = Offset(center.x - 50.dp.toPx(), center.y + 5.dp.toPx()), end = Offset(center.x - 30.dp.toPx(), center.y + 20.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                        drawLine(color = mutedColor.copy(alpha = 0.4f), start = Offset(center.x + 30.dp.toPx(), center.y + 20.dp.toPx()), end = Offset(center.x + 50.dp.toPx(), center.y + 5.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                    }
                                } else {
                                    // Slide 3 Illustration: Interconnected Ikigai diagrams simplified
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val center = Offset(size.width / 2, size.height / 2)
                                            // Centered background glow
                                            drawCircle(
                                                color = relaxationColor.copy(alpha = if (isDark) 0.08f else 0.06f),
                                                radius = 60.dp.toPx(),
                                                center = center
                                            )
                                            // Line connections radiating from center dot
                                            drawLine(color = primaryColor, start = center, end = Offset(center.x, center.y - 45.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                            drawLine(color = accentColor, start = center, end = Offset(center.x + 37.dp.toPx(), center.y + 23.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                            drawLine(color = relaxationColor, start = center, end = Offset(center.x - 37.dp.toPx(), center.y + 23.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                            // Center core dot
                                            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = center)
                                        }
                                        // Overlaying high-fidelity round cards with custom symbols/emojis
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = 10.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(primaryColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("❤", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(bottom = 20.dp, end = 25.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💡", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(bottom = 20.dp, start = 25.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(relaxationColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🌍", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp)) // gap-8 spacing

                        // Title text block
                        val slideTitle = when (currentPage) {
                            0 -> "AI Understands\nYour Sleep"
                            1 -> "Reduce\nOverthinking"
                            else -> "Discover Your\nIkigai"
                        }
                        Text(
                            text = slideTitle,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 38.sp,
                                textAlign = TextAlign.Center,
                                color = if (isDark) Color(0xFFE8E6F2) else Color(0xFF1A1530)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp)) // gap-3 spacing

                        // Subtitle text block
                        val slideSubtitle = when (currentPage) {
                            0 -> "Our AI analyzes your sleep patterns and provides personalized insights to help you sleep deeper and wake refreshed."
                            1 -> "CBT-based journaling and AI reflection help quiet your mind, process emotions, and build mental clarity every day."
                            else -> "Find your reason for being. Our AI guides you through the four pillars of Ikigai to reveal your unique life purpose."
                        }
                        Text(
                            text = slideSubtitle,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                color = if (isDark) Color(0xFF8A9ABB) else Color(0xFF8A8A9A)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // 3. BOTTOM CONTROLS AREA: Dots row + ActionButton
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp) // space-y-5
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..2) {
                        val isActive = page == i
                        val dotWidth by animateDpAsState(
                            targetValue = if (isActive) 24.dp else 8.dp,
                            animationSpec = if (isReducedMotion) snap() else tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "dot_width"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isActive) primaryColor else primaryColor.copy(alpha = 0.2f),
                            animationSpec = if (isReducedMotion) snap() else tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "dot_color"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = dotWidth, height = 8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    page = i
                                }
                                .testTag("onboarding_dot_$i")
                        )
                    }
                }

                // Smoothly Swapping Action Button (Continue or Get Started)
                AnimatedContent(
                    targetState = page < 2,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "button_swap",
                    modifier = Modifier.fillMaxWidth()
                ) { isContinueButton ->
                    if (isContinueButton) {
                        // Continue Button
                        val continueBrush = remember(isDark) {
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF7C72F5), Color(0xFF9D95F8))
                                } else {
                                    listOf(Color(0xFF5850E7), Color(0xFF7C72F5))
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(continueBrush)
                                .clickable {
                                    if (page < 2) {
                                        page++
                                    }
                                }
                                .testTag("continue_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Continue",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        // Get Started Button with sparkle gradient
                        val getStartedBrush = remember(isDark) {
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF7C72F5), Color(0xFF4ECDC4))
                                } else {
                                    listOf(Color(0xFF5850E7), Color(0xFFEB845C))
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(getStartedBrush)
                                .clickable {
                                    onComplete()
                                }
                                .testTag("get_started_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Get Started ✨",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val scale = size.width / 18f
        withTransform({
            scale(scale, scale, Offset.Zero)
        }) {
            // Path 1 (Blue)
            val p1 = Path().apply {
                moveTo(17.64f, 9.2f)
                cubicTo(17.64f, 8.563f, 17.583f, 7.949f, 17.476f, 7.36f)
                lineTo(9f, 7.36f)
                lineTo(9f, 10.841f)
                lineTo(13.844f, 10.841f)
                cubicTo(13.635f, 11.966f, 13.001f, 12.919f, 12.048f, 13.558f)
                lineTo(12.048f, 15.816f)
                lineTo(14.956f, 15.816f)
                cubicTo(16.658f, 14.249f, 17.64f, 11.942f, 17.64f, 9.2f)
                close()
            }
            drawPath(p1, Color(0xFF4285F4))

            // Path 2 (Green)
            val p2 = Path().apply {
                moveTo(9f, 18f)
                cubicTo(11.43f, 18f, 13.467f, 17.194f, 14.956f, 15.82f)
                lineTo(12.048f, 13.561f)
                cubicTo(11.242f, 14.101f, 10.211f, 14.421f, 9f, 14.421f)
                cubicTo(6.656f, 14.421f, 4.672f, 12.837f, 3.964f, 10.71f)
                lineTo(0.957f, 10.71f)
                lineTo(0.957f, 13.042f)
                cubicTo(2.43f, 16.5f, 5.48f, 18f, 9f, 18f)
                close()
            }
            drawPath(p2, Color(0xFF34A853))

            // Path 3 (Yellow)
            val p3 = Path().apply {
                moveTo(3.964f, 10.71f)
                cubicTo(3.784f, 10.167f, 3.682f, 9.597f, 3.682f, 9f)
                cubicTo(3.682f, 8.403f, 3.784f, 7.833f, 3.964f, 7.29f)
                lineTo(3.964f, 4.958f)
                lineTo(0.957f, 4.958f)
                cubicTo(0.348f, 6.173f, 0f, 7.548f, 0f, 9f)
                cubicTo(0f, 10.452f, 0.348f, 11.827f, 0.957f, 13.042f)
                lineTo(3.964f, 10.71f)
                close()
            }
            drawPath(p3, Color(0xFFFBBC05))

            // Path 4 (Red)
            val p4 = Path().apply {
                moveTo(9f, 3.58f)
                cubicTo(10.321f, 3.58f, 11.508f, 4.034f, 12.44f, 4.925f)
                lineTo(15.022f, 2.345f)
                cubicTo(13.463f, 0.891f, 11.426f, 0f, 9f, 0f)
                cubicTo(5.48f, 0f, 2.43f, 1.5f, 0.957f, 4.958f)
                lineTo(3.964f, 6.29f)
                cubicTo(4.672f, 4.163f, 6.656f, 3.58f, 9f, 3.58f)
                close()
            }
            drawPath(p4, Color(0xFFEA4335))
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // `remember` penting — default parameter akan dievaluasi ulang tiap recompose,
    // yang akan membuat ViewModel baru dan me-reset state ke nilai awal.
    val viewModel: LoginViewModel = remember { LoginViewModel.create() }
    val state by viewModel.uiState.collectAsState()

    // Trigger navigasi tepat sekali setelah sign-in berhasil.
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Scrollable upper area containing header, title, subtitle, and input fields
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // 1. BrandHeader
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    MoonLogo(size = 36.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "MindRest AI",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // 2. PageTitle
                Text(
                    text = "Welcome back 👋",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 3. PageSubtitle
                Text(
                    text = "Sign in to continue your wellness journey",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 4. InputGroup
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextInputField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Email address",
                        leadingIcon = Icons.Default.MailOutline,
                        keyboardType = KeyboardType.Email,
                        error = state.emailError,
                        testTag = "email_input"
                    )

                    PasswordInputField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Password",
                        error = state.passwordError,
                        testTag = "password_input"
                    )
                }

                // 5. ForgotPasswordLink
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 32.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { /* No-op placeholder */ }
                            .padding(4.dp)
                            .testTag("forgot_password_link")
                    )
                }

                // 6. SignInButton
                PrimaryButton(
                    text = if (state.isLoading) "Signing in..." else "Sign In",
                    onClick = { viewModel.onSubmit() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sign_in_button"
                )

                // 6b. Error banner — tampil di bawah tombol saat submit gagal.
                state.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("sign_in_error"),
                        textAlign = TextAlign.Start,
                    )
                }
            }

            // Bottom stack: Divider, Google SSO, and Sign Up row
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 7. DividerRow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "or continue with",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }

                // 8. GoogleButton (Continue with Google Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable(enabled = !state.isLoading) { /* TODO: native Google SSO */ }
                        .testTag("google_sso_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google multicolor G icon
                        GoogleLogoIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 9. SignUpRow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Sign Up",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(enabled = !state.isLoading) { onNavigateToRegister() }
                            .padding(4.dp)
                            .testTag("sign_up_link")
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // `remember` penting — default parameter akan dievaluasi ulang tiap recompose,
    // yang akan membuat ViewModel baru dan me-reset state ke nilai awal.
    val viewModel: RegisterViewModel = remember { RegisterViewModel.create() }
    val state by viewModel.uiState.collectAsState()

    // Trigger navigasi tepat sekali setelah sign-up berhasil.
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onRegisterSuccess()
        }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val primaryLightColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // 1. BackButton
                Row(
                    modifier = Modifier
                        .clickable(enabled = !state.isLoading) { onNavigateToLogin() }
                        .padding(vertical = 4.dp)
                        .testTag("back_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back to sign in",
                        tint = if (isDark) Color(0xFF6B7A99) else Color(0xFF7A7590),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFF6B7A99) else Color(0xFF7A7590)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. PageTitle
                Text(
                    text = "Create Account 🌙",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 3. PageSubtitle
                Text(
                    text = "Start your mental wellness journey today",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 4. InputGroup
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextInputField(
                        value = state.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        placeholder = "Full name",
                        leadingIcon = Icons.Default.Person,
                        error = state.fullNameError,
                        testTag = "full_name_input"
                    )

                    TextInputField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Email address",
                        leadingIcon = Icons.Default.MailOutline,
                        keyboardType = KeyboardType.Email,
                        error = state.emailError,
                        testTag = "email_input"
                    )

                    TextInputField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Password",
                        leadingIcon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        error = state.passwordError,
                        testTag = "password_input"
                    )

                    TextInputField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        placeholder = "Confirm password",
                        leadingIcon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        error = state.confirmPasswordError,
                        testTag = "confirm_password_input"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. LegalConsentBox
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryLightColor)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    val annotatedText = buildAnnotatedString {
                        append("By creating an account, you agree to our ")
                        
                        pushStringAnnotation(tag = "terms", annotation = "terms")
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )) {
                            append("Terms of Service")
                        }
                        pop()
                        
                        append(" and ")
                        
                        pushStringAnnotation(tag = "privacy", annotation = "privacy")
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )) {
                            append("Privacy Policy")
                        }
                        pop()
                        
                        append(".")
                    }

                    ClickableText(
                        text = annotatedText,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    // Handle Terms of Service click - no-op placeholder
                                }
                            annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    // Handle Privacy Policy click - no-op placeholder
                                }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. CreateAccountButton (PrimaryButton)
                PrimaryButton(
                    text = if (state.isLoading) "Creating account..." else "Create Account",
                    onClick = { viewModel.onSubmit() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "create_account_button"
                )

                // 6b. Error banner
                state.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("register_error"),
                        textAlign = TextAlign.Start,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. SignInRow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(enabled = !state.isLoading) { onNavigateToLogin() }
                        .padding(4.dp)
                        .testTag("sign_in_link")
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    MindRestTheme {
        SplashScreen(onNavigateToOnboarding = {}, onNavigateToHome = {}, onNavigateToLogin = {})
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    MindRestTheme {
        OnboardingScreen(onComplete = {})
    }
}
