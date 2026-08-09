package com.example.features.authentication.presentation.state

/**
 * State untuk layar Sign-In.
 *
 * - [emailError] / [passwordError]: error validasi field-level.
 * - [errorMessage]: error global (mis. kredensial salah, jaringan gagal).
 * - [isSuccess]: flag transient yang di-set setelah sign-in berhasil,
 *   lalu di-consume oleh Screen untuk navigasi ke Home.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

/**
 * State untuk layar Sign-Up. Sama dengan [LoginUiState] plus field
 * `fullName` dan `confirmPassword`.
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)
