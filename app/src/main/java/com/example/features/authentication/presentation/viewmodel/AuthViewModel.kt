package com.example.features.authentication.presentation.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.authentication.data.repository.AuthRepository
import com.example.features.authentication.data.repository.AuthRepositoryImpl
import com.example.features.authentication.presentation.state.LoginUiState
import com.example.features.authentication.presentation.state.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar Sign-In.
 *
 * Alur:
 * 1. UI memanggil [onSubmit].
 * 2. ViewModel memvalidasi input lokal (email format, password length).
 * 3. Kalau valid, memanggil [AuthRepository.signIn] di [viewModelScope].
 * 4. Hasil dipetakan ke [LoginUiState.isSuccess] / [LoginUiState.errorMessage].
 */
class LoginViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onSubmit() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        var emailError: String? = null
        var passwordError: String? = null
        var isValid = true

        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        }
        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        }
        if (!isValid) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.signIn(email, password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e ->
                    android.util.Log.e("LoginViewModel", "signIn failed", e)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.friendlySignInMessage())
                    }
                }
        }
    }

    /** Dipanggil Screen setelah navigasi dilakukan agar flag tidak trigger ulang. */
    fun consumeSuccess() = _uiState.update { it.copy(isSuccess = false) }

    private fun Throwable.friendlySignInMessage(): String {
        val raw = message.orEmpty()
        return when {
            raw.contains("Invalid login credentials", ignoreCase = true) ->
                "Incorrect email or password."
            raw.contains("Email not confirmed", ignoreCase = true) ->
                "Please confirm your email first — check your inbox."
            raw.contains("network", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ->
                "Network error. Check your connection and try again."
            raw.isBlank() -> "Sign-in failed. Please try again."
            else -> raw.take(160)
        }
    }

    companion object {
        fun create(): LoginViewModel = LoginViewModel()
    }
}

/**
 * ViewModel untuk layar Sign-Up. Mirip [LoginViewModel] tapi mengirim
 * `signUp` ke Supabase. Validasi tambahan: fullName & confirmPassword.
 */
class RegisterViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = null, errorMessage = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
    }

    fun onSubmit() {
        val state = _uiState.value
        val fullName = state.fullName.trim()
        val email = state.email.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword

        var fullNameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        var isValid = true

        if (fullName.isEmpty()) {
            fullNameError = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            fullNameError = "Full name must be at least 2 characters"
            isValid = false
        }
        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        }
        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }
        if (!isValid) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.signUp(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.friendlySignUpMessage())
                    }
                }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(isSuccess = false) }

    private fun Throwable.friendlySignUpMessage(): String {
        val raw = message.orEmpty()
        return when {
            raw.contains("already registered", ignoreCase = true) ||
                raw.contains("user already", ignoreCase = true) ->
                "This email is already registered. Try signing in instead."
            raw.contains("password", ignoreCase = true) &&
                (raw.contains("weak", ignoreCase = true) ||
                    raw.contains("characters", ignoreCase = true)) ->
                "Password is too weak. Use at least 8 characters."
            raw.contains("network", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ->
                "Network error. Check your connection and try again."
            raw.isBlank() -> "Sign-up failed. Please try again."
            else -> raw.take(160)
        }
    }

    companion object {
        fun create(): RegisterViewModel = RegisterViewModel()
    }
}
