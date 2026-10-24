package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.util.Log
import br.edu.utfpr.roadifylogger.data.model.BatteryStatus
import br.edu.utfpr.roadifylogger.data.model.ColetaEntity
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import br.edu.utfpr.roadifylogger.data.model.GpsSample
import br.edu.utfpr.roadifylogger.data.model.MotionSample
import br.edu.utfpr.roadifylogger.data.model.PressureSample
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import br.edu.utfpr.roadifylogger.data.model.enabledSensors
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
import kotlinx.coroutines.withContext

private const val TAG = "RecordingRepository"
private const val HISTORY_SIZE = 60

/** Intervalo de escrita dos buffers em disco (1 segundo). */
private const val FLUSH_INTERVAL_MS = 1000L

/** Cabeçalhos exigidos pelo Backend do Roadify */
private const val ACC_CSV_HEADER = "x;y;z;date\n"
private const val MAG_CSV_HEADER = "x;y;z;date\n"
private const val GPS_CSV_HEADER = "lat;lon;date\n"

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

class RecordingRepository(
    context: Context,
    private val database: br.edu.utfpr.roadifylogger.data.database.DatabaseInstance.AppDatabase,
    private val settingsRepository: SettingsRepository,
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

    // Buffers independentes em memória para cada ficheiro CSV
    private val accBuffer = StringBuilder()
    private val magBuffer = StringBuilder()
    private val gpsBuffer = StringBuilder()

    private var accFile: File? = null
    private var magFile: File? = null
    private var gpsFile: File? = null
    private var sessionStartMs = 0L
    private var coletaId: Long? = null

    private var gpsJob: Job? = null
    private var flushJob: Job? = null
    private var elapsedJob: Job? = null
    private var micAmplitudeJob: Job? = null
    private var livePreviewStarted = false

    init {
        startLivePreview()
    }

    private fun startLivePreview() {
        if (livePreviewStarted) return
        livePreviewStarted = true

        motionSensorRepository.observe(motionSensorRepository.accelerometer())
            .onEach { sample ->
                _state.update {
                    it.copy(accel = sample, accelHistory = (it.accelHistory + sample).takeLast(HISTORY_SIZE))
                }
                if (_state.value.isRecording) appendAccRow(sample)
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
                if (_state.value.isRecording) appendMagRow(sample)
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

    fun startGpsPreviewIfPermitted() {
        if (gpsJob != null || !locationRepository.hasLocationPermission()) return
        gpsJob = locationRepository.observe()
            .onEach { fix ->
                _state.update {
                    val wasInitialGpsNull = it.gps == null && it.isRecording
                    val updated = it.copy(gps = fix, hasLocationFix = true)

                    if (wasInitialGpsNull && coletaId != null) {
                        repoScope.launch(Dispatchers.IO) {
                            try {
                                val coleta = databaseColetaDao().buscarPorId(coletaId!!)
                                if (coleta != null && coleta.latitudeInicio == null) {
                                    databaseColetaDao().atualizar(
                                        coleta.copy(latitudeInicio = fix.latitude, longitudeInicio = fix.longitude)
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Erro ao atualizar GPS inicial", e)
                            }
                        }
                    }
                    updated
                }

                if (_state.value.isRecording && SensorKind.GPS in _state.value.activeSensors) {
                    appendGpsRow(fix)
                }
            }
            .launchIn(repoScope)
    }

    suspend fun start(config: ConfiguracaoEntity) {
        if (_state.value.isRecording) return

        startGpsPreviewIfPermitted()

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

        val initialGps = _state.value.gps
        val locationName = withContext(Dispatchers.IO) {
            LocationNameResolver.resolve(
                context = appContext,
                latitude = initialGps?.latitude,
                longitude = initialGps?.longitude,
            )
        }
        val csvBaseName = LocationNameResolver.asFileName(locationName, folderName)

        // Definição dos ficheiros no padrão do Roadify
        val accCsv = File(dir, "${csvBaseName}_acc.csv")
        val magCsv = File(dir, "${csvBaseName}_mag.csv")
        val gpsCsv = File(dir, "${csvBaseName}_gps.csv")
        accFile = accCsv
        magFile = magCsv
        gpsFile = gpsCsv

        synchronized(accBuffer) { accBuffer.setLength(0) }
        synchronized(magBuffer) { magBuffer.setLength(0) }
        synchronized(gpsBuffer) { gpsBuffer.setLength(0) }

        if (!writeHeader(accCsv, ACC_CSV_HEADER) ||
            !writeHeader(magCsv, MAG_CSV_HEADER) ||
            !writeHeader(gpsCsv, GPS_CSV_HEADER)
        ) {
            _state.update { it.copy(lastError = "Não foi possível criar os arquivos de gravação.") }
            return
        }

        coletaId = try {
            val configuracaoId = settingsRepository.saveForRecording(config)
            databaseColetaDao().inserir(
                ColetaEntity(
                    configuracaoId = configuracaoId,
                    dataHoraInicio = sessionStartMs,
                    latitudeInicio = initialGps?.latitude,
                    longitudeInicio = initialGps?.longitude,
                    nomeArquivoColeta = accCsv.name,
                    caminhoPastaGravacao = dir.absolutePath,
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível registrar a coleta no banco", e)
            _state.update { it.copy(lastError = "Não foi possível registrar a coleta no banco.") }
            null
        }

        _state.update {
            it.copy(
                isRecording = true,
                elapsedSeconds = 0L,
                activeSensors = config.enabledSensors,
                currentFileName = accCsv.name,
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
                _state.update { it.copy(lastError = "Não foi possível iniciar a gravação de áudio.") }
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
                _state.update { it.copy(lastError = "Não foi possível iniciar a gravação de vídeo.") }
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
                flushBuffersToDisk()
            }
        }

    }

    suspend fun stop() {
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

        withContext(Dispatchers.IO) {
            flushBuffersToDisk()

            coletaId?.let { id ->
                try {
                    databaseColetaDao().finalizarColeta(
                        coletaId = id,
                        dataHoraFim = System.currentTimeMillis(),
                        latitudeFim = _state.value.gps?.latitude,
                        longitudeFim = _state.value.gps?.longitude,
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Não foi possível finalizar a coleta no banco", e)
                    _state.update { it.copy(lastError = "Não foi possível finalizar a coleta no banco.") }
                }
            }
        }
        coletaId = null
    }

    fun clearError() {
        _state.update { it.copy(lastError = null) }
    }

    /** Adiciona uma linha de Aceleração ao buffer (x;y;z;date) */
    private fun appendAccRow(sample: MotionSample) {
        val dateStr = csvDateFormatter.format(Date(sample.timestampMs))
        val row = "${sample.x};${sample.y};${sample.z};$dateStr\n"
        synchronized(accBuffer) { accBuffer.append(row) }
    }

    /** Adiciona uma linha de Magnetómetro ao buffer (x;y;z;date) */
    private fun appendMagRow(sample: MotionSample) {
        val dateStr = csvDateFormatter.format(Date(sample.timestampMs))
        val row = "${sample.x};${sample.y};${sample.z};$dateStr\n"
        synchronized(magBuffer) { magBuffer.append(row) }
    }

    /** Adiciona uma linha de GPS ao buffer (lat;lon;date) */
    private fun appendGpsRow(fix: GpsSample) {
        val dateStr = csvDateFormatter.format(Date(fix.timestampMs))
        val row = "${fix.latitude};${fix.longitude};$dateStr\n"
        synchronized(gpsBuffer) { gpsBuffer.append(row) }
    }

    private fun writeHeader(file: File, header: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            FileOutputStream(file, false).use { it.write(header.toByteArray()) }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao criar o arquivo (${file.absolutePath})", e)
            false
        }
    }

    /** Escreve os dados acumulados nos respetivos ficheiros em disco */
    private fun flushBuffersToDisk() {
        flushSingleBuffer(accFile, accBuffer)
        flushSingleBuffer(magFile, magBuffer)
        flushSingleBuffer(gpsFile, gpsBuffer)
    }

    private fun flushSingleBuffer(file: File?, buffer: StringBuilder) {
        if (file == null) return
        val dataToWrite: String
        synchronized(buffer) {
            if (buffer.isEmpty()) return
            dataToWrite = buffer.toString()
            buffer.setLength(0)
        }
        try {
            FileOutputStream(file, true).use { it.write(dataToWrite.toByteArray()) }
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao gravar dados no arquivo (${file.absolutePath})", e)
            _state.update { it.copy(lastError = "Erro ao salvar arquivo: ${e.message}") }
            synchronized(buffer) { buffer.insert(0, dataToWrite) }
        }
    }

    private fun databaseColetaDao() = database.coletaDao()

    companion object {
        private val fileTimestampFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        private val csvDateFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS", Locale.US)
    }
}

private fun computeCompassHeading(sample: MotionSample): Float {
    var degrees = Math.toDegrees(atan2(sample.y.toDouble(), sample.x.toDouble())).toFloat()
    if (degrees < 0f) degrees += 360f
    return degrees
}