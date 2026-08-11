package com.example.features.journal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body untuk Edge Function `chat-gemini`.
 *
 * Sesuai API contract di `supabase/functions/chat-gemini/index.ts`:
 *   - message       (WAJIB): pesan user terbaru
 *   - session_id    (OPSIONAL): UUID sesi untuk conversation history
 *   - model         (OPSIONAL): override model Gemini
 *
 * Backend akan fetch history dari `journal_entries` WHERE
 * session_id = ? AND role IS NOT NULL, lalu kirim ke Gemini.
 */
@Serializable
data class ChatGeminiRequest(
    @SerialName("message") val message: String,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("model") val model: String? = null,
)

/**
 * Response sukses dari Edge Function `chat-gemini`.
 *
 * `data` berisi:
 *   - reply         : teks balasan AI
 *   - model         : model Gemini yang dipakai
 *   - session_id    : echo session ID
 *   - history_used  : jumlah history message yang dipakai untuk context
 *   - latency_ms    : waktu eksekusi server
 *   - usage         : token usage (opsional, kalau SDK expose)
 */
@Serializable
data class ChatGeminiResponseData(
    @SerialName("reply") val reply: String,
    @SerialName("model") val model: String,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("history_used") val historyUsed: Int = 0,
    @SerialName("latency_ms") val latencyMs: Int = 0,
    @SerialName("usage") val usage: ChatGeminiUsage? = null,
)

@Serializable
data class ChatGeminiUsage(
    @SerialName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerialName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerialName("totalTokenCount") val totalTokenCount: Int = 0,
)
