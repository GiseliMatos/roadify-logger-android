package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.local.ConfiguracoesLocalDataSource
import br.edu.utfpr.roadifylogger.data.model.Configuracoes
import kotlinx.coroutines.flow.StateFlow

class ConfiguracoesRepositoryImpl(
    private val localDataSource: ConfiguracoesLocalDataSource = ConfiguracoesLocalDataSource()
) : ConfiguracoesRepository {

    override val configuracoes: StateFlow<Configuracoes> = localDataSource.configuracoes

    override fun salvarConfiguracoes(configuracoes: Configuracoes) {
        localDataSource.salvar(configuracoes)
    }
}