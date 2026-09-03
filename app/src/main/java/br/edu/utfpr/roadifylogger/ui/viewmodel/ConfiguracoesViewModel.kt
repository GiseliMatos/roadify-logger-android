package br.edu.utfpr.roadifylogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import br.edu.utfpr.roadifylogger.data.model.Configuracoes
import br.edu.utfpr.roadifylogger.data.repository.ConfiguracoesRepository
import br.edu.utfpr.roadifylogger.data.repository.ConfiguracoesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfiguracoesViewModel(
    private val repository: ConfiguracoesRepository = ConfiguracoesRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracoesState())
    val uiState: StateFlow<ConfiguracoesState> = _uiState.asStateFlow()

    fun onEvent(event: ConfiguracoesEvent) {
        when (event) {
            is ConfiguracoesEvent.MarcaSmartphoneChanged ->
                _uiState.update { it.copy(marcaSmartphone = event.marca) }

            is ConfiguracoesEvent.ModeloSmartphoneChanged ->
                _uiState.update { it.copy(modeloSmartphone = event.modelo) }

            is ConfiguracoesEvent.PosicaoTelefoneChanged ->
                _uiState.update { it.copy(posicaoTelefone = event.posicao) }

            is ConfiguracoesEvent.MarcaVeiculoChanged ->
                _uiState.update { it.copy(marcaVeiculo = event.marca) }

            is ConfiguracoesEvent.ModeloVeiculoChanged ->
                _uiState.update { it.copy(modeloVeiculo = event.modelo) }

            is ConfiguracoesEvent.QuilometragemVeiculoChanged ->
                _uiState.update { it.copy(quilometragemVeiculo = event.quilometragem) }

            is ConfiguracoesEvent.TaxaGpsChanged ->
                _uiState.update { it.copy(taxaGpsMs = event.taxa) }

            is ConfiguracoesEvent.TaxaSensoresChanged ->
                _uiState.update { it.copy(taxaSensoresHz = event.taxa) }

            is ConfiguracoesEvent.Salvar -> {
                val state = _uiState.value
                val config = Configuracoes(
                    marcaSmartphone = state.marcaSmartphone,
                    modeloSmartphone = state.modeloSmartphone,
                    posicaoTelefone = state.posicaoTelefone,
                    marcaVeiculo = state.marcaVeiculo,
                    modeloVeiculo = state.modeloVeiculo,
                    quilometragemVeiculo = state.quilometragemVeiculo,
                    taxaGpsMs = state.taxaGpsMs,
                    taxaSensoresHz = state.taxaSensoresHz
                )
                repository.salvarConfiguracoes(config)
            }
        }
    }
}