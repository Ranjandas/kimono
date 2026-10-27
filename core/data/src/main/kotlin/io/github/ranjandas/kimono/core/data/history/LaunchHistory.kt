package io.github.ranjandas.kimono.core.data.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.ranjandas.kimono.core.data.kimonoPreferences
import io.github.ranjandas.kimono.core.data.workspace.decodePins
import io.github.ranjandas.kimono.core.data.workspace.encodePins
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The apps Kimono itself opened, most recent first. Recents come from the system's usage stats when Kimono may
 * read them, but Google TV only lets that be granted over adb, so this is what most people's recents are (#109).
 */
interface LaunchHistory {
    val recent: Flow<List<String>>

    suspend fun record(packageName: String)

    companion object {
        fun create(context: Context): LaunchHistory = DataStoreLaunchHistory(context.kimonoPreferences)
    }
}

internal class DataStoreLaunchHistory(private val dataStore: DataStore<Preferences>) : LaunchHistory {
    override val recent: Flow<List<String>> = dataStore.data.map { decodePins(it[Key]) }.distinctUntilChanged()

    override suspend fun record(packageName: String) {
        dataStore.edit { it[Key] = encodePins(withLaunch(decodePins(it[Key]), packageName)) }
    }

    private companion object {
        val Key = stringPreferencesKey("history.launches")
    }
}

/** [history] with [packageName] moved to the front, kept to [MAX_LAUNCHES]. */
internal fun withLaunch(history: List<String>, packageName: String): List<String> =
    (listOf(packageName) + (history - packageName)).take(MAX_LAUNCHES)

// Enough for the last used tile and the recents row, with room for apps that get uninstalled.
internal const val MAX_LAUNCHES = 12
