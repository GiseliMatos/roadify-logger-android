package br.edu.utfpr.roadifylogger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracao")
data class ConfiguracaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val marcaSmartphone: String,
    val modeloSmartphone: String,
    val posicaoTelefone: String,
    val marcaVeiculo: String,
    val modeloVeiculo: String,
    val quilometragemVeiculo: Float,
    val taxaGpsMs: Int,
    val taxaSensoresHz: Int,
    val dataCriacao: Long = System.currentTimeMillis(),
    val acelerometro: Boolean,
    val giroscopio: Boolean,
    val gps: Boolean,
    val camera: Boolean,
    val microfone: Boolean,
    val temperaturaBateria: Boolean,
    val nivelBateria: Boolean,
    val barometro: Boolean
)
