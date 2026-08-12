package com.example.features.profile.presentation.state

import com.example.core.network.dto.ProfileRow

/**
 * State untuk layar Profile.
 *
 * - [profile]: row dari tabel `profiles` hasil query (null saat loading
 *   pertama atau kalau user belum punya row — biasanya auto-create via
 *   trigger `on_auth_user_created`).
 * - [draftFullName] / [draftDateOfBirth]: nilai form yang sedang diedit.
 *   Save button enabled hanya kalau draft ≠ nilai asli (computed: [hasChanges]).
 * - [isSaving]: flag loading saat submit update ke DB.
 * - [errorMessage]: error global (DB/network); null = clear.
 * - [infoMessage]: notifikasi sukses sementara ("Profil berhasil disimpan").
 */
data class ProfileUiState(
    val email: String = "",
    val profile: ProfileRow? = null,
    val draftFullName: String = "",
    val draftDateOfBirth: String? = null, // ISO date string "YYYY-MM-DD" or null
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val showSignOutDialog: Boolean = false,
) {
    /** True kalau draft berbeda dari nilai tersimpan — tombol Save aktif. */
    val hasChanges: Boolean
        get() {
            val originalName = profile?.displayName.orEmpty()
            val originalDob = profile?.dateOfBirth
            return draftFullName.trim() != originalName.trim() ||
                draftDateOfBirth != originalDob
        }

    /** True kalau user sudah sign-in (email terisi). */
    val isAuthenticated: Boolean get() = email.isNotBlank()
}