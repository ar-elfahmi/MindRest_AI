package com.example.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalEntryInsert(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("content") val content: String,
    // T-003: kolom baru untuk chat history. Opsional (legacy full-entry
    // tidak pakai). Default null di JSON agar backward-compatible dengan
    // baris lama yang belum punya kolom ini.
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
)

@Serializable
data class SleepLogInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("bed_time") val bedTime: String,
    @SerialName("wake_up_time") val wakeUpTime: String,
    @SerialName("sleep_quality") val sleepQuality: String
)

@Serializable
data class MoodLogInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("mood_score") val moodScore: Int
)

// ---------------------------------------------------------------------------
// READ response models — `createdAt` disimpan sebagai ISO 8601 string.
// Decode ke LocalDateTime / Instant dilakukan di ViewModel.
// ---------------------------------------------------------------------------

@Serializable
data class MoodLogRow(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("mood_score") val moodScore: Int,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SleepLogRow(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("bed_time") val bedTime: String,
    @SerialName("wake_up_time") val wakeUpTime: String,
    @SerialName("sleep_quality") val sleepQuality: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class JournalEntryRow(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: String,
    // T-003: kolom baru untuk chat history. Nullable agar decode tetap
    // bekerja untuk baris lama (legacy full-entry).
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
)
