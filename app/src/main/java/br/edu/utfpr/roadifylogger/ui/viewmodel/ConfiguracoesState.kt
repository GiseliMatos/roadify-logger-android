package br.edu.utfpr.roadifylogger.ui.viewmodel

data class ConfiguracoesState(
    val id: Long = 0L,
    val marcaSmartphone: String = "",
    val modeloSmartphone: String = "",
    val posicaoTelefone: String = "",
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val quilometragemVeiculo: String = "",
    val taxaGpsMs: Int = 1000,
    val taxaSensoresHz: Int = 50,
    val dataCriacao: Long = System.currentTimeMillis(),
    val acelerometro: Boolean = false,
    val giroscopio: Boolean = false,
    val gps: Boolean = false,
    val camera: Boolean = false,
    val microfone: Boolean = false,
    val temperaturaBateria: Boolean = false,
    val nivelBateria: Boolean = false,
    val barometro: Boolean = false,
    val isLoading: Boolean = false,
    val mensagemFeedback: String? = null
)