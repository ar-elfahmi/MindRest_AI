package com.example.features.ikigai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.LocalElevation
import com.example.core.designsystem.LocalShapes
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.AppCard
import com.example.core.designsystem.components.AppCardVariant
import com.example.core.designsystem.components.AppScaffold
import com.example.core.designsystem.components.screenEdgeValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IkigaiDashboardScreen(
    onNavigateBack: () -> Unit,
    /** Dipakai tombol "Mulai Assessment Ikigai" (TASK 2.3). */
    onStartAssessment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ikigai Journey",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(spacing.space12)) // To balance the back button
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = screenEdgeValues(),
            verticalArrangement = Arrangement.spacedBy(spacing.space6)
        ) {
            // SECTION 0: MULAI ASSESSMENT (TASK 2.3 — tombol masuk ke onboarding)
            item {
                Button(
                    onClick = onStartAssessment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), // residual: no shapes token for 12
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp), // residual: no token for 14
                ) {
                    Text(
                        text = "Mulai Assessment Ikigai",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // SECTION 1: RANGKUMAN JURNAL
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                    Text(
                        text = "Refleksi Hari Ini",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LocalShapes.current.lg)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .border(
                                width = LocalElevation.current.xs,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                shape = LocalShapes.current.lg
                            )
                            .padding(spacing.cardPadding)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = spacing.space3)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp) // residual: icon intrinsic
                                )
                                Spacer(modifier = Modifier.width(6.dp)) // residual: no token for 6
                                Text(
                                    text = "Generated by AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Dari obrolan kita hari ini, sepertinya kamu sedang merasa terbebani dengan ekspektasi karir, namun kamu memiliki passion yang kuat di bidang desain kreatif. Kamu sudah melakukan yang terbaik.",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp), // residual: sp lineHeight
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SECTION 2: PETA IKIGAI
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    Text(
                        text = "Fokus Ikigai-mu Saat Ini",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.space0)
                    ) {
                        IkigaiNode(
                            text = "Passion: Eksplorasi Seni Digital",
                            isLast = false
                        )
                        IkigaiNode(
                            text = "Misi: Mencari keseimbangan kerja (Work-life balance)",
                            isLast = true
                        )
                    }
                }
            }

            // SECTION 3: REKOMENDASI HIDUP
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    Text(
                        text = "Langkah Kecil Selanjutnya",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
                        contentPadding = PaddingValues(end = spacing.screenHorizontal)
                    ) {
                        item {
                            ActionCard(
                                text = "Luangkan 15 menit hari ini untuk menggambar tanpa tujuan.",
                                icon = Icons.Default.Palette
                            )
                        }
                        item {
                            ActionCard(
                                text = "Atur batas waktu layar (Screen Time) di jam 9 malam.",
                                icon = Icons.Default.Smartphone
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IkigaiNode(
    text: String,
    isLast: Boolean
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(spacing.space8)
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.space6)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.space3)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp) // residual: 2dp stroke width
                        .fillMaxHeight()
                        .padding(vertical = spacing.space1)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            } else {
                Spacer(modifier = Modifier.height(spacing.space6))
            }
        }

        Spacer(modifier = Modifier.width(spacing.space4))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) spacing.space0 else spacing.space6)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp) // residual: no token for 2
            )
        }
    }
}

@Composable
private fun ActionCard(
    text: String,
    icon: ImageVector
) {
    val spacing = LocalSpacing.current
    AppCard(
        modifier = Modifier.width(260.dp), // residual: ActionCard intrinsic width
        variant = AppCardVariant.Tonal
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.space4)
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.space12)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 3
            )

            Button(
                onClick = { /* Accept action */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // residual: no shapes token for 12
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Terima Tantangan",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IkigaiDashboardScreenPreview() {
    MindRestTheme {
        IkigaiDashboardScreen(onNavigateBack = {}, onStartAssessment = {})
    }
}
