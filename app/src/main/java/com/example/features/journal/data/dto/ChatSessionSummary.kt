package com.example.features.journal.data.dto

/**
 * Ringkasan satu sesi chat AI journaling.
 *
 * Di-group dari `journal_entries` by `session_id` — satu sesi = satu kartu
 * di Riwayat Jurnal (bukan satu kartu per pesan).
 */
data class ChatSessionSummary(
    val sessionId: String,
    /** Timestamp pesan pertama sesi (ISO 8601). */
    val startedAt: String,
    /** Timestamp pesan terakhir sesi (ISO 8601). */
    val lastMessageAt: String,
    val messageCount: Int,
    /** Pesan user pertama — dipakai sebagai judul/preview sesi. */
    val firstUserMessage: String?,
    /** Pesan terakhir (biasanya balasan AI) — dipakai sebagai preview. */
    val lastMessage: String?,
)
