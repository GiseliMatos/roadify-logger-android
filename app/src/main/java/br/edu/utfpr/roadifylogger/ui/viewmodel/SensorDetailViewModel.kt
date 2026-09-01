package br.edu.utfpr.pb.dainf.medicaosensores.ui.sensordetail

import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.MotionSample
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.SensorKind
import br.edu.utfpr.pb.dainf.medicaosensores.data.repository.MotionSensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SensorDetailUiState(
    val kind: SensorKind = SensorKind.ACCELEROMETER,
    val latest: MotionSample = MotionSample(),
    val history: List<MotionSample> = emptyList(),
    val hardwareInfo: HardwareInfo? = null,
)

data class HardwareInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val maxRange: Float,
    val resolution: Float,
    val power: Float,
    val minDelayUs: Int,
    val wakeUpSensor: Boolean,
)

private const val HISTORY_SIZE = 80

class SensorDetailViewModel(
    private val kind: SensorKind,
    private val motionSensorRepository: MotionSensorRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SensorDetailUiState(kind = kind))

    val state: StateFlow<SensorDetailUiState> = _state.asStateFlow()

    init {
        val sensor: Sensor? = when (kind) {
            SensorKind.ACCELEROMETER -> motionSensorRepository.accelerometer()
            SensorKind.GYROSCOPE -> motionSensorRepository.gyroscope()
            SensorKind.MAGNETOMETER -> motionSensorRepository.magnetometer()
            else -> null
        }

        _state.value = _state.value.copy(hardwareInfo = sensor?.toHardwareInfo())

        motionSensorRepository.observe(sensor)
            .onEach { sample ->
                _state.value = _state.value.copy(
                    latest = sample,
                    history = (_state.value.history + sample).takeLast(HISTORY_SIZE),
                )
            }
            .launchIn(viewModelScope)
    }
}

private fun Sensor.toHardwareInfo() = HardwareInfo(
    name = name,
    vendor = vendor,
    version = version,
    maxRange = maximumRange,
    resolution = resolution,
    power = power,
    minDelayUs = minDelay,
    wakeUpSensor = isWakeUpSensor,
)
