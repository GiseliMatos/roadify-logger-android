package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.database.DatabaseInstance
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsRepository(database: DatabaseInstance.AppDatabase) {

    private val configuracaoDao = database.configuracaoDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val defaultConfig = ConfiguracaoEntity(
        id = 1L,
        marcaSmartphone = "",
        modeloSmartphone = "",
        posicaoTelefone = "Retrato",
        marcaVeiculo = "",
        modeloVeiculo = "",
        quilometragemVeiculo = 0f,
        taxaGpsMs = 1000,
        taxaSensoresHz = 50,
        acelerometro = true,
        giroscopio = true,
        gps = true,
        camera = false,
        microfone = false,
        temperaturaBateria = true,
        nivelBateria = true,
        barometro = false
    )

    private val _configuration = MutableStateFlow(defaultConfig)
    val configuration: StateFlow<ConfiguracaoEntity> = _configuration.asStateFlow()

    init {
        scope.launch {
            configuracaoDao.observeUltimaConfiguracao().collectLatest { entity ->
                _configuration.value = entity ?: defaultConfig
            }
        }
    }

    fun update(transform: (ConfiguracaoEntity) -> ConfiguracaoEntity) {
        val updated = transform(_configuration.value)
        _configuration.value = updated
        scope.launch { save(updated) }
    }

    suspend fun saveForRecording(configuracao: ConfiguracaoEntity): Long = save(configuracao)

    private suspend fun save(configuracao: ConfiguracaoEntity): Long {
        val existing = configuracaoDao.buscarUltimaConfiguracao()
        val targetId = existing?.id ?: 1L
        val entityToSave = configuracao.copy(id = targetId)

        return if (existing == null) {
            configuracaoDao.inserir(entityToSave)
        } else {
            configuracaoDao.atualizar(entityToSave)
            targetId
        }
    }
}