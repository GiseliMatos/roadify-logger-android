package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.util.Log
import androidx.camera.core.impl.CameraRepository
import br.edu.utfpr.roadifylogger.data.model.AppConfiguration
import br.edu.utfpr.roadifylogger.data.model.BatteryStatus
import br.edu.utfpr.roadifylogger.data.model.GpsSample
import br.edu.utfpr.roadifylogger.data.model.MotionSample
import br.edu.utfpr.roadifylogger.data.model.PressureSample
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "RecordingRepository"
private const val HISTORY_SIZE = 60

/** How often the buffered rows are flushed to disk - mirrors the 1s interval used by the original app. */
private const val FLUSH_INTERVAL_MS = 1000L

private const val CSV_HEADER =
    "timestamp,bateria_temp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,bussola_deg,pressao_hpa,altitude_m,lat,lon\n"

data class RecordingUiState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val accel: MotionSample = MotionSample(),
    val gyro: MotionSample = MotionSample(),
    val magnetometer: MotionSample = MotionSample(),
    val compassDeg: Float = 0f,
    val pressure: PressureSample = PressureSample(),
    val pressureHistory: List<Float> = emptyList(),
    val gps: GpsSample? = null,
    val battery: BatteryStatus = BatteryStatus(),
    val accelHistory: List<MotionSample> = emptyList(),
    val gyroHistory: List<MotionSample> = emptyList(),
    val magnetometerHistory: List<MotionSample> = emptyList(),
    val activeSensors: Set<SensorKind> = emptySet(),
    val currentFileName: String? = null,
    val hasLocationFix: Boolean = false,
    val isRecordingAudio: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val micAmplitude: Int = 0,
    val lastError: String? = null,
)

/**
 * Owns live sensor/GPS/battery monitoring for the dashboard *and* the recording
 * lifecycle. The two are intentionally decoupled:
 *
 *  - Live readings (accel/gyro/gps/battery) are observed continuously from the moment
 *    the repository is created, so the dashboard always reflects the current sensor
 *    state - not just while a recording is in progress.
 *  - [start]/[stop] only control whether those same readings are also being buffered
 *    and periodically flushed to a CSV file on disk, following the original app's
 *    model: accumulate samples in memory, flush the buffer to a [FileOutputStream]
 *    on a fixed interval (see [FLUSH_INTERVAL_MS]), and surface any IO failure
 *    instead of swallowing it.
 */
class RecordingRepository(
    context: Context,
    private val motionSensorRepository: MotionSensorRepository,
    private val locationRepository: LocationRepository,
    private val batteryRepository: BatteryRepository,
    private val audioRepository: AudioRepository,
    private val cameraRepository: CameraRepository,
) {
    private val appContext = context.applicationContext
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(RecordingUiState())
    val state: StateFlow<RecordingUiState> = _state.asStateFlow()

    // Buffer accumulating not-yet-written CSV rows. Guarded by `synchronized(csvBuffer)`
    // since accel/gyro/gps/battery each update it from their own collector coroutine.
    private val csvBuffer = StringBuilder()

    private var csvFile: File? = null
    private var sessionStartMs = 0L

    private var gpsJob: Job? = null
    private var flushJob: Job? = null
    private var elapsedJob: Job? = null
    private var micAmplitudeJob: Job? = null
    private var livePreviewStarted = false

    init {
        startLivePreview()
    }

    /** Begins continuous accel/gyro/battery observation. Safe to call multiple times. */
    private fun startLivePreview() {
        if (livePreviewStarted) return
        livePreviewStarted = true

        motionSensorRepository.observe(motionSensorRepository.accelerometer())
            .onEach { sample ->
                _state.update {
                    it.copy(accel = sample, accelHistory = (it.accelHistory + sample).takeLast(HISTORY_SIZE))
                }
                if (_state.value.isRecording) appendRow()
            }
            .launchIn(repoScope)

        motionSensorRepository.observe(motionSensorRepository.gyroscope())
            .onEach { sample ->
                _state.update {
                    it.copy(gyro = sample, gyroHistory = (it.gyroHistory + sample).takeLast(HISTORY_SIZE))
                }
            }
            .launchIn(repoScope)

        motionSensorRepository.observe(motionSensorRepository.magnetometer())
            .onEach { sample ->
                _state.update {
                    it.copy(
                        magnetometer = sample,
                        magnetometerHistory = (it.magnetometerHistory + sample).takeLast(HISTORY_SIZE),
                        compassDeg = computeCompassHeading(sample),
                    )
                }
            }
            .launchIn(repoScope)

        motionSensorRepository.observePressure(motionSensorRepository.barometer())
            .onEach { sample ->
                _state.update {
                    it.copy(
                        pressure = sample,
                        pressureHistory = (it.pressureHistory + sample.hectopascals).takeLast(HISTORY_SIZE),
                    )
                }
            }
            .launchIn(repoScope)

        batteryRepository.observe()
            .onEach { battery -> _state.update { it.copy(battery = battery) } }
            .launchIn(repoScope)

        startGpsPreviewIfPermitted()
    }

    /** Call after a location-permission grant so GPS readings start flowing without an app restart. */
    fun startGpsPreviewIfPermitted() {
        if (gpsJob != null || !locationRepository.hasLocationPermission()) return
        gpsJob = locationRepository.observe()
            .onEach { fix -> _state.update { it.copy(gps = fix, hasLocationFix = true) } }
            .launchIn(repoScope)
    }

    fun start(config: AppConfiguration) {
        if (_state.value.isRecording) return

        sessionStartMs = System.currentTimeMillis()
        val folderName = fileTimestampFormatter.format(Date(sessionStartMs))
        val sessionsRoot = File(appContext.getExternalFilesDir(null), "sessions")
        val dir = File(sessionsRoot, folderName)
        val dirCreated = dir.mkdirs()
        if (!dirCreated && !dir.exists()) {
            Log.e(TAG, "Não foi possível criar o diretório da coleta: ${dir.absolutePath}")
            _state.update { it.copy(lastError = "Não foi possível criar a pasta de gravação.") }
            return
        }

        val csv = File(dir, "$folderName.csv")
        csvFile = csv
        synchronized(csvBuffer) { csvBuffer.setLength(0) }
        if (!writeHeader(csv)) {
            _state.update { it.copy(lastError = "Não foi possível criar o arquivo de gravação.") }
            return
        }

        _state.update {
            it.copy(
                isRecording = true,
                elapsedSeconds = 0L,
                activeSensors = config.enabledSensors,
                currentFileName = csv.name,
                isRecordingAudio = false,
                isRecordingVideo = false,
                micAmplitude = 0,
                lastError = null,
            )
        }

        if (SensorKind.MICROPHONE in config.enabledSensors) {
            val audioStarted = audioRepository.start(File(dir, "audio.m4a"))
            _state.update { it.copy(isRecordingAudio = audioStarted) }
            if (!audioStarted) {
                _state.update { it.copy(lastError = "Não foi possível iniciar a gravação de áudio (permissão ou microfone em uso).") }
            } else {
                micAmplitudeJob = repoScope.launch {
                    while (isActive) {
                        delay(200)
                        _state.update { it.copy(micAmplitude = audioRepository.currentAmplitude()) }
                    }
                }
            }
        }

        if (SensorKind.CAMERA in config.enabledSensors) {
            val videoStarted = cameraRepository.startRecording(File(dir, "video.mp4"))
            _state.update { it.copy(isRecordingVideo = videoStarted) }
            if (!videoStarted) {
                _state.update { it.copy(lastError = "Não foi possível iniciar a gravação de vídeo (permissão ou câmera indisponível).") }
            }
        }

        elapsedJob = repoScope.launch {
            while (isActive) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = (System.currentTimeMillis() - sessionStartMs) / 1000) }
            }
        }

        flushJob = repoScope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flushBufferToDisk()
            }
        }
    }

    fun stop() {
        if (!_state.value.isRecording) return

        flushJob?.cancel()
        flushJob = null
        elapsedJob?.cancel()
        elapsedJob = null
        micAmplitudeJob?.cancel()
        micAmplitudeJob = null

        audioRepository.stop()
        cameraRepository.stopRecording()

        _state.update { it.copy(isRecording = false, isRecordingAudio = false, isRecordingVideo = false, micAmplitude = 0) }

        // Final flush so the last buffered rows aren't lost.
        repoScope.launch { flushBufferToDisk() }
    }

    fun clearError() {
        _state.update { it.copy(lastError = null) }
    }

    /** Appends one CSV row (driven by each new accelerometer sample) to the in-memory buffer. */
    private fun appendRow() {
        val s = _state.value
        val row = listOf(
            System.currentTimeMillis().toString(),
            s.battery.temperatureCelsius.toString(),
            s.accel.x.toString(), s.accel.y.toString(), s.accel.z.toString(),
            s.gyro.x.toString(), s.gyro.y.toString(), s.gyro.z.toString(),
            s.compassDeg.toString(),
            s.pressure.hectopascals.toString(),
            s.pressure.altitudeMeters.toString(),
            s.gps?.latitude?.toString().orEmpty(),
            s.gps?.longitude?.toString().orEmpty(),
        ).joinToString(",") + "\n"
        synchronized(csvBuffer) { csvBuffer.append(row) }
    }

    /** Writes the CSV header, matching the original app's create-or-append [FileOutputStream] pattern. */
    private fun writeHeader(file: File): Boolean {
        return try {
            file.parentFile?.mkdirs()
            FileOutputStream(file, false).use { it.write(CSV_HEADER.toByteArray()) }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao criar o arquivo de gravação (${file.absolutePath})", e)
            false
        }
    }

    /** Flushes the in-memory buffer to disk in append mode - same pattern as the original saveFile(). */
    private fun flushBufferToDisk() {
        val file = csvFile ?: return
        val dataToWrite: String
        synchronized(csvBuffer) {
            if (csvBuffer.isEmpty()) return
            dataToWrite = csvBuffer.toString()
            csvBuffer.setLength(0)
        }
        try {
            FileOutputStream(file, true).use { it.write(dataToWrite.toByteArray()) }
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao gravar dados no arquivo (${file.absolutePath})", e)
            _state.update { it.copy(lastError = "Erro ao salvar arquivo: ${e.message}") }
            // Put the data back so we retry on the next flush instead of losing it.
            synchronized(csvBuffer) { csvBuffer.insert(0, dataToWrite) }
        }
    }

    companion object {
        private val fileTimestampFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    }
}

/**
 * Rough (non-tilt-compensated) compass heading from the raw magnetometer vector.
 * Good enough for a live dashboard readout; a tilt-compensated heading using the
 * accelerometer + [android.hardware.SensorManager.getRotationMatrix] is a natural
 * follow-up once the device orientation UX is defined.
 */
private fun computeCompassHeading(sample: MotionSample): Float {
    var degrees = Math.toDegrees(atan2(sample.y.toDouble(), sample.x.toDouble())).toFloat()
    if (degrees < 0f) degrees += 360f
    return degrees
}
