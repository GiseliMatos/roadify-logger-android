package br.edu.utfpr.roadifylogger.ui.viewmodel

import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.MotionSample
import br.edu.utfpr.roadifylogger.data.model.GpsSample
import br.edu.utfpr.roadifylogger.data.model.BatteryStatus
import br.edu.utfpr.roadifylogger.data.model.PressureSample
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import br.edu.utfpr.roadifylogger.data.repository.MotionSensorRepository
import br.edu.utfpr.roadifylogger.data.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SensorDetailUiState(
    val kind: SensorKind = SensorKind.ACCELEROMETER,
    val latest: MotionSample = MotionSample(),
    val history: List<MotionSample> = emptyList(),
    val pressure: PressureSample = PressureSample(),
    val pressureHistory: List<Float> = emptyList(),
    val gps: GpsSample? = null,
    val microphoneAmplitude: Int = 0,
    val isRecordingAudio: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val battery: BatteryStatus = BatteryStatus(),
    val isAvailable: Boolean = true,
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

class SensorDetailViewModel(
    private val kind: SensorKind,
    private val motionSensorRepository: MotionSensorRepository,
    private val recordingRepository: RecordingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SensorDetailUiState(kind = kind))

    val state: StateFlow<SensorDetailUiState> = _state.asStateFlow()

    init {
        val sensor: Sensor? = when (kind) {
            SensorKind.ACCELEROMETER -> motionSensorRepository.accelerometer()
            SensorKind.GYROSCOPE -> motionSensorRepository.gyroscope()
            SensorKind.MAGNETOMETER -> motionSensorRepository.magnetometer()
            SensorKind.BAROMETER -> motionSensorRepository.barometer()
            else -> null
        }

        val usesAndroidSensor = kind in setOf(
            SensorKind.ACCELEROMETER,
            SensorKind.GYROSCOPE,
            SensorKind.MAGNETOMETER,
            SensorKind.BAROMETER,
        )
        _state.value = _state.value.copy(
            hardwareInfo = sensor?.toHardwareInfo(),
            isAvailable = !usesAndroidSensor || sensor != null,
        )

        recordingRepository.state
            .onEach { recording ->
                _state.value = when (kind) {
                    SensorKind.ACCELEROMETER -> _state.value.copy(
                        latest = recording.accel,
                        history = recording.accelHistory,
                    )
                    SensorKind.GYROSCOPE -> _state.value.copy(
                        latest = recording.gyro,
                        history = recording.gyroHistory,
                    )
                    SensorKind.MAGNETOMETER -> _state.value.copy(
                        latest = recording.magnetometer,
                        history = recording.magnetometerHistory,
                    )
                    SensorKind.BAROMETER -> _state.value.copy(
                        pressure = recording.pressure,
                        pressureHistory = recording.pressureHistory,
                    )
                    SensorKind.GPS -> _state.value.copy(gps = recording.gps)
                    SensorKind.CAMERA -> _state.value.copy(isRecordingVideo = recording.isRecordingVideo)
                    SensorKind.MICROPHONE -> _state.value.copy(
                        microphoneAmplitude = recording.micAmplitude,
                        isRecordingAudio = recording.isRecordingAudio,
                    )
                    SensorKind.BATTERY_LEVEL,
                    SensorKind.BATTERY_TEMPERATURE -> _state.value.copy(battery = recording.battery)
                }
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
