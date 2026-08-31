package br.edu.utfpr.roadifylogger.data.local

import br.edu.utfpr.roadifylogger.data.model.Configuracoes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfiguracoesLocalDataSource {
    private val _configuracoes = MutableStateFlow(Configuracoes())
    val configuracoes: StateFlow<Configuracoes> = _configuracoes.asStateFlow()

    fun salvar(novasConfiguracoes: Configuracoes) {
        _configuracoes.value = novasConfiguracoes
    }
}