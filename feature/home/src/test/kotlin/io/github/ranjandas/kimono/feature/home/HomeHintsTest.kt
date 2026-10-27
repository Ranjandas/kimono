package io.github.ranjandas.kimono.feature.home

import io.github.ranjandas.kimono.core.design.component.Hint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeHintsTest {
    @Test
    fun `the apps list says what ok and held ok do`() {
        val hints = homeHints(HomeArea.Apps)
        assertTrue(Hint("ok", "launch") in hints)
        assertTrue(Hint("hold ok", "app options") in hints)
    }

    @Test
    fun `ok does what the focused top bar button does`() {
        assertEquals("switch workspace", okAction(HomeArea.Workspaces))
        assertEquals("open settings", okAction(HomeArea.SettingsButton))
    }

    @Test
    fun `ok opens the last used or a recent app, and held ok its options`() {
        listOf(HomeArea.LastUsed, HomeArea.Recents).forEach { area ->
            assertEquals("open", okAction(area), "$area")
            assertTrue(Hint("hold ok", "app options") in homeHints(area), "$area")
        }
    }

    @Test
    fun `every area ends with how to switch workspace, and names no key most remotes lack`() {
        HomeArea.entries.forEach { area ->
            val hints = homeHints(area)
            assertEquals(Hint("edge ◀ ▶", "workspace"), hints.last(), "$area")
            assertTrue(hints.none { it.key == "menu" || it.key.startsWith("1") }, "$area")
        }
    }

    @Test
    fun `the grid pages say how to go home`() {
        assertTrue(Hint("back", "home") in GridHints)
        assertEquals(EdgeHint, GridHints.last())
    }

    @Test
    fun `no area repeats a key`() {
        HomeArea.entries.forEach { area ->
            val keys = homeHints(area).map { it.key }
            assertEquals(keys.distinct(), keys, "$area")
        }
    }

    private fun okAction(area: HomeArea) = homeHints(area).find { it.key == "ok" }?.action
}
