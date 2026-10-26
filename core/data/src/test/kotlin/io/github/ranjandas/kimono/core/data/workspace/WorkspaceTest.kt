package io.github.ranjandas.kimono.core.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WorkspaceTest {
    @Test
    fun `numbers the five workspaces from one, in mockup order`() {
        assertEquals(listOf("home", "media", "games", "apps", "system"), Workspace.entries.map { it.label })
        assertEquals(listOf(1, 2, 3, 4, 5), Workspace.entries.map { it.number })
        assertEquals(Workspace.Home, Workspace.Default)
    }

    @Test
    fun `finds a workspace by its number`() {
        assertEquals(Workspace.Media, Workspace.ofNumber(2))
        assertNull(Workspace.ofNumber(0))
        assertNull(Workspace.ofNumber(6))
    }

    @Test
    fun `cycles in both directions`() {
        assertEquals(Workspace.Media, Workspace.Home.next())
        assertEquals(Workspace.Home, Workspace.System.next())
        assertEquals(Workspace.System, Workspace.Home.previous())
        assertEquals(Workspace.Apps, Workspace.System.previous())
    }
}
