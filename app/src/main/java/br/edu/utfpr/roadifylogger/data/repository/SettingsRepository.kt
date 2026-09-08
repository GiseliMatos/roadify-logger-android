package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.database.DatabaseInstance
import br.edu.utfpr.roadifylogger.data.model.AppConfiguration
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the current [AppConfiguration] (which sensors are enabled, update rates, mount
 * calibration, etc.) for the running process.
 *
 * NOTE: this is currently in-memory only, so settings reset to the defaults in
 * [AppConfiguration] every time the app process is restarted. Persisting this to Room
 * (via [br.edu.utfpr.roadifylogger.data.dao.DataAccessConfiguracoes]) or DataStore is a
 * natural follow-up once the full settings form (sensor toggles, GPS/sensor rates,
 * vehicle info) is wired up in [br.edu.utfpr.roadifylogger.ui.screens.ConfiguracoesScreen].
 */
class SettingsRepository(database: DatabaseInstance.AppDatabase) {

    private val configuracaoDao = database.configuracaoDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _configuration = MutableStateFlow(AppConfiguration())
    val configuration: StateFlow<AppConfiguration> = _configuration.asStateFlow()

    init {
        scope.launch {
            configuracaoDao.buscarUltimaConfiguracao()?.let { entity ->
                _configuration.value = entity.toAppConfiguration()
            }
        }
    }

    fun update(transform: (AppConfiguration) -> AppConfiguration) {
        val updated = transform(_configuration.value)
        _configuration.value = updated
        scope.launch { save(updated) }
    }

    fun setMountCalibration(rollDeg: Float, pitchDeg: Float) {
        update { it.copy(mountCalibrationRollDeg = rollDeg, mountCalibrationPitchDeg = pitchDeg) }
    }

    suspend fun saveForRecording(configuration: AppConfiguration): Long = save(configuration)

    private suspend fun save(configuration: AppConfiguration): Long {
        val existing = configuracaoDao.buscarUltimaConfiguracao()
        val entity = configuration.toEntity(existing?.id ?: 0L)
        return if (existing == null) configuracaoDao.inserir(entity) else {
            configuracaoDao.atualizar(entity)
            existing.id
        }
    }
}

private fun AppConfiguration.toEntity(id: Long) = ConfiguracaoEntity(
    id = id,
    marcaSmartphone = phoneBrand,
    modeloSmartphone = phoneModel,
    posicaoTelefone = phonePosition.name,
    marcaVeiculo = vehicleBrandModel,
    modeloVeiculo = "",
    quilometragemVeiculo = vehicleMileageKm.toFloat(),
    taxaGpsMs = gpsUpdateRateMs,
    taxaSensoresHz = sensorUpdateRateHz,
    acelerometro = SensorKind.ACCELEROMETER in enabledSensors,
    giroscopio = SensorKind.GYROSCOPE in enabledSensors,
    gps = SensorKind.GPS in enabledSensors,
    camera = SensorKind.CAMERA in enabledSensors,
    microfone = SensorKind.MICROPHONE in enabledSensors,
    temperaturaBateria = true,
    nivelBateria = true,
    barometro = SensorKind.BAROMETER in enabledSensors,
)

private fun ConfiguracaoEntity.toAppConfiguration() = AppConfiguration(
    phoneBrand = marcaSmartphone,
    phoneModel = modeloSmartphone,
    vehicleBrandModel = marcaVeiculo,
    vehicleMileageKm = quilometragemVeiculo.toInt(),
    gpsUpdateRateMs = taxaGpsMs,
    sensorUpdateRateHz = taxaSensoresHz,
    enabledSensors = buildSet {
        if (acelerometro) add(SensorKind.ACCELEROMETER)
        if (giroscopio) add(SensorKind.GYROSCOPE)
        if (gps) add(SensorKind.GPS)
        if (camera) add(SensorKind.CAMERA)
        if (microfone) add(SensorKind.MICROPHONE)
        if (barometro) add(SensorKind.BAROMETER)
    },
)
