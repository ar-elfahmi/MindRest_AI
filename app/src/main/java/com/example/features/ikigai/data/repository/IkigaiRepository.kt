package com.example.features.ikigai.data.repository

import com.example.core.network.SupabaseClient
import com.example.features.ikigai.data.dto.IkigaiAssessmentInsert
import com.example.features.ikigai.data.dto.IkigaiAssessmentRow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.CancellationException

/**
 * Repository untuk fitur Ikigai.
 *
 * Tugas task 2.3+2.4: hanya insert assessment.
 * Pembacaan assessment + report akan ditambah di task 3.3.
 *
 * T-004 (FR-015 Dashboard): tambah [getAssessmentCount] untuk widget
 * "Ikigai Progress" di HomeScreen — single source of truth "sudah pernah
 * assessment?" lewat query COUNT pada `ikigai_assessments` milik user.
 */
interface IkigaiRepository {

    /**
     * Simpan assessment baru. Return id barunya supaya caller
     * (ViewModel) bisa navigate ke loading screen dengan membawa id,
     * yang akan dipakai Edge Function generate-ikigai-report di task 3.1.
     */
    suspend fun insertAssessment(insert: IkigaiAssessmentInsert): Result<String>

    /**
     * Hitung jumlah assessment Ikigai yang dimiliki user saat ini.
     * Digunakan HomeScreen untuk empty-state CTA ("Mulai Ikigai Assessment")
     * atau filled-state "Lihat Laporan" + badge "X assessment".
     *
     * Pakai `Count.EXACT` dari PostgREST header (`Prefer: count=exact`).
     * Return 0 kalau user belum login / Supabase belum konfigurasi — UX
     * tetap menampilkan empty state tanpa crash.
     */
    suspend fun getAssessmentCount(): Result<Int>
}

class IkigaiRepositoryImpl : IkigaiRepository {

    override suspend fun insertAssessment(insert: IkigaiAssessmentInsert): Result<String> {
        return try {
            // insert(...).select() = insert lalu kembalikan row yang barusan dibuat.
            // decodeSingle<T> = decode 1 row hasil response.
            val row = SupabaseClient.requireClient()
                .postgrest["ikigai_assessments"]
                .insert(insert) { select() }
                .decodeSingle<IkigaiAssessmentRow>()
            Result.success(row.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssessmentCount(): Result<Int> {
        return try {
            val userId = currentUserId() ?: return Result.success(0)
            val response = SupabaseClient.requireClient()
                .postgrest["ikigai_assessments"]
                .select {
                    count(Count.EXACT)
                    filter { eq("user_id", userId) }
                }
            // countOrNull() baca header Content-Range — return null kalau
            // server tidak mengirim (mis. tanpa Prefer: count=exact).
            Result.success(response.countOrNull()?.toInt() ?: 0)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun currentUserId(): String? {
        val client = SupabaseClient.client ?: return null
        return client.auth.currentSessionOrNull()?.user?.id
    }
}
