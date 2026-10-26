package br.edu.utfpr.roadifylogger.ui.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.RecordingSession
import br.edu.utfpr.roadifylogger.data.repository.SessionFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun exportSession(
        session: RecordingSession,
        destinationUri: Uri
    ) {
        viewModelScope.launch {
            try {
                sessionFileRepository.exportSession(
                    session = session,
                    destinationTreeUri = destinationUri
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.localizedMessage
                        ?: "Erro ao exportar a coleta."
                )
            }
        }
    }



    fun exportToDownloads(
        session: RecordingSession,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    sessionFileRepository.exportSessionToDownloads(session)
                }

                onSuccess()

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.localizedMessage
                        ?: "Erro ao exportar a coleta."
                )
            }
        }
    }

}
