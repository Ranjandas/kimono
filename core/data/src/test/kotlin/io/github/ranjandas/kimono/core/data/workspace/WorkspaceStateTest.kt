package io.github.ranjandas.kimono.core.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WorkspaceStateTest {
    @Test
    fun `pins keep the order they were added in`() {
        val state = WorkspaceState()
            .pin(Workspace.Watch, "pkg.plex")
            .pin(Workspace.Watch, "pkg.kodi")
        assertEquals(listOf("pkg.plex", "pkg.kodi"), state.pinsOf(Workspace.Watch))
    }

    @Test
    fun `pinning an app twice changes nothing`() {
        val state = WorkspaceState().pin(Workspace.Watch, "pkg.plex")
        assertEquals(state, state.pin(Workspace.Watch, "pkg.plex"))
    }

    @Test
    fun `pins are per workspace`() {
        val state = WorkspaceState()
            .pin(Workspace.Watch, "pkg.plex")
            .pin(Workspace.Play, "pkg.retro")
        assertEquals(listOf("pkg.plex"), state.pinsOf(Workspace.Watch))
        assertEquals(listOf("pkg.retro"), state.pinsOf(Workspace.Play))
        assertEquals(emptyList<String>(), state.pinsOf(Workspace.Home))
    }

    @Test
    fun `unpinning leaves the other pins in place`() {
        val state = WorkspaceState()
            .pin(Workspace.Watch, "pkg.plex")
            .pin(Workspace.Watch, "pkg.kodi")
            .unpin(Workspace.Watch, "pkg.plex")
        assertEquals(listOf("pkg.kodi"), state.pinsOf(Workspace.Watch))
    }

    @Test
    fun `moving a pin reorders it, stopping at either end`() {
        val state = WorkspaceState()
            .pin(Workspace.Watch, "pkg.plex")
            .pin(Workspace.Watch, "pkg.kodi")
            .pin(Workspace.Watch, "pkg.jellyfin")
        assertEquals(
            listOf("pkg.plex", "pkg.jellyfin", "pkg.kodi"),
            state.move(Workspace.Watch, "pkg.jellyfin", -1).pinsOf(Workspace.Watch),
        )
        assertEquals(
            listOf("pkg.jellyfin", "pkg.plex", "pkg.kodi"),
            state.move(Workspace.Watch, "pkg.jellyfin", -5).pinsOf(Workspace.Watch),
        )
        assertEquals(state, state.move(Workspace.Watch, "pkg.jellyfin", 1))
    }

    @Test
    fun `moving an app that isn't pinned changes nothing`() {
        val state = WorkspaceState().pin(Workspace.Watch, "pkg.plex")
        assertEquals(state, state.move(Workspace.Watch, "pkg.kodi", 1))
        assertEquals(state, state.move(Workspace.Play, "pkg.plex", 1))
    }

    @Test
    fun `pin choices cover every workspace but all apps`() {
        val state = WorkspaceState().pin(Workspace.Watch, "pkg.plex")
        assertEquals(
            listOf(Workspace.Home to false, Workspace.Watch to true, Workspace.Play to false),
            state.pinChoices("pkg.plex"),
        )
    }
}
