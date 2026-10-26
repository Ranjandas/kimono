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
        assertEquals("next theme", okAction(HomeArea.ThemeButton))
        assertEquals("open settings", okAction(HomeArea.SettingsButton))
    }

    @Test
    fun `placeholder tiles offer no ok action`() {
        assertEquals(null, okAction(HomeArea.Tiles))
    }

    @Test
    fun `every area ends with the shortcuts that work anywhere on home`() {
        HomeArea.entries.forEach { area ->
            assertEquals(
                listOf(Hint("1…5", "workspace"), Hint("menu", "all apps")),
                homeHints(area).takeLast(2),
                "$area",
            )
        }
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
