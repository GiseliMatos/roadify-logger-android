package br.edu.utfpr.roadifylogger.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.edu.utfpr.roadifylogger.data.model.BubbleViscosity
import br.edu.utfpr.roadifylogger.data.model.LevelDisplayType
import br.edu.utfpr.roadifylogger.data.model.LevelOptionsState
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Salva e disponibiliza as configurações da Tela de Nível Bolha.
private val Context.levelOptionsDataStore by preferencesDataStore(
    name = "level_options"
)

class LevelPreferencesRepository(
    private val context: Context
) {
    private object Keys {
        val showAngle: Preferences.Key<Boolean> =
            booleanPreferencesKey("show_angle")

        val displayType: Preferences.Key<String> =
            stringPreferencesKey("display_type")

        val lockLevelType: Preferences.Key<Boolean> =
            booleanPreferencesKey("lock_level_type")

        val economyMode: Preferences.Key<Boolean> =
            booleanPreferencesKey("economy_mode")

        val viscosity: Preferences.Key<String> =
            stringPreferencesKey("viscosity")

        val soundEnabled: Preferences.Key<Boolean> =
            booleanPreferencesKey("sound_enabled")
    }

    val options: Flow<LevelOptionsState> =
        context.levelOptionsDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                LevelOptionsState(
                    showAngle = preferences[Keys.showAngle] ?: true,

                    displayType = preferences[Keys.displayType]
                        ?.let { savedValue ->
                            runCatching {
                                LevelDisplayType.valueOf(savedValue)
                            }.getOrNull()
                        }
                        ?: LevelDisplayType.ANGLE,

                    lockLevelType =
                        preferences[Keys.lockLevelType] ?: false,

                    economyMode =
                        preferences[Keys.economyMode] ?: false,

                    viscosity = preferences[Keys.viscosity]
                        ?.let { savedValue ->
                            runCatching {
                                BubbleViscosity.valueOf(savedValue)
                            }.getOrNull()
                        }
                        ?: BubbleViscosity.MEDIUM,

                    soundEnabled =
                        preferences[Keys.soundEnabled] ?: false
                )
            }

    suspend fun setShowAngle(show: Boolean) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.showAngle] = show
        }
    }

    suspend fun setDisplayType(type: LevelDisplayType) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.displayType] = type.name
        }
    }

    suspend fun setLockLevelType(locked: Boolean) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.lockLevelType] = locked
        }
    }

    suspend fun setEconomyMode(enabled: Boolean) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.economyMode] = enabled
        }
    }

    suspend fun setViscosity(viscosity: BubbleViscosity) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.viscosity] = viscosity.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.levelOptionsDataStore.edit { preferences ->
            preferences[Keys.soundEnabled] = enabled
        }
    }
}