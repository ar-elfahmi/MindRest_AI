package com.example.features.journal.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.designsystem.MindRestTheme
import com.example.core.network.dto.JournalEntryRow
import com.example.features.journal.presentation.viewmodel.JournalViewModel

/**
 * T-003: AI Journaling Chatbot (FR-009, FR-011).
 *
 * Wired ke [JournalViewModel]:
 *   - chatMessages     → render list (user + assistant)
 *   - chatSessionId    → UUID sesi aktif (auto-generated saat pertama load)
 *   - isSendingMessage → loading indicator saat panggil Edge Function
 *   - chatError        → snackbar / banner error
 *
 * Behavior:
 *   1. Screen dibuka → ViewModel.start sesi baru (kalau belum ada).
 *   2. User ketik → tap Send → ViewModel.save user message + call chat-gemini
 *      + save AI reply → state update otomatis.
 *   3. Loading indicator saat `isSendingMessage`. Disable input saat loading.
 *   4. Error tampil sebagai snackbar (auto-dismiss via [onChatErrorShown]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiJournalScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var inputText by remember { mutableStateOf("") }

    // Inisialisasi sesi chat: kalau belum ada sessionId, start baru.
    LaunchedEffect(Unit) {
        if (state.chatSessionId == null) {
            viewModel.onStartNewChatSession()
        }
    }

    // Tampilkan error sebagai snackbar.
    LaunchedEffect(state.chatError) {
        state.chatError?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg, actionLabel = "OK")
            viewModel.onChatErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ruang Refleksi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Therapist",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Chat Message List.
            // Tampilkan placeholder kalau belum ada pesan sama sekali.
            if (state.chatMessages.isEmpty() && !state.isLoadingChatHistory) {
                EmptyChatHint(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    reverseLayout = false,
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(state.chatMessages, key = { it.id }) { row ->
                        ChatMessageRow(row = row)
                    }
                    // Loading indicator inline saat menunggu AI reply.
                    if (state.isSendingMessage) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sedang merenungkan…",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Prompts Row.
            QuickPromptsRow(
                onPromptClick = { prompt ->
                    inputText = prompt
                },
            )

            // Bottom Input Bar.
            ChatInputBar(
                inputText = inputText,
                onInputTextChanged = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank() && !state.isSendingMessage) {
                        viewModel.onSendMessage(inputText)
                        inputText = ""
                    }
                },
                isSending = state.isSendingMessage,
            )
        }
    }
}

/** Tampilan row pesan (user atau assistant). */
@Composable
private fun ChatMessageRow(row: JournalEntryRow) {
    val isFromAi = row.role == "assistant"
    ChatMessageBubble(
        text = row.content,
        isFromAi = isFromAi,
    )
}

@Composable
private fun ChatMessageBubble(
    text: String,
    isFromAi: Boolean,
) {
    val alignment = if (isFromAi) Alignment.Start else Alignment.End
    val backgroundColor = if (isFromAi) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
    val contentColor = if (isFromAi) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
    val shape = if (isFromAi) {
        RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 2.dp, 16.dp, 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(0.85f),
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EmptyChatHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ruang Refleksi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ceritakan apa yang sedang kamu rasakan. Aku di sini untuk mendengarkan tanpa menghakimi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuickPromptsRow(
    onPromptClick: (String) -> Unit,
) {
    val prompts = listOf("Pikiranku kacau", "Aku takut gagal", "Bantu aku tenang")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        prompts.forEach { prompt ->
            SuggestionChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { /* Handle Voice */ },
            enabled = !isSending,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = onInputTextChanged,
            placeholder = { Text(if (isSending) "Menunggu respons…" else "Ceritakan pelan-pelan…") },
            modifier = Modifier.weight(1f),
            enabled = !isSending,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
        )

        if (inputText.isNotBlank() && !isSending) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                )
            }
        }
    }
}

@Preview
@Composable
fun AiJournalScreenPreview() {
    MindRestTheme {
        AiJournalScreen(onNavigateBack = {})
    }
}
