package br.edu.utfpr.roadifylogger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "coleta",
    foreignKeys = [
        ForeignKey(
            entity = ConfiguracaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["configuracaoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["configuracaoId"])]
)
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val configuracaoId: Long,
    val dataHoraInicio: Long,
    var dataHoraFim: Long? = null,
    val latitudeInicio: Double,
    var latitudeFim: Double? = null,
    val longitudeInicio: Double,
    var longitudeFim: Double? = null,
    val nomeArquivoColeta: String,
    val caminhoPastaGravacao: String
)