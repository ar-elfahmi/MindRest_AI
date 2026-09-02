package com.example.features.journal.data.repository

import com.example.core.network.dto.JournalEntryRow
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression test untuk bug "history chat tidak dikelompokkan per sesi":
 * semua pesan dengan session_id yang sama harus menjadi SATU ChatSessionSummary.
 */
class ChatSessionGroupingTest {

    private fun row(
        id: String,
        content: String,
        createdAt: String,
        sessionId: String?,
        role: String?,
    ) = JournalEntryRow(
        id = id,
        userId = "user-1",
        content = content,
        createdAt = createdAt,
        sessionId = sessionId,
        role = role,
        parentId = null,
    )

    @Test
    fun `messages with same session_id are grouped into one session`() {
        val rows = listOf(
            row("a1", "Aku sedang cemas", "2026-01-01T10:00:00Z", "sess-1", "user"),
            row("a2", "Ceritakan lebih banyak", "2026-01-01T10:00:05Z", "sess-1", "assistant"),
            row("a3", "Cemas karena kerjaan", "2026-01-01T10:01:00Z", "sess-1", "user"),
            row("a4", "Mari kita urai", "2026-01-01T10:01:10Z", "sess-1", "assistant"),
        )

        val sessions = groupChatSessions(rows)

        assertEquals(1, sessions.size)
        val session = sessions.first()
        assertEquals("sess-1", session.sessionId)
        assertEquals(4, session.messageCount)
        assertEquals("Aku sedang cemas", session.firstUserMessage)
        assertEquals("Mari kita urai", session.lastMessage)
        assertEquals("2026-01-01T10:00:00Z", session.startedAt)
        assertEquals("2026-01-01T10:01:10Z", session.lastMessageAt)
    }

    @Test
    fun `different sessions produce separate summaries ordered by latest activity`() {
        val rows = listOf(
            row("b1", "Chat lama", "2026-01-01T09:00:00Z", "sess-old", "user"),
            row("b2", "Balasan lama", "2026-01-01T09:00:05Z", "sess-old", "assistant"),
            row("b3", "Chat baru", "2026-01-02T08:00:00Z", "sess-new", "user"),
            row("b4", "Balasan baru", "2026-01-02T08:00:07Z", "sess-new", "assistant"),
        )

        val sessions = groupChatSessions(rows)

        assertEquals(2, sessions.size)
        assertEquals("sess-new", sessions[0].sessionId)
        assertEquals("sess-old", sessions[1].sessionId)
    }

    @Test
    fun `legacy entries without session or role are ignored`() {
        val rows = listOf(
            row("c1", "Jurnal manual", "2026-01-01T07:00:00Z", sessionId = null, role = null),
            row("c2", "Chat user", "2026-01-01T08:00:00Z", "sess-1", "user"),
            row("c3", "row tanpa role", "2026-01-01T08:00:01Z", "sess-2", role = null),
        )

        val sessions = groupChatSessions(rows)

        assertEquals(1, sessions.size)
        assertEquals("sess-1", sessions.first().sessionId)
    }
}
