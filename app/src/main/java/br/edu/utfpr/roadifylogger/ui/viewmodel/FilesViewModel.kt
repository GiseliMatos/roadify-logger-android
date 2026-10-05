package br.edu.utfpr.roadifylogger.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.RecordingSession
import br.edu.utfpr.roadifylogger.data.repository.SessionFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FilesUiState(
    val sessions: List<RecordingSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FilesViewModel(
    private val sessionFileRepository: SessionFileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FilesUiState())
    val state: StateFlow<FilesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val sessions = sessionFileRepository.listSessions()
            _state.value = FilesUiState(sessions = sessions, isLoading = false)
        }
    }

    fun delete(session: RecordingSession) {
        viewModelScope.launch {
            sessionFileRepository.delete(session)
            refresh()
        }
    }

    fun rename(session: RecordingSession, newName: String) {
        viewModelScope.launch {
            try {
                sessionFileRepository.renameSession(session.databaseId, newName)
                refresh()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.localizedMessage ?: "Erro ao renomear o arquivo"
                )
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            sessionFileRepository.deleteAll()
            refresh()
        }
    }

    fun clearErrorMessage() {
        _state.value = _state.value.copy(error = null)
    }

    fun shareIntent(session: RecordingSession): Intent = sessionFileRepository.shareIntent(session)
}
