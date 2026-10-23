package br.edu.utfpr.roadifylogger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.edu.utfpr.roadifylogger.data.dao.DataAccessColeta
import br.edu.utfpr.roadifylogger.data.dao.DataAccessConfiguracoes
import br.edu.utfpr.roadifylogger.data.model.ColetaEntity
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity

class DatabaseInstance {
    @Database(
        entities = [ConfiguracaoEntity::class, ColetaEntity::class],
        version = 2,
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
                    ).addMigrations(MIGRATION_1_2).build()
                    INSTANCE = instance
                    instance
                }
            }

            private val MIGRATION_1_2 = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE configuracao ADD COLUMN magnetometro INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }
        }
    }

}
