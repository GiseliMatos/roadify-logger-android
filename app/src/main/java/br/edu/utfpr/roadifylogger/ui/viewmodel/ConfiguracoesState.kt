package br.edu.utfpr.roadifylogger.ui.viewmodel

import br.edu.utfpr.roadifylogger.data.model.Posicao

data class ConfiguracoesState(
    val marcaTelefone: String= "",
    val modeloTelefone: String= "",
    val posicaoTelefone: Posicao = Posicao.RETRATO,
    val carro: String= "",
    val quilometragem: String= "",
    val taxaGpsMs: String= "",
    val taxaSensoresHz: String= ""
)