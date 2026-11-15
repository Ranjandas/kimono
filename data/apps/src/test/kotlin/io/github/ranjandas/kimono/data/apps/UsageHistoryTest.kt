package io.github.ranjandas.kimono.data.apps

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UsageHistoryTest {
    @Test
    fun `lists each app once, by its latest use`() {
        val uses = listOf(
            AppUse("plex", 100),
            AppUse("youtube", 200),
            AppUse("plex", 300),
            AppUse("kodi", 50),
        )
        assertEquals(listOf("plex", "youtube", "kodi"), mostRecentFirst(uses))
    }

    @Test
    fun `no uses means no recents`() {
        assertEquals(emptyList<String>(), mostRecentFirst(emptyList()))
    }

    @Test
    fun `screen time is longest first and drops apps never on screen`() {
        val times = listOf(
            ScreenTime("kodi", Duration.ofMinutes(5)),
            ScreenTime("tuner", Duration.ZERO),
            ScreenTime("youtube", Duration.ofMinutes(72)),
        )
        assertEquals(
            listOf(ScreenTime("youtube", Duration.ofMinutes(72)), ScreenTime("kodi", Duration.ofMinutes(5))),
            longestFirst(times),
        )
    }

    @Test
    fun `recents come from events when there are some`() {
        val events = listOf(AppUse("youtube", 200))
        val lastUsed = listOf(AppUse("plex", 300))
        assertEquals(listOf("youtube"), recentFrom(events, lastUsed, now = 1_000))
    }

    @Test
    fun `recents fall back to last used times when events are empty`() {
        val lastUsed = listOf(AppUse("plex", 300), AppUse("youtube", 500), AppUse("never", 0))
        assertEquals(listOf("youtube", "plex"), recentFrom(emptyList(), lastUsed, now = 1_000))
    }

    @Test
    fun `future-dated uses are dropped`() {
        val events = listOf(AppUse("future", 5_000))
        val lastUsed = listOf(AppUse("future", 5_000), AppUse("plex", 300))
        assertEquals(listOf("plex"), recentFrom(events, lastUsed, now = 1_000))
    }
}
