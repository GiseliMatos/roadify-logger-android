package br.edu.utfpr.roadifylogger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity

@Dao
interface DataAccessConfiguracoes {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(configuracao: ConfiguracaoEntity): Long

    @Update
    suspend fun atualizar(configuracao: ConfiguracaoEntity)

    @Query("SELECT * FROM configuracao WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): ConfiguracaoEntity?

    @Query("SELECT * FROM configuracao ORDER BY id DESC LIMIT 1")
    suspend fun buscarUltimaConfiguracao(): ConfiguracaoEntity?

    @Query("SELECT * FROM configuracao ORDER BY dataCriacao DESC")
    suspend fun listarTodas(): List<ConfiguracaoEntity>
}