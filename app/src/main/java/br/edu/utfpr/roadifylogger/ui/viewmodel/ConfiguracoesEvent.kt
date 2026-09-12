package br.edu.utfpr.roadifylogger.ui.viewmodel

sealed class ConfiguracoesEvent {
    data class MarcaSmartphoneChanged(val marca: String) : ConfiguracoesEvent()
    data class ModeloSmartphoneChanged(val modelo: String) : ConfiguracoesEvent()
    data class PosicaoTelefoneChanged(val posicao: String) : ConfiguracoesEvent()
    data class MarcaVeiculoChanged(val marca: String) : ConfiguracoesEvent()
    data class ModeloVeiculoChanged(val modelo: String) : ConfiguracoesEvent()
    data class QuilometragemVeiculoChanged(val quilometragem: String) : ConfiguracoesEvent()
    data class TaxaGpsChanged(val taxaGpsMs: Int) : ConfiguracoesEvent()
    data class TaxaSensoresChanged(val taxaSensoresHz: Int) : ConfiguracoesEvent()
    data class DataCriacao(val dataCriacao: Long) : ConfiguracoesEvent()
    data class Acelerometro(val acelerometro: Boolean) : ConfiguracoesEvent()
    data class Giroscopio(val giroscopio: Boolean) : ConfiguracoesEvent()
    data class Gps(val gps: Boolean) : ConfiguracoesEvent()
    data class Camera(val camera: Boolean) : ConfiguracoesEvent()
    data class Microfone(val microfone: Boolean) : ConfiguracoesEvent()
    data class TemperaturaBateria(val temperaturaBateria: Boolean) : ConfiguracoesEvent()
    data class NivelBateria(val nivelBateria: Boolean) : ConfiguracoesEvent()
    data class Barometro(val barometro: Boolean) : ConfiguracoesEvent()
    data object Salvar : ConfiguracoesEvent()
}