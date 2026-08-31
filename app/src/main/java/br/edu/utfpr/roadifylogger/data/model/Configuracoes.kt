package br.edu.utfpr.roadifylogger.data.model

data class Configuracoes(
    val marcaTelefone: String = "",
    val modeloTelefone: String = "",
    val posicaoTelefone: Posicao = Posicao.RETRATO,
    val carro: String = "",
    val quilometragem: String = "",
    val taxaGpsMs: String = "",
    val taxaSensoresHz: String = ""
)