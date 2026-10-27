package io.github.ranjandas.kimono.core.data.workspace

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import io.github.ranjandas.kimono.core.data.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataStoreWorkspaceStoreTest {
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
            store.select(Workspace.Play)
            assertEquals(Workspace.Play, awaitItem().current)
        }
    }

    @Test
    fun `remembers pins per workspace, in pin order`() = runTest {
        store.pin(Workspace.Watch, "pkg.plex")
        store.pin(Workspace.Watch, "pkg.kodi")
        store.pin(Workspace.Play, "pkg.retro")
        store.state.test {
            val state = awaitItem()
            assertEquals(listOf("pkg.plex", "pkg.kodi"), state.pinsOf(Workspace.Watch))
            assertEquals(listOf("pkg.retro"), state.pinsOf(Workspace.Play))
        }
    }

    @Test
    fun `unpins an app`() = runTest {
        store.pin(Workspace.Watch, "pkg.plex")
        store.pin(Workspace.Watch, "pkg.kodi")
        store.unpin(Workspace.Watch, "pkg.plex")
        store.state.test { assertEquals(listOf("pkg.kodi"), awaitItem().pinsOf(Workspace.Watch)) }
    }

    @Test
    fun `moves a pin`() = runTest {
        store.pin(Workspace.Watch, "pkg.plex")
        store.pin(Workspace.Watch, "pkg.kodi")
        store.move(Workspace.Watch, "pkg.kodi", -1)
        store.state.test { assertEquals(listOf("pkg.kodi", "pkg.plex"), awaitItem().pinsOf(Workspace.Watch)) }
    }

    @Test
    fun `a reread of the same file sees what was written`() = runTest {
        store.select(Workspace.AllApps)
        store.pin(Workspace.AllApps, "pkg.plex")
        DataStoreWorkspaceStore(dataStore).state.test {
            val state = awaitItem()
            assertEquals(Workspace.AllApps, state.current)
            assertEquals(listOf("pkg.plex"), state.pinsOf(Workspace.AllApps))
        }
    }

    @Test
    fun `moves pins and the selection stored under the old workspace names`() = runTest {
        dataStore.updateData {
            mutablePreferencesOf(
                stringPreferencesKey("workspace.current") to "Games",
                stringPreferencesKey("workspace.Home.pins") to "pkg.plex",
                stringPreferencesKey("workspace.Apps.pins") to "pkg.files\npkg.plex",
                stringPreferencesKey("workspace.Media.pins") to "pkg.kodi",
            )
        }
        store.state.test {
            val state = awaitItem()
            assertEquals(Workspace.Play, state.current)
            assertEquals(listOf("pkg.plex", "pkg.files"), state.pinsOf(Workspace.Home))
            assertEquals(listOf("pkg.kodi"), state.pinsOf(Workspace.Watch))
        }
        store.pin(Workspace.Watch, "pkg.jellyfin")
        val prefs = dataStore.data.first()
        assertEquals(null, prefs[stringPreferencesKey("workspace.Media.pins")])
        assertEquals(null, prefs[stringPreferencesKey("workspace.Apps.pins")])
        assertEquals("pkg.kodi\npkg.jellyfin", prefs[stringPreferencesKey("workspace.Watch.pins")])
        assertEquals("pkg.plex\npkg.files", prefs[stringPreferencesKey("workspace.Home.pins")])
    }

    @Test
    fun `falls back to the default when the stored workspace name no longer exists`() = runTest {
        dataStore.updateData { mutablePreferencesOf(stringPreferencesKey("workspace.current") to "Retired") }
        store.state.test { assertEquals(Workspace.Default, awaitItem().current) }
    }
}
