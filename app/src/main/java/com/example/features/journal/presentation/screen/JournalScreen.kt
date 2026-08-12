package com.example.features.journal.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.LocalSpacing
import com.example.core.designsystem.components.AppScaffold
import com.example.core.designsystem.components.screenEdgePadded
import com.example.features.journal.presentation.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val spacing = LocalSpacing.current

    LaunchedEffect(uiState.errorMessage, uiState.isSuccess) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage!!,
                actionLabel = "OK"
            )
            viewModel.onMessageShown()
        } else if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Jurnal berhasil disimpan!"
            )
            viewModel.onMessageShown()
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Daily Journal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .screenEdgePadded(),
            verticalArrangement = Arrangement.spacedBy(spacing.space4)
        ) {
            OutlinedTextField(
                value = uiState.journalText,
                onValueChange = { viewModel.onTextChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes up most of the screen
                placeholder = { Text("Tuliskan apa yang ada di pikiranmu hari ini...") },
                enabled = !uiState.isSaving
            )

            Button(
                onClick = { viewModel.onSaveEntryClicked() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(spacing.space4),
                enabled = !uiState.isSaving && uiState.journalText.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(spacing.space2))
                    Text("Saving...")
                } else {
                    Text("Save Entry")
                }
            }
        }
    }
}
