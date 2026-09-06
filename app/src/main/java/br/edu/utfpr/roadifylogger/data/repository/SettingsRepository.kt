package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.model.AppConfiguration
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
class SettingsRepository {

    private val _configuration = MutableStateFlow(AppConfiguration())
    val configuration: StateFlow<AppConfiguration> = _configuration.asStateFlow()

    fun update(transform: (AppConfiguration) -> AppConfiguration) {
        _configuration.value = transform(_configuration.value)
    }

    fun setMountCalibration(rollDeg: Float, pitchDeg: Float) {
        update { it.copy(mountCalibrationRollDeg = rollDeg, mountCalibrationPitchDeg = pitchDeg) }
    }
}
