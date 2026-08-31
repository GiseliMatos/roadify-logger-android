package br.edu.utfpr.roadifylogger.ui.viewmodel

import br.edu.utfpr.roadifylogger.data.model.Posicao

sealed class ConfiguracoesEvent {
    data class MarcaSmartphoneChanged(val marca: String) : ConfiguracoesEvent()
    data class ModeloSmartphoneChanged(val modelo: String) : ConfiguracoesEvent()
    data class PosicaoTelefoneChanged(val posicao: Posicao) : ConfiguracoesEvent()
    data class MarcaVeiculoChanged(val marca: String) : ConfiguracoesEvent()
    data class ModeloVeiculoChanged(val modelo: String) : ConfiguracoesEvent()
    data class QuilometragemVeiculoChanged(val quilometragem: String) : ConfiguracoesEvent()
    data class TaxaGpsChanged(val taxa: String) : ConfiguracoesEvent()
    data class TaxaSensoresChanged(val taxa: String) : ConfiguracoesEvent()
    data object Salvar : ConfiguracoesEvent()
}