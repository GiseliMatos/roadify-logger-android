package br.edu.utfpr.roadifylogger.data.model

import java.io.File

data class ColetaSummary(
    val arquivo: File,
    val nomeArquivoColeta: String = arquivo.name,
    val caminhoPastaGravacao: String = arquivo.parent ?: "",
    val dataHoraInicio: Long? = null,
    val dataHoraFim: Long? = null,
    val latitudeInicio: Double? = null,
    val longitudeInicio: Double? = null,
    val latitudeFim: Double? = null,
    val longitudeFim: Double? = null,
    val duracaoSegundos: Double = 0.0,
    val temDadosGps: Boolean = false,
    val distanciaTotalMetros: Double = 0.0
)
