package io.github.ranjandas.kimono.core.data.workspace

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Stores workspace state in Preferences DataStore: the selected workspace by name, and each workspace's pins as
 * one newline-separated string, since a string set wouldn't keep the pin order. Pins stored under a workspace's
 * old name (see [Workspace.ofStoredName]) are read as its own, after them, and moved over on the next write.
 */
internal class DataStoreWorkspaceStore(private val dataStore: DataStore<Preferences>) : WorkspaceStore {
    override val state: Flow<WorkspaceState> = dataStore.data.map { it.toWorkspaceState() }.distinctUntilChanged()

    override suspend fun select(workspace: Workspace) {
        dataStore.edit { it[CurrentKey] = workspace.name }
    }

    override suspend fun pin(workspace: Workspace, packageName: String) = update { it.pin(workspace, packageName) }

    override suspend fun unpin(workspace: Workspace, packageName: String) = update { it.unpin(workspace, packageName) }

    private suspend fun update(transform: (WorkspaceState) -> WorkspaceState) {
        dataStore.edit { prefs ->
            val updated = transform(prefs.toWorkspaceState())
            Workspace.entries.forEach { workspace ->
                Workspace.storedNames(workspace).drop(1).forEach { prefs.remove(pinsKey(it)) }
                prefs.setPins(workspace, updated.pinsOf(workspace))
            }
        }
    }

    private companion object {
        val CurrentKey = stringPreferencesKey("workspace.current")

        fun pinsKey(name: String) = stringPreferencesKey("workspace.$name.pins")

        fun Preferences.toWorkspaceState() = WorkspaceState(
            current = Workspace.ofStoredName(this[CurrentKey]) ?: Workspace.Default,
            pins = Workspace.entries.associateWith { pinsOf(it) }.filterValues { it.isNotEmpty() },
        )

        fun Preferences.pinsOf(workspace: Workspace): List<String> =
            Workspace.storedNames(workspace).flatMap { decodePins(this[pinsKey(it)]) }.distinct()

        fun MutablePreferences.setPins(workspace: Workspace, pins: List<String>) {
            val key = pinsKey(workspace.name)
            if (pins.isEmpty()) remove(key) else this[key] = encodePins(pins)
        }
    }
}

private const val PIN_SEPARATOR = "\n"

// Package names can't contain a newline, so it's a safe separator.
internal fun encodePins(pins: List<String>): String = pins.joinToString(PIN_SEPARATOR)

internal fun decodePins(encoded: String?): List<String> =
    encoded?.split(PIN_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
