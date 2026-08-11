package com.example.core.network

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient as SbClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Lazy + null-safe Supabase client.
 *
 * Alasan: object SupabaseClient lama di-inisialisasi saat class pertama kali
 * diakses (eager). Saat BuildConfig.SUPABASE_URL masih placeholder
 * (https://YOUR_PROJECT_REF.supabase.co) dari .env.example, init melempar
 * exception dan membuat app crash.
 *
 * Implementasi ini:
 * - Menunda inisialisasi sampai benar-benar dipakai (lazy).
 * - Mengecek placeholder sebelum membuat client. Jika placeholder,
 *   [client] akan null dan [isConfigured] = false sehingga UI bisa
 *   menampilkan pesan ramah ("Supabase belum dikonfigurasi") alih-alih crash.
 */
object SupabaseClient {

    private const val PLACEHOLDER_URL =
        "https://YOUR_PROJECT_REF.supabase.co"
    private const val PLACEHOLDER_KEY_PREFIX = "your_"

    val isConfigured: Boolean
        get() = isUrlValid(BuildConfig.SUPABASE_URL) &&
                isKeyValid(BuildConfig.SUPABASE_ANON_KEY)

    val client: SbClient? by lazy {
        if (!isConfigured) {
            android.util.Log.w(
                "SupabaseClient",
                "Supabase belum dikonfigurasi. Set SUPABASE_URL & SUPABASE_ANON_KEY di .env. " +
                    "URL=${BuildConfig.SUPABASE_URL}"
            )
            null
        } else {
            runCatching {
                createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
                ) {
                    install(Postgrest)
                    install(Auth)
                    install(Functions)
                }
            }.getOrElse { e ->
                android.util.Log.e(
                    "SupabaseClient",
                    "Gagal membuat Supabase client: ${e.message}", e
                )
                null
            }
        }
    }

    /** Throws jika client belum siap — panggil dari repository / ViewModel. */
    fun requireClient(): SbClient =
        client ?: error(
            "Supabase belum dikonfigurasi. Salin .env.example ke .env dan isi " +
                "SUPABASE_URL & SUPABASE_ANON_KEY dari dashboard Supabase, lalu rebuild."
        )

    private fun isUrlValid(url: String): Boolean =
        url.isNotBlank() &&
            url != PLACEHOLDER_URL &&
            url.startsWith("https://") &&
            url.contains(".supabase.co")

    private fun isKeyValid(key: String): Boolean =
        key.isNotBlank() && !key.startsWith(PLACEHOLDER_KEY_PREFIX)
}
