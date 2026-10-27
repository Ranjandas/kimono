package io.github.ranjandas.kimono.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Stores each setting under its own key, by name, so adding a setting never invalidates the others. */
internal class DataStoreSettingsStore(private val dataStore: DataStore<Preferences>) : SettingsStore {
    override val settings: Flow<LauncherSettings> = dataStore.data.map { it.toSettings() }.distinctUntilChanged()

    override suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private companion object {
        val ThemeKey = stringPreferencesKey("settings.theme")
        val CornerStyleKey = stringPreferencesKey("settings.cornerStyle")
        val ClockFormatKey = stringPreferencesKey("settings.clockFormat")
        val TextSizeKey = stringPreferencesKey("settings.textSize")
        val IdleTimeoutKey = stringPreferencesKey("settings.idleTimeout")
        val PixelShiftKey = booleanPreferencesKey("settings.pixelShift")
        val WallpaperKey = booleanPreferencesKey("settings.wallpaper")
        val WallpaperUrlKey = stringPreferencesKey("settings.wallpaperUrl")

        fun Preferences.toSettings() = LauncherSettings(
            themeName = this[ThemeKey],
            cornerStyleName = this[CornerStyleKey],
            clockFormat = ClockFormat.entries.find { it.name == this[ClockFormatKey] } ?: ClockFormat.System,
            textSize = TextSize.entries.find { it.name == this[TextSizeKey] } ?: TextSize.Default,
            idleTimeout = IdleTimeout.entries.find { it.name == this[IdleTimeoutKey] } ?: IdleTimeout.Default,
            pixelShift = this[PixelShiftKey] ?: true,
            wallpaper = this[WallpaperKey] ?: true,
            wallpaperUrl = this[WallpaperUrlKey],
        )

        fun MutablePreferences.write(settings: LauncherSettings) {
            setOrRemove(ThemeKey, settings.themeName)
            setOrRemove(CornerStyleKey, settings.cornerStyleName)
            this[ClockFormatKey] = settings.clockFormat.name
            this[TextSizeKey] = settings.textSize.name
            this[IdleTimeoutKey] = settings.idleTimeout.name
            this[PixelShiftKey] = settings.pixelShift
            this[WallpaperKey] = settings.wallpaper
            setOrRemove(WallpaperUrlKey, settings.wallpaperUrl)
        }

        fun MutablePreferences.setOrRemove(key: Preferences.Key<String>, value: String?) {
            if (value == null) remove(key) else this[key] = value
        }
    }
}
