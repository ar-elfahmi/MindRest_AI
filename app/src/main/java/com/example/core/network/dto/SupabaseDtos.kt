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

// ---------------------------------------------------------------------------
// T-007: Profile DTOs (FR-003 — lengkapi/update profil).
//
// Field `display_name` di schema.sql = "full name" user. `email` di-copy
// dari auth.users saat sign-up oleh trigger `on_auth_user_created`.
// `date_of_birth` nullable — kolom ditambahkan oleh migration 006, jadi
// baris lama decode dengan null aman.
//
// ProfileUpdate adalah subset yang boleh di-update dari client (display_name
// + date_of_birth). Kolom lain (id, email, avatar_url, created_at,
// updated_at) di-set server-side via trigger / RLS.
// ---------------------------------------------------------------------------

@Serializable
data class ProfileRow(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProfileUpdate(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
)
