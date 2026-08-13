package com.example.features.relaxation.presentation.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.InfoColor
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.AppScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedRelaxationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("Gerak") }
    var isPlaying by remember { mutableStateOf(false) }

    val spacing = LocalSpacing.current
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF001F3F), // Immersive ambient navy (no flat token; like SleepHero gradient)
            Color.Black
        )
    )

    AppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ruang Tenang",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Sleep Timer */ }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mode Switcher
                val modes = listOf("Gerak", "Napas", "Suara")
                TabRow(
                    selectedTabIndex = modes.indexOf(selectedMode),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {}
                ) {
                    modes.forEachIndexed { index, mode ->
                        Tab(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            text = {
                                Text(
                                    text = mode,
                                    fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedMode == mode) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.space6))

                // Dynamic Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space6),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedMode) {
                        "Gerak" -> GerakContent()
                        "Napas" -> NapasContent()
                        "Suara" -> SuaraContent()
                    }
                }

                // Master Play/Pause Control
                MasterControls(
                    isPlaying = isPlaying,
                    onPlayPauseToggle = { isPlaying = !isPlaying }
                )
            }
        }
    }
}

@Composable
private fun GerakContent() {
    val spacing = LocalSpacing.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(LocalShapes.current.md)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // "Video" content placeholder
            // Blue-light / Wind-down Filter
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x20FF8C00)) // Warm overlay
            )
            
            // Play Icon
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.space4))
        
        Text(
            text = "Bedtime Stretch (10 Min)",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NapasContent() {
    val spacing = LocalSpacing.current
    var isHapticSyncEnabled by remember { mutableStateOf(true) }
    
    // Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            InfoColor.copy(alpha = 0.53f),
                            InfoColor.copy(alpha = 0f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(InfoColor)
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.space12))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space3)
        ) {
            Text(
                text = "Haptic Feedback Sync",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            // Trigger device vibrations tied to animation when enabled
            Switch(
                checked = isHapticSyncEnabled,
                onCheckedChange = { isHapticSyncEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = InfoColor
                )
            )
        }
    }
}

@Composable
private fun SuaraContent() {
    val spacing = LocalSpacing.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "AI Dynamic Soundscape",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = spacing.space8)
        )
        
        AudioMixerRow(label = "Hujan Gerimis", initialValue = 0.7f)
        Spacer(modifier = Modifier.height(spacing.space6))
        AudioMixerRow(label = "Binaural Theta", initialValue = 0.4f)
        Spacer(modifier = Modifier.height(spacing.space6))
        AudioMixerRow(label = "Api Unggun", initialValue = 0f)
    }
}

@Composable
private fun AudioMixerRow(label: String, initialValue: Float) {
    var sliderValue by remember { mutableStateOf(initialValue) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = InfoColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun MasterControls(
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.space6),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onPlayPauseToggle,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedRelaxationScreenPreview() {
    MindRestTheme {
        AdvancedRelaxationScreen(onNavigateBack = {})
    }
}
