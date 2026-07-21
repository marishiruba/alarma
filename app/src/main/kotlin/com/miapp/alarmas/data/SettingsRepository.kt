package com.miapp.alarmas.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourFormat: Boolean = false,
    val defaultVibrate: Boolean = true,
    val defaultSnoozeMinutes: Int = 10,
    val defaultSoundUri: String? = null,
    val defaultSoundName: String = "Sonido predeterminado",
    val alarmVolume: Float = 0.8f
)

/** Guarda las preferencias globales de la app (sonido por defecto, tema, formato de hora, etc). */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val FORMAT_24H = booleanPreferencesKey("format_24h")
        val VIBRATE = booleanPreferencesKey("default_vibrate")
        val SNOOZE_MIN = intPreferencesKey("default_snooze_minutes")
        val SOUND_URI = stringPreferencesKey("default_sound_uri")
        val SOUND_NAME = stringPreferencesKey("default_sound_name")
        val VOLUME = floatPreferencesKey("alarm_volume")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.entries.find { it.name == prefs[Keys.THEME] } ?: ThemeMode.SYSTEM,
            use24HourFormat = prefs[Keys.FORMAT_24H] ?: false,
            defaultVibrate = prefs[Keys.VIBRATE] ?: true,
            defaultSnoozeMinutes = prefs[Keys.SNOOZE_MIN] ?: 10,
            defaultSoundUri = prefs[Keys.SOUND_URI],
            defaultSoundName = prefs[Keys.SOUND_NAME] ?: "Sonido predeterminado",
            alarmVolume = prefs[Keys.VOLUME] ?: 0.8f
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setUse24HourFormat(value: Boolean) {
        context.dataStore.edit { it[Keys.FORMAT_24H] = value }
    }

    suspend fun setDefaultVibrate(value: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATE] = value }
    }

    suspend fun setDefaultSnoozeMinutes(value: Int) {
        context.dataStore.edit { it[Keys.SNOOZE_MIN] = value }
    }

    suspend fun setDefaultSound(uri: String, name: String) {
        context.dataStore.edit {
            it[Keys.SOUND_URI] = uri
            it[Keys.SOUND_NAME] = name
        }
    }

    suspend fun setAlarmVolume(value: Float) {
        context.dataStore.edit { it[Keys.VOLUME] = value }
    }
}
