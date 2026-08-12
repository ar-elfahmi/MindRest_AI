package com.example.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SupabaseClient
import com.example.core.network.dto.ProfileRow
import com.example.core.network.dto.ProfileUpdate
import com.example.features.profile.presentation.state.ProfileUiState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar Profile (FR-003).
 *
 * Alur:
 * 1. UI observe [uiState].
 * 2. Saat screen pertama tampil, panggil [load] untuk fetch row `profiles`
 *    milik user (auto-create via trigger `on_auth_user_created` saat sign-up).
 * 3. User masuk edit mode → mutate `draftFullName` / `draftDateOfBirth`.
 * 4. Save → call `profiles.update(...)` dengan `ProfileUpdate` payload.
 * 5. Sukses/gagal emit snackbar via `errorMessage` / `infoMessage`.
 *
 * Pola paralel dengan `MoodViewModel` / `SleepViewModel` / `LifestyleViewModel`:
 * - Supabase null-safe (cek `client` sebelum akses)
 * - `viewModelScope.launch` untuk async
 * - `Result` mapping di ViewModel
 * - Snackbar via dismiss callback (`onErrorShown` / `onInfoMessageShown`)
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Ambil profile user saat ini dari tabel `profiles` + email dari
     * `auth.currentUserOrNull()`. Aman dipanggil beberapa kali (idempotent).
     */
    fun load() {
        val client = SupabaseClient.client
        if (client == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Supabase belum dikonfigurasi.",
                )
            }
            return
        }
        val currentUser = client.auth.currentUserOrNull()
        val userId = currentUser?.id
        val email = currentUser?.email.orEmpty()

        if (userId == null) {
            _uiState.update {
                it.copy(
                    email = email,
                    isLoading = false,
                    errorMessage = "User belum login. Silakan sign in.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                client.postgrest.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<ProfileRow>()
            }.onSuccess { row ->
                _uiState.update {
                    it.copy(
                        email = email,
                        profile = row,
                        draftFullName = row?.displayName.orEmpty(),
                        draftDateOfBirth = row?.dateOfBirth,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("ProfileViewModel", "load profile failed", e)
                _uiState.update {
                    it.copy(
                        email = email,
                        isLoading = false,
                        errorMessage = e.friendlyMessage("Gagal memuat profil."),
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(draftFullName = value, errorMessage = null, infoMessage = null)
    }

    fun onDateOfBirthChange(value: String?) = _uiState.update {
        it.copy(draftDateOfBirth = value, errorMessage = null, infoMessage = null)
    }

    fun onEditModeChange(enabled: Boolean) = _uiState.update {
        if (!enabled) {
            // Cancel → reset draft ke nilai tersimpan
            it.copy(
                isEditMode = false,
                draftFullName = it.profile?.displayName.orEmpty(),
                draftDateOfBirth = it.profile?.dateOfBirth,
                errorMessage = null,
                infoMessage = null,
            )
        } else {
            it.copy(isEditMode = true, errorMessage = null, infoMessage = null)
        }
    }

    /** Simpan draft ke tabel `profiles`. Hanya field yang berubah dikirim. */
    fun saveProfile() {
        val state = _uiState.value
        if (!state.hasChanges || state.isSaving) return

        val client = SupabaseClient.client
        if (client == null) {
            _uiState.update { it.copy(errorMessage = "Supabase belum dikonfigurasi.") }
            return
        }
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "User belum login.") }
            return
        }

        val original = state.profile
        val update = ProfileUpdate(
            displayName = state.draftFullName.trim().takeIf {
                it.isNotEmpty() && it != original?.displayName.orEmpty()
            },
            dateOfBirth = state.draftDateOfBirth?.takeIf { it != original?.dateOfBirth },
        )

        // Nothing to update (defensive — hasChanges should catch this)
        if (update.displayName == null && update.dateOfBirth == null) {
            _uiState.update { it.copy(isEditMode = false, infoMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, infoMessage = null) }
            runCatching {
                client.postgrest.from("profiles").update(update) {
                    filter { eq("id", userId) }
                }
            }.onSuccess {
                // Re-fetch to get updated_at + persisted values
                val refreshed = runCatching {
                    client.postgrest.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<ProfileRow>()
                }.getOrNull()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditMode = false,
                        profile = refreshed ?: it.profile,
                        draftFullName = refreshed?.displayName
                            ?: it.draftFullName.trim(),
                        draftDateOfBirth = refreshed?.dateOfBirth ?: it.draftDateOfBirth,
                        infoMessage = "Profil berhasil disimpan.",
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("ProfileViewModel", "save profile failed", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.friendlyMessage("Gagal menyimpan profil."),
                    )
                }
            }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }
    fun onInfoMessageShown() = _uiState.update { it.copy(infoMessage = null) }

    fun onShowSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = true) }
    fun onDismissSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = false) }

    /**
     * Sign-out flow di-trigger dari ProfileScreen setelah user konfirmasi
     * di dialog "Yakin ingin keluar?". Setelah berhasil, sessionStatus di
     * Supabase akan berubah jadi NotAuthenticated → MainActivity's SplashScreen
     * observer auto-navigate ke Login (existing wiring, di luar scope T-007).
     *
     * Error di-handle secara silent — kalau sign-out gagal (jarang), user
     * tetap di ProfileScreen dan bisa coba lagi.
     */
    fun signOut() {
        val client = SupabaseClient.client ?: return
        _uiState.update { it.copy(showSignOutDialog = false) }
        viewModelScope.launch {
            runCatching {
                client.auth.signOut()
            }.onFailure { e ->
                android.util.Log.e("ProfileViewModel", "signOut failed", e)
                _uiState.update {
                    it.copy(errorMessage = "Gagal sign out. Coba lagi.")
                }
            }
        }
    }

    private fun Throwable.friendlyMessage(fallback: String): String {
        val raw = message.orEmpty()
        return when {
            raw.isBlank() -> fallback
            raw.contains("network", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ->
                "Gagal terhubung ke server. Cek koneksi internet."
            else -> raw.take(160)
        }
    }

    companion object {
        fun create(): ProfileViewModel = ProfileViewModel()
    }
}