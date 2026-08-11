package com.example.features.ikigai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.ikigai.presentation.viewmodel.IkigaiAssessmentViewModel

/**
 * 6 pertanyaan yang akan dikirim ke AI.
 * Pertanyaan PERSIS dari ROADMAP.md section 1 Q4 + komentar kolom DB.
 */
private val questions = listOf(
    "Sebutkan 3 hal yang paling kamu nikmati di hidup ini.",
    "Hal apa yang kamu rasa paling kamu kuasai, atau sering dipuji orang lain?",
    "Apa pekerjaan atau aktivitas utama yang kamu jalani sekarang?",
    "Kalau kamu bisa memberi satu kontribusi ke dunia, apa yang ingin kamu berikan?",
    "Hal apa yang paling sering membuatmu overthinking akhir-akhir ini?",
    "Secara keseluruhan, seberapa puaskah kamu dengan hidupmu saat ini?",
)

/**
 * Pilihan chip untuk Q5 — disimpan sebagai TEXT di kolom q5_overthinking.
 * User boleh pilih salah satu (single-select).
 */
private val overthinkingChoices = listOf(
    "Tidur / insomnia",
    "Pikiran / overthinking",
    "Pekerjaan / karir",
    "Hubungan / sosial",
    "Kesehatan",
    "Finansial",
    "Masa depan",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IkigaiAssessmentScreen(
    onNavigateBack: () -> Unit = {},
    /** Dipanggil setelah assessment tersimpan; bawa id ke loading screen. */
    onAssessmentSaved: (assessmentId: String) -> Unit = {},
    viewModel: IkigaiAssessmentViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger navigasi ke loading screen begitu savedAssessmentId terisi.
    LaunchedEffect(uiState.savedAssessmentId) {
        uiState.savedAssessmentId?.let { onAssessmentSaved(it) }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ikigai Assessment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ---------------- Stepper header ----------------
            Text(
                text = "Step ${uiState.currentStep + 1} / ${uiState.totalSteps}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (uiState.currentStep + 1).toFloat() / uiState.totalSteps },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ---------------- Step content ----------------
            val step = uiState.currentStep
            Text(
                text = questions[step],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when (step) {
                    4 -> "Pilih salah satu yang paling relevan."
                    5 -> "Geser slider dari 1 (tidak puas) sampai 10 (sangat puas)."
                    else -> "Jawabanmu membantu AI memahami dirimu."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            when (step) {
                0 -> OutlinedTextField(
                    value = uiState.q1Passion,
                    onValueChange = viewModel::onQ1Changed,
                    label = { Text("Hal yang kamu nikmati") },
                    placeholder = { Text("Mis. menulis, memasak, hiking...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                1 -> OutlinedTextField(
                    value = uiState.q2Skill,
                    onValueChange = viewModel::onQ2Changed,
                    label = { Text("Skill / kekuatanmu") },
                    placeholder = { Text("Mis. public speaking, problem solving...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                2 -> OutlinedTextField(
                    value = uiState.q3Profession,
                    onValueChange = viewModel::onQ3Changed,
                    label = { Text("Pekerjaan / aktivitas utama") },
                    placeholder = { Text("Mis. software engineer, mahasiswa, ibu rumah tangga...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                3 -> OutlinedTextField(
                    value = uiState.q4Mission,
                    onValueChange = viewModel::onQ4Changed,
                    label = { Text("Misi / kontribusi ke dunia") },
                    placeholder = { Text("Mis. membantu anak muda, mengurangi polusi...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                4 -> ChipChoiceGrid(
                    options = overthinkingChoices,
                    selected = uiState.q5Overthinking,
                    onSelect = viewModel::onQ5Selected,
                )
                5 -> SatisfactionSlider(
                    value = uiState.q6Satisfaction,
                    onValueChange = viewModel::onQ6Changed,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ---------------- Footer nav buttons ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = viewModel::onPrevStep,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    ) {
                        Text("Kembali")
                    }
                }

                val isLast = uiState.currentStep == uiState.totalSteps - 1
                if (!isLast) {
                    Button(
                        onClick = viewModel::onNextStep,
                        enabled = uiState.isCurrentStepValid(),
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    ) {
                        Text("Lanjut")
                    }
                } else {
                    Button(
                        onClick = viewModel::onSaveAssessment,
                        enabled = uiState.canSave() && !uiState.isSaving,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Text("Generate Laporan")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables: chip grid (Q5) + slider (Q6)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipChoiceGrid(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    // 2 kolom chip layout via FlowRow equivalent (Column + Row sederhana).
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        label = {
                            Text(
                                option,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
                // Sisipkan spacer kosong kalau baris terakhir cuma 1 item
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SatisfactionSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column {
        Text(
            text = "Skor: $value / 10",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8, // 1,2,3,...,10 → 8 step internal antar tick
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("1\nTidak puas", style = MaterialTheme.typography.bodySmall)
            Text("10\nSangat puas", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        }
    }
}
