package io.github.ranjandas.kimono.core.data.settings

import android.content.Context
import io.github.ranjandas.kimono.core.data.kimonoPreferences
import kotlinx.coroutines.flow.Flow

/** Keeps [LauncherSettings] across restarts. */
interface SettingsStore {
    val settings: Flow<LauncherSettings>

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings)

    companion object {
        fun create(context: Context): SettingsStore = DataStoreSettingsStore(context.kimonoPreferences)
    }
}
