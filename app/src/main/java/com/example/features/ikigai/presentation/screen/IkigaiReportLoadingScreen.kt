package com.example.features.ikigai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.components.AppScaffold

/**
 * Placeholder loading screen — di-navigate setelah user submit assessment.
 *
 * Konten final (UI laporan + 4 lingkaran + rekomendasi) akan dipasang di
 * TASK 3.3 (Ikigai Report Display UI), yang membaca dari `ikigai_reports`
 * via Edge Function `generate-ikigai-report` (TASK 3.1).
 *
 * Untuk sekarang, screen ini cuma menampilkan indikator loading + back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IkigaiReportLoadingScreen(
    onNavigateBack: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generating Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(spacing.space8),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 4.dp) // residual: spinner size + stroke
                Spacer(Modifier.height(spacing.space6))
                Text(
                    text = "Menyusun laporan Ikigai kamu...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(spacing.space2))
                Text(
                    text = "AI sedang menganalisis jawabanmu.\nLaporan akan muncul di sini segera.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp, // residual: bodyMedium is 16sp here; override to 14sp preserves original size
                )
            }
        }
    }
}
