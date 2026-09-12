package br.edu.utfpr.roadifylogger.ui.viewmodel

data class ConfiguracoesState(
    val marcaSmartphone: String = "",
    val modeloSmartphone: String = "",
    val posicaoTelefone: String = "",
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val quilometragemVeiculo: String = "",
    val taxaGpsMs: Int = 0,
    val taxaSensoresHz: Int = 0,
    val dataCriacao: Long = System.currentTimeMillis(),
    val acelerometro: Boolean = false,
    val giroscopio: Boolean = false,
    val gps: Boolean = false,
    val camera: Boolean = false,
    val microfone: Boolean = false,
    val temperaturaBateria: Boolean = false,
    val nivelBateria: Boolean = false,
    val barometro: Boolean = false
)