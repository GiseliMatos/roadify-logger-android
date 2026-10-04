package br.edu.utfpr.roadifylogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.ColetaSummary
import br.edu.utfpr.roadifylogger.data.repository.SessionFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val isLoading: Boolean = true,
    val summary: ColetaSummary? = null,
    val error: String? = null
)

class SummaryViewModel(
    private val databaseId: Long,
    private val sessionFileRepository: SessionFileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = SummaryUiState(isLoading = true)
            try {
                val summary = sessionFileRepository.getColetaSummaryFromDb(databaseId)
                _uiState.value = SummaryUiState(isLoading = false, summary = summary)
            } catch (e: Exception) {
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar o resumo da coleta"
                )
            }
        }
    }
}
