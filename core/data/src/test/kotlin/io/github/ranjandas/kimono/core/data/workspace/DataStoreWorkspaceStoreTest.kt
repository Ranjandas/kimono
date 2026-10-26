package io.github.ranjandas.kimono.core.data.workspace

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataStoreWorkspaceStoreTest {
    /** Stands in for a Preferences DataStore file, which needs a device. */
    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            val updated = transform(preferences.value)
            preferences.value = updated
            return updated
        }
    }

    private val dataStore = InMemoryDataStore()
    private val store = DataStoreWorkspaceStore(dataStore)

    @Test
    fun `starts on the default workspace with no pins`() = runTest {
        store.state.test { assertEquals(WorkspaceState(), awaitItem()) }
    }

    @Test
    fun `remembers the selected workspace`() = runTest {
        store.state.test {
            awaitItem()
            store.select(Workspace.Games)
            assertEquals(Workspace.Games, awaitItem().current)
        }
    }

    @Test
    fun `remembers pins per workspace, in pin order`() = runTest {
        store.pin(Workspace.Media, "pkg.plex")
        store.pin(Workspace.Media, "pkg.kodi")
        store.pin(Workspace.Games, "pkg.retro")
        store.state.test {
            val state = awaitItem()
            assertEquals(listOf("pkg.plex", "pkg.kodi"), state.pinsOf(Workspace.Media))
            assertEquals(listOf("pkg.retro"), state.pinsOf(Workspace.Games))
        }
    }

    @Test
    fun `unpins an app`() = runTest {
        store.pin(Workspace.Media, "pkg.plex")
        store.pin(Workspace.Media, "pkg.kodi")
        store.unpin(Workspace.Media, "pkg.plex")
        store.state.test { assertEquals(listOf("pkg.kodi"), awaitItem().pinsOf(Workspace.Media)) }
    }

    @Test
    fun `a reread of the same file sees what was written`() = runTest {
        store.select(Workspace.Apps)
        store.pin(Workspace.Apps, "pkg.plex")
        DataStoreWorkspaceStore(dataStore).state.test {
            val state = awaitItem()
            assertEquals(Workspace.Apps, state.current)
            assertEquals(listOf("pkg.plex"), state.pinsOf(Workspace.Apps))
        }
    }

    @Test
    fun `falls back to the default when the stored workspace name no longer exists`() = runTest {
        dataStore.updateData { mutablePreferencesOf(stringPreferencesKey("workspace.current") to "Retired") }
        store.state.test { assertEquals(Workspace.Default, awaitItem().current) }
    }
}
