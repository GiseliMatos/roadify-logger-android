package br.edu.utfpr.roadifylogger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.edu.utfpr.roadifylogger.data.model.LevelState
import br.edu.utfpr.roadifylogger.data.sensor.OrientationSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Controla os dados, a leitura do sensor e a calibração da Tela de Nível Bolha.
class LevelViewModel(application: Application) : AndroidViewModel(application) {

    private val orientationSensor = OrientationSensor(application)

    private val _state = MutableStateFlow(LevelState())
    val state: StateFlow<LevelState> = _state.asStateFlow()

    private var currentRoll = 0f
    private var currentPitch = 0f

    private var calibrationRoll = 0f
    private var calibrationPitch = 0f

    private var isListening = false

    fun startListening() {
        if (isListening) {
            return
        }

        val sensorStarted = orientationSensor.startListening { roll, pitch ->
            currentRoll = roll
            currentPitch = pitch

            _state.update { currentState ->
                currentState.copy(
                    roll = currentRoll - calibrationRoll,
                    pitch = currentPitch - calibrationPitch
                )
            }
        }

        isListening = sensorStarted

        _state.update { currentState ->
            currentState.copy(
                isSensorAvailable = sensorStarted
            )
        }
    }

    fun stopListening() {
        if (!isListening) {
            return
        }

        orientationSensor.stopListening()
        isListening = false
    }

    fun calibrate() {
        calibrationRoll = currentRoll
        calibrationPitch = currentPitch

        _state.update { currentState ->
            currentState.copy(
                roll = 0f,
                pitch = 0f,
                isCalibrated = true
            )
        }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}