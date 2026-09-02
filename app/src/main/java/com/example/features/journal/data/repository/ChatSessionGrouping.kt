package com.example.features.journal.data.repository

import com.example.core.network.dto.JournalEntryRow
import com.example.features.journal.data.dto.ChatSessionSummary

/**
 * Group baris `journal_entries` (chat AI) menjadi daftar sesi.
 *
 * Satu sesi = semua baris dengan `session_id` sama. Baris legacy
 * (tanpa session_id / role) di-skip. Diurutkan dari sesi terbaru.
 */
internal fun groupChatSessions(entries: List<JournalEntryRow>): List<ChatSessionSummary> {
    return entries
        .filter { !it.sessionId.isNullOrBlank() && !it.role.isNullOrBlank() }
        .groupBy { it.sessionId!! }
        .map { (sessionId, rows) ->
            val sorted = rows.sortedBy { it.createdAt }
            ChatSessionSummary(
                sessionId = sessionId,
                startedAt = sorted.firstOrNull()?.createdAt.orEmpty(),
                lastMessageAt = sorted.lastOrNull()?.createdAt.orEmpty(),
                messageCount = sorted.size,
                firstUserMessage = sorted.firstOrNull { it.role == "user" }?.content,
                lastMessage = sorted.lastOrNull()?.content,
            )
        }
        .sortedByDescending { it.lastMessageAt }
}
