package br.edu.utfpr.pb.dainf.medicaosensores.ui.files

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.RecordingSession
import br.edu.utfpr.pb.dainf.medicaosensores.data.repository.SessionFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FilesUiState(
    val sessions: List<RecordingSession> = emptyList(),
    val isLoading: Boolean = true,
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

    fun shareIntent(session: RecordingSession): Intent = sessionFileRepository.shareIntent(session)
}
