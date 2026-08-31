package br.edu.utfpr.roadifylogger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.edu.utfpr.roadifylogger.data.dao.DataAccessColeta
import br.edu.utfpr.roadifylogger.data.dao.DataAccessConfiguracoes
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity

class DatabaseInstance {
    @Database(
        entities = [ConfiguracaoEntity::class, ConfiguracaoEntity::class],
        version = 1,
        exportSchema = false
    )
    abstract class AppDatabase : RoomDatabase() {

        abstract fun configuracaoDao(): DataAccessConfiguracoes
        abstract fun coletaDao(): DataAccessColeta

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getInstance(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "medicao_sensores_db"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

}