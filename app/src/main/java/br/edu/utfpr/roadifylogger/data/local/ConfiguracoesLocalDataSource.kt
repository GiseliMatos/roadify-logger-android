package br.edu.utfpr.roadifylogger.data.local

import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfiguracoesLocalDataSource {
    
    private val _configuracoes = MutableStateFlow(
        ConfiguracaoEntity(
            marcaSmartphone = "",
            modeloSmartphone = "",
            posicaoTelefone = "",
            marcaVeiculo = "",
            modeloVeiculo = "",
            quilometragemVeiculo = 0f,
            taxaGpsMs = 0,
            taxaSensoresHz = 0,
            acelerometro = false,
            giroscopio = false,
            gps = false,
            camera = false,
            microfone = false,
            temperaturaBateria = false,
            nivelBateria = false,
            barometro = false
        )
    )
    val configuracoes: StateFlow<ConfiguracaoEntity> = _configuracoes.asStateFlow()

    fun salvar(novasConfiguracoes: ConfiguracaoEntity) {
        _configuracoes.value = novasConfiguracoes
    }
}