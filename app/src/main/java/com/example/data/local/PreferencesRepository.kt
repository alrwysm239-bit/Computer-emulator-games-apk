package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ShaderFilter
import com.example.data.model.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "retro_pc_prefs")

data class GlobalSettingsState(
    val graphicsFiltersEnabled: Boolean = true,
    val audioMidiEnabled: Boolean = true,
    val controlsMappingEnabled: Boolean = true,
    val performanceBoostEnabled: Boolean = false,
    val cloudBackupEnabled: Boolean = false,
    val language: AppLanguage = if (Locale.getDefault().language == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH,
    val themePreset: ThemePreset = ThemePreset.NEON_DARK,
    val defaultShader: ShaderFilter = ShaderFilter.CRT_SCANLINES,
    val selectedSoundFont: String = "FluidR3_GM.sf2",
    val lowBatteryMode: Boolean = false,
    val wineVersion: String = "Wine 9.0 + Box64 0.3.2",
    val dosboxVersion: String = "DOSBox-X 2024.12",
    val scummvmVersion: String = "ScummVM 2.9.0",
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val GRAPHICS_FILTERS = booleanPreferencesKey("graphics_filters_enabled")
        val AUDIO_MIDI = booleanPreferencesKey("audio_midi_enabled")
        val CONTROLS_MAPPING = booleanPreferencesKey("controls_mapping_enabled")
        val PERFORMANCE_BOOST = booleanPreferencesKey("performance_boost_enabled")
        val CLOUD_BACKUP = booleanPreferencesKey("cloud_backup_enabled")
        val LANGUAGE = stringPreferencesKey("app_language")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val DEFAULT_SHADER = stringPreferencesKey("default_shader")
        val SOUNDFONT = stringPreferencesKey("soundfont_name")
        val LOW_BATTERY = booleanPreferencesKey("low_battery_mode")
        val WINE_VERSION = stringPreferencesKey("wine_version")
    }

    val settingsFlow: Flow<GlobalSettingsState> = context.dataStore.data.map { prefs ->
        val defaultLang = if (Locale.getDefault().language == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
        GlobalSettingsState(
            graphicsFiltersEnabled = prefs[Keys.GRAPHICS_FILTERS] ?: true,
            audioMidiEnabled = prefs[Keys.AUDIO_MIDI] ?: true,
            controlsMappingEnabled = prefs[Keys.CONTROLS_MAPPING] ?: true,
            performanceBoostEnabled = prefs[Keys.PERFORMANCE_BOOST] ?: false,
            cloudBackupEnabled = prefs[Keys.CLOUD_BACKUP] ?: false,
            language = prefs[Keys.LANGUAGE]?.let { code ->
                AppLanguage.entries.firstOrNull { it.code == code }
            } ?: defaultLang,
            themePreset = prefs[Keys.THEME_PRESET]?.let { name ->
                ThemePreset.entries.firstOrNull { it.name == name }
            } ?: ThemePreset.NEON_DARK,
            defaultShader = prefs[Keys.DEFAULT_SHADER]?.let { name ->
                ShaderFilter.entries.firstOrNull { it.name == name }
            } ?: ShaderFilter.CRT_SCANLINES,
            selectedSoundFont = prefs[Keys.SOUNDFONT] ?: "FluidR3_GM.sf2",
            lowBatteryMode = prefs[Keys.LOW_BATTERY] ?: false,
            wineVersion = prefs[Keys.WINE_VERSION] ?: "Wine 9.0 + Box64 0.3.2",
        )
    }

    suspend fun setGraphicsFilters(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GRAPHICS_FILTERS] = enabled }
    }

    suspend fun setAudioMidi(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUDIO_MIDI] = enabled }
    }

    suspend fun setControlsMapping(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CONTROLS_MAPPING] = enabled }
    }

    suspend fun setPerformanceBoost(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PERFORMANCE_BOOST] = enabled }
    }

    suspend fun setCloudBackup(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CLOUD_BACKUP] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemePreset(preset: ThemePreset) {
        context.dataStore.edit { it[Keys.THEME_PRESET] = preset.name }
    }

    suspend fun setDefaultShader(shader: ShaderFilter) {
        context.dataStore.edit { it[Keys.DEFAULT_SHADER] = shader.name }
    }

    suspend fun setSoundFont(name: String) {
        context.dataStore.edit { it[Keys.SOUNDFONT] = name }
    }

    suspend fun setLowBatteryMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOW_BATTERY] = enabled }
    }

    suspend fun setWineVersion(version: String) {
        context.dataStore.edit { it[Keys.WINE_VERSION] = version }
    }
}
