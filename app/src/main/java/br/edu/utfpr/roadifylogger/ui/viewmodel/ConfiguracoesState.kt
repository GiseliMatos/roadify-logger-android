package br.edu.utfpr.roadifylogger.ui.viewmodel

import br.edu.utfpr.roadifylogger.data.model.Posicao

data class ConfiguracoesState(
    val marcaSmartphone: String = "",
    val modeloSmartphone: String = "",
    val posicaoTelefone: Posicao = Posicao.RETRATO,
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val quilometragemVeiculo: String = "",
    val taxaGpsMs: String = "",
    val taxaSensoresHz: String = ""
)