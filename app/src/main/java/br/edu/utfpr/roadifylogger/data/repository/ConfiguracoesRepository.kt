package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.model.Configuracoes
import kotlinx.coroutines.flow.StateFlow

interface ConfiguracoesRepository {
    val configuracoes: StateFlow<Configuracoes>
    fun salvarConfiguracoes(configuracoes: Configuracoes)
}