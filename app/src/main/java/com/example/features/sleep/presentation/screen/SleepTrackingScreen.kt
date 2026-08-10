package com.example.features.sleep.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.sleep.presentation.state.SleepQuality
import com.example.features.sleep.presentation.viewmodel.SleepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SleepViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val timeRegex = remember { Regex("^([01]\\d|2[0-3]):[0-5]\\d$") }
    val isBedTimeValid = uiState.bedTime.matches(timeRegex)
    val isWakeTimeValid = uiState.wakeUpTime.matches(timeRegex)

    LaunchedEffect(uiState.errorMessage, uiState.isSuccess) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage!!,
                actionLabel = "OK"
            )
            viewModel.onMessageShown()
        } else if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Jurnal tidur berhasil disimpan!"
            )
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Sleep Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Log Your Sleep",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Bed Time Input Mockup
            OutlinedTextField(
                value = uiState.bedTime,
                onValueChange = { viewModel.onBedTimeChanged(it) },
                label = { Text("Bed Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.bedTime.isNotEmpty() && !isBedTimeValid,
                supportingText = {
                    if (uiState.bedTime.isNotEmpty() && !isBedTimeValid) {
                        Text("Use HH:mm, 24-hour (e.g. 22:30)")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Wake Up Time Input Mockup
            OutlinedTextField(
                value = uiState.wakeUpTime,
                onValueChange = { viewModel.onWakeUpTimeChanged(it) },
                label = { Text("Wake Up Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.wakeUpTime.isNotEmpty() && !isWakeTimeValid,
                supportingText = {
                    if (uiState.wakeUpTime.isNotEmpty() && !isWakeTimeValid) {
                        Text("Use HH:mm, 24-hour (e.g. 06:30)")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Total Sleep Duration (Read Only)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Sleep Duration",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.totalSleepDuration,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Sleep Quality
            Text(
                text = "Sleep Quality",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SleepQuality.values().forEach { quality ->
                    FilterChip(
                        selected = uiState.sleepQuality == quality,
                        onClick = { viewModel.onSleepQualityChanged(quality) },
                        label = { Text(quality.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.onSaveClicked() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                enabled = !uiState.isSaving && isBedTimeValid && isWakeTimeValid
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }
        }
    }
}
