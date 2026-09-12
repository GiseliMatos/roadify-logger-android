package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import kotlinx.coroutines.flow.StateFlow

interface ConfiguracoesRepository {
    val configuracoes: StateFlow<ConfiguracaoEntity>
    fun salvarConfiguracoes(configuracoes: ConfiguracaoEntity)
}