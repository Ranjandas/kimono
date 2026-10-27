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
    fun `home and all apps list every app`() {
        assertEquals(apps, apps.forWorkspace(Workspace.Home, WorkspaceState()))
        assertEquals(apps, apps.forWorkspace(Workspace.AllApps, WorkspaceState()))
    }

    @Test
    fun `watch and play list their categories`() {
        assertEquals(listOf(plex, spotify), apps.forWorkspace(Workspace.Watch, WorkspaceState()))
        assertEquals(listOf(retro), apps.forWorkspace(Workspace.Play, WorkspaceState()))
    }

    @Test
    fun `pins come first, in pin order, and are not repeated below`() {
        val state = WorkspaceState()
            .pin(Workspace.Watch, spotify.packageName)
            .pin(Workspace.Watch, files.packageName)
        assertEquals(listOf(spotify, files, plex), apps.forWorkspace(Workspace.Watch, state))
    }

    @Test
    fun `ignores a pin whose app is no longer installed`() {
        val state = WorkspaceState().pin(Workspace.Play, "pkg.gone")
        assertEquals(listOf(retro), apps.forWorkspace(Workspace.Play, state))
    }
}
