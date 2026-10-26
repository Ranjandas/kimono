package io.github.ranjandas.kimono.data.apps

import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WorkspaceAppsTest {
    private fun app(label: String, category: AppCategory) =
        InstalledApp("pkg.$label", "pkg.$label.Main", label, category, isTvApp = true)

    private val plex = app("plex", AppCategory.Video)
    private val spotify = app("spotify", AppCategory.Audio)
    private val retro = app("retro", AppCategory.Game)
    private val files = app("files", AppCategory.Other)
    private val apps = listOf(plex, spotify, retro, files)

    @Test
    fun `home and apps list every app`() {
        assertEquals(apps, apps.forWorkspace(Workspace.Home, WorkspaceState()))
        assertEquals(apps, apps.forWorkspace(Workspace.Apps, WorkspaceState()))
    }

    @Test
    fun `media and games list their categories`() {
        assertEquals(listOf(plex, spotify), apps.forWorkspace(Workspace.Media, WorkspaceState()))
        assertEquals(listOf(retro), apps.forWorkspace(Workspace.Games, WorkspaceState()))
    }

    @Test
    fun `system lists only what is pinned to it`() {
        val state = WorkspaceState().pin(Workspace.System, files.packageName)
        assertEquals(emptyList<InstalledApp>(), apps.forWorkspace(Workspace.System, WorkspaceState()))
        assertEquals(listOf(files), apps.forWorkspace(Workspace.System, state))
    }

    @Test
    fun `pins come first, in pin order, and are not repeated below`() {
        val state = WorkspaceState()
            .pin(Workspace.Media, spotify.packageName)
            .pin(Workspace.Media, files.packageName)
        assertEquals(listOf(spotify, files, plex), apps.forWorkspace(Workspace.Media, state))
    }

    @Test
    fun `ignores a pin whose app is no longer installed`() {
        val state = WorkspaceState().pin(Workspace.Games, "pkg.gone")
        assertEquals(listOf(retro), apps.forWorkspace(Workspace.Games, state))
    }
}
