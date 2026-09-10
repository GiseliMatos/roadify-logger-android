package br.edu.utfpr.roadifylogger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.BubbleViscosity
import br.edu.utfpr.roadifylogger.data.model.LevelDisplayType
import br.edu.utfpr.roadifylogger.data.model.LevelOptionsState
import br.edu.utfpr.roadifylogger.data.preferences.LevelPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Controla as opções do nível bolha e envia as alterações para o repositório.
class LevelOptionsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        LevelPreferencesRepository(application)

    val state: StateFlow<LevelOptionsState> =
        repository.options.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LevelOptionsState()
        )

    fun setShowAngle(show: Boolean) {
        viewModelScope.launch {
            repository.setShowAngle(show)
        }
    }

    fun setDisplayType(type: LevelDisplayType) {
        viewModelScope.launch {
            repository.setDisplayType(type)
        }
    }

    fun setLockLevelType(locked: Boolean) {
        viewModelScope.launch {
            repository.setLockLevelType(locked)
        }
    }

    fun setEconomyMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEconomyMode(enabled)
        }
    }

    fun setViscosity(viscosity: BubbleViscosity) {
        viewModelScope.launch {
            repository.setViscosity(viscosity)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }
}