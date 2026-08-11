package com.example.features.ikigai.data.repository

import com.example.core.network.SupabaseClient
import com.example.features.ikigai.data.dto.IkigaiAssessmentInsert
import com.example.features.ikigai.data.dto.IkigaiAssessmentRow
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

/**
 * Repository untuk fitur Ikigai.
 *
 * Tugas task 2.3+2.4: hanya insert assessment.
 * Pembacaan assessment + report akan ditambah di task 3.3.
 */
interface IkigaiRepository {

    /**
     * Simpan assessment baru. Return id barunya supaya caller
     * (ViewModel) bisa navigate ke loading screen dengan membawa id,
     * yang akan dipakai Edge Function generate-ikigai-report di task 3.1.
     */
    suspend fun insertAssessment(insert: IkigaiAssessmentInsert): Result<String>
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
}
