package br.edu.utfpr.roadifylogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import br.edu.utfpr.roadifylogger.data.repository.ConfiguracoesRepository
import br.edu.utfpr.roadifylogger.data.repository.ConfiguracoesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                _uiState.update { it.copy(taxaGpsMs = event.taxaGpsMs) }
            is ConfiguracoesEvent.TaxaSensoresChanged ->
                _uiState.update { it.copy(taxaSensoresHz = event.taxaSensoresHz) }
            is ConfiguracoesEvent.DataCriacao ->
                _uiState.update { it.copy(dataCriacao = event.dataCriacao) }
            is ConfiguracoesEvent.Acelerometro ->
                _uiState.update { it.copy(acelerometro = event.acelerometro) }
            is ConfiguracoesEvent.Giroscopio ->
                _uiState.update { it.copy(giroscopio = event.giroscopio) }
            is ConfiguracoesEvent.Gps ->
                _uiState.update { it.copy(gps = event.gps) }
            is ConfiguracoesEvent.Camera ->
                _uiState.update { it.copy(camera = event.camera) }
            is ConfiguracoesEvent.Microfone ->
                _uiState.update { it.copy(microfone = event.microfone) }
            is ConfiguracoesEvent.TemperaturaBateria ->
                _uiState.update { it.copy(temperaturaBateria = event.temperaturaBateria) }
            is ConfiguracoesEvent.NivelBateria ->
                _uiState.update { it.copy(nivelBateria = event.nivelBateria) }
            is ConfiguracoesEvent.Barometro ->
                _uiState.update { it.copy(barometro = event.barometro) }

            is ConfiguracoesEvent.Salvar -> {
                val state = _uiState.value
                val config = ConfiguracaoEntity(
                    marcaSmartphone = state.marcaSmartphone,
                    modeloSmartphone = state.modeloSmartphone,
                    posicaoTelefone = state.posicaoTelefone,
                    marcaVeiculo = state.marcaVeiculo,
                    modeloVeiculo = state.modeloVeiculo,
                    quilometragemVeiculo = state.quilometragemVeiculo.toFloatOrNull() ?: 0f,
                    taxaGpsMs = state.taxaGpsMs,
                    taxaSensoresHz = state.taxaSensoresHz,
                    dataCriacao = state.dataCriacao,
                    acelerometro = state.acelerometro,
                    giroscopio = state.giroscopio,
                    gps = state.gps,
                    camera = state.camera,
                    microfone = state.microfone,
                    temperaturaBateria = state.temperaturaBateria,
                    nivelBateria = state.nivelBateria,
                    barometro = state.barometro
                )

                viewModelScope.launch {
                    repository.salvarConfiguracoes(config)
                }
            }
        }
    }
}