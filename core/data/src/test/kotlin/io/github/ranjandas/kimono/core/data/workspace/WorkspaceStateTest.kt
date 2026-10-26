package io.github.ranjandas.kimono.core.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WorkspaceStateTest {
    @Test
    fun `pins keep the order they were added in`() {
        val state = WorkspaceState()
            .pin(Workspace.Media, "pkg.plex")
            .pin(Workspace.Media, "pkg.kodi")
        assertEquals(listOf("pkg.plex", "pkg.kodi"), state.pinsOf(Workspace.Media))
    }

    @Test
    fun `pinning an app twice changes nothing`() {
        val state = WorkspaceState().pin(Workspace.Media, "pkg.plex")
        assertEquals(state, state.pin(Workspace.Media, "pkg.plex"))
    }

    @Test
    fun `pins are per workspace`() {
        val state = WorkspaceState()
            .pin(Workspace.Media, "pkg.plex")
            .pin(Workspace.Games, "pkg.retro")
        assertEquals(listOf("pkg.plex"), state.pinsOf(Workspace.Media))
        assertEquals(listOf("pkg.retro"), state.pinsOf(Workspace.Games))
        assertEquals(emptyList<String>(), state.pinsOf(Workspace.Home))
    }

    @Test
    fun `unpinning leaves the other pins in place`() {
        val state = WorkspaceState()
            .pin(Workspace.Media, "pkg.plex")
            .pin(Workspace.Media, "pkg.kodi")
            .unpin(Workspace.Media, "pkg.plex")
        assertEquals(listOf("pkg.kodi"), state.pinsOf(Workspace.Media))
    }
}
