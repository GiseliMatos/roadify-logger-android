package br.edu.utfpr.roadifylogger.data.repository

import br.edu.utfpr.roadifylogger.data.local.ConfiguracoesLocalDataSource
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import kotlinx.coroutines.flow.StateFlow

class ConfiguracoesRepositoryImpl(
    private val localDataSource: ConfiguracoesLocalDataSource = ConfiguracoesLocalDataSource()
) : ConfiguracoesRepository {

    override val configuracoes: StateFlow<ConfiguracaoEntity> = localDataSource.configuracoes

    override fun salvarConfiguracoes(configuracoes: ConfiguracaoEntity) {
        localDataSource.salvar(configuracoes)
    }
}