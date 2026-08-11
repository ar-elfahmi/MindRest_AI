package com.example.features.ikigai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SupabaseClient
import com.example.features.ikigai.data.dto.IkigaiAssessmentInsert
import com.example.features.ikigai.data.repository.IkigaiRepository
import com.example.features.ikigai.data.repository.IkigaiRepositoryImpl
import com.example.features.ikigai.presentation.state.IkigaiAssessmentUiState
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk 6-step Ikigai assessment.
 *
 * Flow:
 *   user isi 6 pertanyaan → onSaveAssessment() insert ke ikigai_assessments
 *   → savedAssessmentId terisi → screen navigate ke loading placeholder
 *   (Edge Function generate-ikigai-report menyusul di TASK 3.1).
 */
class IkigaiAssessmentViewModel(
    private val repository: IkigaiRepository = IkigaiRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(IkigaiAssessmentUiState())
    val uiState: StateFlow<IkigaiAssessmentUiState> = _uiState.asStateFlow()

    // ---------------- Step navigation ----------------

    fun onNextStep() {
        _uiState.update {
            if (it.isCurrentStepValid() && it.currentStep < it.totalSteps - 1) {
                it.copy(currentStep = it.currentStep + 1)
            } else it
        }
    }

    fun onPrevStep() {
        _uiState.update {
            if (it.currentStep > 0) it.copy(currentStep = it.currentStep - 1) else it
        }
    }

    fun onJumpToStep(step: Int) {
        _uiState.update {
            if (step in 0 until it.totalSteps) it.copy(currentStep = step) else it
        }
    }

    // ---------------- Field updates ----------------

    fun onQ1Changed(v: String) = _uiState.update { it.copy(q1Passion = v) }
    fun onQ2Changed(v: String) = _uiState.update { it.copy(q2Skill = v) }
    fun onQ3Changed(v: String) = _uiState.update { it.copy(q3Profession = v) }
    fun onQ4Changed(v: String) = _uiState.update { it.copy(q4Mission = v) }
    fun onQ5Selected(v: String) = _uiState.update { it.copy(q5Overthinking = v) }
    fun onQ6Changed(v: Int) = _uiState.update { it.copy(q6Satisfaction = v.coerceIn(1, 10)) }

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ---------------- Save ----------------

    fun onSaveAssessment() {
        viewModelScope.launch {
            val s = _uiState.value
            if (!s.canSave()) {
                _uiState.update {
                    it.copy(errorMessage = "Lengkapi semua pertanyaan dulu ya.")
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Supabase belum dikonfigurasi. Isi .env lalu rebuild."
                    )
                }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "User not logged in")
                }
                return@launch
            }

            val insert = IkigaiAssessmentInsert(
                userId = userId,
                q1Passion = s.q1Passion.trim(),
                q2Skill = s.q2Skill.trim(),
                q3Profession = s.q3Profession.trim(),
                q4Mission = s.q4Mission.trim(),
                q5Overthinking = s.q5Overthinking!!,
                q6Satisfaction = s.q6Satisfaction,
            )

            val result = repository.insertAssessment(insert)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isSaving = false,
                        errorMessage = null,
                        savedAssessmentId = result.getOrNull(),
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Gagal menyimpan assessment. Coba lagi.",
                    )
                }
            }
        }
    }
}
