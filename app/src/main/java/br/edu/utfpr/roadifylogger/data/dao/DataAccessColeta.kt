package br.edu.utfpr.roadifylogger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.edu.utfpr.roadifylogger.data.model.ColetaEntity

@Dao
interface DataAccessColeta {

    @Insert
    suspend fun inserir(coleta: ColetaEntity): Long

    @Update
    suspend fun atualizar(coleta: ColetaEntity)

    @Query("SELECT * FROM coleta WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): ColetaEntity?

    @Query("SELECT * FROM coleta ORDER BY dataHoraInicio DESC")
    suspend fun listarTodas(): List<ColetaEntity>

    @Query("""
        UPDATE coleta
        SET dataHoraFim = :dataHoraFim,
            latitudeFim = :latitudeFim,
            longitudeFim = :longitudeFim
        WHERE id = :coletaId
    """)
    suspend fun finalizarColeta(
        coletaId: Long,
        dataHoraFim: Long,
        latitudeFim: Double,
        longitudeFim: Double
    )
}
