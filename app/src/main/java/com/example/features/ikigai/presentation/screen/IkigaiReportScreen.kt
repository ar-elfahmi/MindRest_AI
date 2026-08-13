package com.example.features.ikigai.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.components.AppCard
import com.example.core.designsystem.components.AppCardVariant
import com.example.core.designsystem.components.AppScaffold
import com.example.features.ikigai.data.dto.IkigaiCircles
import com.example.features.ikigai.data.dto.IkigaiRecommendation
import com.example.features.ikigai.data.repository.IkigaiReport
import com.example.features.ikigai.presentation.viewmodel.IkigaiReportViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Ikigai Report Display Screen (TASK 3.3).
 *
 * 3 section sesuai UI REQUIREMENT:
 *   1. HEADER: tanggal generate + tombol Refresh (disabled kalau rate-limited).
 *   2. LAPORAN: render report_markdown (Compose Text, bukan WebView).
 *   3. 4 LINGKARAN IKIGAI: Canvas custom, 4 quadrant Cintai/Skill/Profesi/Misi.
 *   4. REKOMENDASI: LazyColumn 3-5 card, masing-masing dengan Checkbox.
 *
 * State handling: Loading (skeleton), Empty (CTA Mulai Assessment), Error, 429.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IkigaiReportScreen(
    onNavigateBack: () -> Unit = {},
    /** Dipakai saat user tiba dari Assessment → auto-trigger generate. */
    autoTriggerFromAssessment: Boolean = false,
    onStartAssessment: () -> Unit = {},
    viewModel: IkigaiReportViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-trigger dari Assessment: fire-once saat screen pertama kali compose.
    LaunchedEffect(Unit) {
        if (autoTriggerFromAssessment) {
            viewModel.onScreenEnteredFromAssessment()
        }
    }

    // Error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    // Toggle success snackbar
    LaunchedEffect(uiState.toggleMessage) {
        uiState.toggleMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    AppScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Laporan Ikigai",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onGenerateReport() },
                        enabled = !uiState.isGenerating && !uiState.isRateLimited,
                        modifier = Modifier.testTag("btn_refresh_report"),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when {
            // INITIAL LOADING (first load, belum ada data)
            uiState.isInitialLoading && uiState.report == null -> {
                InitialLoadingPlaceholder(innerPadding)
            }

            // EMPTY STATE (belum ada report)
            uiState.showEmptyState -> {
                EmptyState(
                    innerPadding = innerPadding,
                    onStartAssessment = onStartAssessment,
                )
            }

            // LOADED / GENERATING (overlay)
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenTop),
                    verticalArrangement = Arrangement.spacedBy(spacing.space5),
                ) {
                    // Regenerating banner (kalau sedang trigger)
                    AnimatedVisibility(
                        visible = uiState.isGenerating,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200)),
                    ) {
                        GeneratingBanner()
                    }

                    // ========== HEADER ==========
                    uiState.report?.let { report ->
                        ReportHeader(report = report)
                    }

                    // ========== 4 LINGKARAN IKIGAI (Canvas) ==========
                    uiState.report?.let { report ->
                        IkigaiCirclesCard(circles = report.circles)
                    }

                    // ========== LAPORAN (markdown) ==========
                    uiState.report?.let { report ->
                        MarkdownReportCard(markdown = report.reportMarkdown)
                    }

                    // ========== REKOMENDASI ==========
                    uiState.report?.let { report ->
                        RecommendationsSection(
                            recommendations = report.recommendations,
                            onToggle = viewModel::onToggleRecommendation,
                        )
                    }

                    Spacer(Modifier.height(spacing.space4))
                }
            }
        }
    }
}

// ===========================================================================
// SUB-COMPOSABLES
// ===========================================================================

@Composable
private fun ReportHeader(report: IkigaiReport) {
    val dateText = remember(report.generatedAt) {
        formatIsoDate(report.generatedAt)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dibuat $dateText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Versi ${report.version}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GeneratingBanner() {
    val spacing = LocalSpacing.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AppCardVariant.Tonal
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space4, vertical = spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), // residual: spinner size
                strokeWidth = 2.dp, // residual: stroke
            )
            Column {
                Text(
                    text = "Sedang menyusun laporan...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "AI menganalisis jawabanmu. Biasanya 3-5 detik.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InitialLoadingPlaceholder(innerPadding: PaddingValues) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(spacing.space8),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.space12),
                strokeWidth = 3.dp // residual: stroke
            )
            Spacer(Modifier.height(spacing.space4))
            Text(
                text = "Memuat laporan...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(
    innerPadding: PaddingValues,
    onStartAssessment: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp) // residual: icon intrinsic
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✨", style = MaterialTheme.typography.displayMedium)
        }
        Spacer(Modifier.height(spacing.space5))
        Text(
            text = "Belum ada laporan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.space2))
        Text(
            text = "Selesaikan assessment 6 pertanyaan untuk mendapatkan laporan Ikigai personalmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.space6))
        Button(
            onClick = onStartAssessment,
            shape = RoundedCornerShape(12.dp), // residual: no shapes token for 12
            contentPadding = PaddingValues(horizontal = spacing.space6, vertical = 14.dp), // residual: no token for 14
            modifier = Modifier.testTag("btn_start_assessment"),
        ) {
            Text(
                "Mulai Assessment",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4 LINGKARAN IKIGAI (Canvas)
// ---------------------------------------------------------------------------

/**
 * 4 lingkaran bertumpuk (Venn diagram style) merepresentasikan 4 pilar Ikigai:
 *   - Cintai (atas) — passion
 *   - Skill (kanan) — vocation
 *   - Profesi (bawah) — profession
 *   - Misi (kiri) — mission
 *
 * Pattern: Canvas dengan 4 arc + label luar pakai nativeCanvas.drawText.
 */
@Composable
private fun IkigaiCirclesCard(
    circles: IkigaiCircles,
) {
    val spacing = LocalSpacing.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AppCardVariant.Tonal
    ) {
        Column {
            Text(
                text = "4 Lingkaran Ikigai",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(spacing.space3))

            // Canvas 4 lingkaran
            IkigaiFourCirclesCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // residual: canvas drawing height
                    .testTag("ikigai_circles_canvas"),
            )

            Spacer(Modifier.height(spacing.space4))

            // Legend list (4 item dengan warna)
            IkigaiCircleLegend(
                label = "Cintai (Passion)",
                value = circles.passion,
                color = IkigaiCircleColors.Passion,
            )
            IkigaiCircleLegend(
                label = "Skill",
                value = circles.skill,
                color = IkigaiCircleColors.Skill,
            )
            IkigaiCircleLegend(
                label = "Profesi",
                value = circles.profession,
                color = IkigaiCircleColors.Profession,
            )
            IkigaiCircleLegend(
                label = "Misi",
                value = circles.mission,
                color = IkigaiCircleColors.Mission,
            )
        }
    }
}

@Composable
private fun IkigaiCircleLegend(label: String, value: String, color: Color) {
    if (value.isBlank()) return
    // Residuals: vertical 6, top 6, size 10, width 10 — no design-system tokens
    // for legend dot/padding micro-spacing; preserved as-is.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private object IkigaiCircleColors {
    val Passion = Color(0xFFE57373)    // Coral
    val Skill = Color(0xFF64B5F6)      // Light Blue
    val Profession = Color(0xFF81C784) // Green
    val Mission = Color(0xFFFFB74D)    // Orange
}

@Composable
private fun IkigaiFourCirclesCanvas(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    var animState by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animState,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "IkigaiAnim",
    )
    LaunchedEffect(Unit) {
        animState = 1f
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Inset supaya lingkaran punya ruang untuk label luar
            val inset = spacing.space8.toPx()
            val drawAreaW = w - inset * 2
            val drawAreaH = h - inset * 2
            val radius = minOf(drawAreaW, drawAreaH) * 0.34f
            val cx = w / 2f
            val cy = h / 2f

            // Offset 4 lingkaran ke atas, bawah, kiri, kanan
            val circleDefs = listOf(
                Triple(IkigaiCircleColors.Passion, Offset(cx, cy - radius * 0.85f), "CINTAI"),
                Triple(IkigaiCircleColors.Skill, Offset(cx + radius * 0.85f, cy), "SKILL"),
                Triple(IkigaiCircleColors.Profession, Offset(cx, cy + radius * 0.85f), "PROFESI"),
                Triple(IkigaiCircleColors.Mission, Offset(cx - radius * 0.85f, cy), "MISI"),
            )

            // Draw 4 circles dengan fill semi-transparent
            circleDefs.forEach { (color, center, _) ->
                drawCircle(
                    color = color.copy(alpha = 0.30f * animatedProgress),
                    radius = radius * animatedProgress,
                    center = center,
                )
                drawCircle(
                    color = color.copy(alpha = 0.85f),
                    radius = radius * animatedProgress,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()), // residual: stroke
                )
            }

            // Draw intersection marker (dot center)
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = 6.dp.toPx(), // residual: dot radius (no token for 6)
                center = Offset(cx, cy),
            )

            // Draw labels (atas/kanan/bawah/kiri)
            val labelPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.BLACK
                textSize = 12.sp.toPx() // residual: sp for canvas paint
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText("CINTAI", cx, cy - radius * 1.4f - spacing.space1.toPx(), labelPaint)
                drawText("SKILL", cx + radius * 1.4f, cy + spacing.space1.toPx(), labelPaint)
                drawText("PROFESI", cx, cy + radius * 1.4f + 12.sp.toPx(), labelPaint) // residual: sp
                drawText("MISI", cx - radius * 1.4f, cy + spacing.space1.toPx(), labelPaint)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// MARKDOWN REPORT (no WebView)
// ---------------------------------------------------------------------------

/**
 * Mini parser markdown: handle # H1, ## H2, **bold**, plain paragraphs.
 * Output: list of [MarkdownBlock] yang dirender Compose Text sederhana.
 */
private sealed class MarkdownBlock {
    data class Heading(val text: String, val level: Int) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

private fun parseMarkdown(md: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = md.lines()
    val paragraphBuffer = StringBuilder()
    fun flushParagraph() {
        if (paragraphBuffer.isNotBlank()) {
            blocks += MarkdownBlock.Paragraph(paragraphBuffer.toString().trim())
            paragraphBuffer.clear()
        }
    }
    for (raw in lines) {
        val line = raw.trimEnd()
        when {
            line.startsWith("# ") -> {
                flushParagraph()
                blocks += MarkdownBlock.Heading(line.removePrefix("# ").trim(), 1)
            }
            line.startsWith("## ") -> {
                flushParagraph()
                blocks += MarkdownBlock.Heading(line.removePrefix("## ").trim(), 2)
            }
            line.startsWith("### ") -> {
                flushParagraph()
                blocks += MarkdownBlock.Heading(line.removePrefix("### ").trim(), 3)
            }
            line.isBlank() -> flushParagraph()
            else -> {
                if (paragraphBuffer.isNotEmpty()) paragraphBuffer.append('\n')
                paragraphBuffer.append(line)
            }
        }
    }
    flushParagraph()
    return blocks
}

/**
 * Convert string dengan **bold** sederhana jadi AnnotatedString.
 */
private fun renderBoldText(text: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("\\*\\*(.+?)\\*\\*")
    var lastEnd = 0
    regex.findAll(text).forEach { match ->
        append(text.substring(lastEnd, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        lastEnd = match.range.last + 1
    }
    if (lastEnd < text.length) append(text.substring(lastEnd))
}

@Composable
private fun MarkdownReportCard(markdown: String) {
    val spacing = LocalSpacing.current
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AppCardVariant.Tonal
    ) {
        Column {
            Text(
                text = "Laporan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(spacing.space3))
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> {
                        Text(
                            text = block.text,
                            style = when (block.level) {
                                1 -> MaterialTheme.typography.headlineSmall
                                2 -> MaterialTheme.typography.titleLarge
                                else -> MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = spacing.space3,
                                bottom = spacing.space1,
                            ),
                        )
                    }
                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = renderBoldText(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp, // residual: lineHeight typography override
                            modifier = Modifier.padding(vertical = spacing.space1),
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// REKOMENDASI
// ---------------------------------------------------------------------------

@Composable
private fun RecommendationsSection(
    recommendations: List<IkigaiRecommendation>,
    onToggle: (recId: String, done: Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
        Text(
            text = "Rekomendasi untukmu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${recommendations.count { it.done }} dari ${recommendations.size} selesai",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Progress bar
        val progress = if (recommendations.isEmpty()) 0f
                       else recommendations.count { it.done }.toFloat() / recommendations.size
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // residual: bar height
                .clip(RoundedCornerShape(3.dp)), // residual: bar radius
        )

        recommendations.forEach { rec ->
            RecommendationCard(
                text = rec.text,
                done = rec.done,
                onToggle = { onToggle(rec.id, it) },
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    text: String,
    done: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rec_card_${text.hashCode()}"),
        variant = AppCardVariant.Tonal
    ) {
        // Note: AppCard.Tonal uses surface (loses the "done"-state primaryContainer
        // tint that the original Card had). Done state is still conveyed by the
        // Checkbox and onSurfaceVariant text color.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space3, vertical = spacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = done,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("rec_checkbox"),
            )
            Spacer(Modifier.width(spacing.space1))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = spacing.space2),
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// UTIL
// ---------------------------------------------------------------------------

/** Format ISO 8601 "2026-01-15T10:30:00Z" → "15 Jan 2026, 17:30" (lokal). */
private fun formatIsoDate(iso: String): String {
    if (iso.isBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso.substringBefore('.').substringBefore('Z'))
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        formatter.format(date ?: return iso)
    } catch (e: Exception) {
        iso
    }
}
