package com.example.features.authentication.data.repository

import com.example.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak untuk operasi autentikasi (sign-in, sign-up, sign-out).
 *
 * Implementasi default [AuthRepositoryImpl] membungkus Supabase Auth API
 * (`auth.signInWith(Email)`, `auth.signUpWith(Email)`, `auth.signOut()`)
 * dan mengembalikan [Result] agar ViewModel bisa menampilkan error
 * ramah tanpa harus menangani exception secara eksplisit.
 */
interface AuthRepository {
    /** Status sesi Supabase saat ini — dipancarkan via StateFlow. */
    val sessionStatus: StateFlow<SessionStatus>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}

class AuthRepositoryImpl : AuthRepository {

    private val client get() = SupabaseClient.requireClient()

    override val sessionStatus: StateFlow<SessionStatus>
        get() = client.auth.sessionStatus

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
        Unit
    }
}
