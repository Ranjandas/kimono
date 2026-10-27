package io.github.ranjandas.kimono.core.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WorkspaceTest {
    @Test
    fun `numbers the four workspaces from one, all apps last`() {
        assertEquals(listOf("home", "watch", "play", "all apps"), Workspace.entries.map { it.label })
        assertEquals(listOf(1, 2, 3, 4), Workspace.entries.map { it.number })
        assertEquals(Workspace.Home, Workspace.Default)
    }

    @Test
    fun `finds a workspace by its number`() {
        assertEquals(Workspace.Watch, Workspace.ofNumber(2))
        assertNull(Workspace.ofNumber(0))
        assertNull(Workspace.ofNumber(5))
    }

    @Test
    fun `cycles in both directions`() {
        assertEquals(Workspace.Watch, Workspace.Home.next())
        assertEquals(Workspace.Home, Workspace.AllApps.next())
        assertEquals(Workspace.AllApps, Workspace.Home.previous())
        assertEquals(Workspace.Play, Workspace.AllApps.previous())
    }

    @Test
    fun `reads the names stored before there were four workspaces`() {
        assertEquals(Workspace.Watch, Workspace.ofStoredName("Media"))
        assertEquals(Workspace.Play, Workspace.ofStoredName("Games"))
        assertEquals(Workspace.Home, Workspace.ofStoredName("Apps"))
        assertEquals(Workspace.Home, Workspace.ofStoredName("System"))
        assertEquals(Workspace.AllApps, Workspace.ofStoredName("AllApps"))
        assertNull(Workspace.ofStoredName("Retired"))
        assertNull(Workspace.ofStoredName(null))
    }

    @Test
    fun `pins made in all apps go to home`() {
        assertEquals(Workspace.Home, Workspace.AllApps.pinTarget)
        assertEquals(Workspace.Play, Workspace.Play.pinTarget)
    }
}
