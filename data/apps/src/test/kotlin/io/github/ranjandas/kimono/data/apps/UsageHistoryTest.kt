package io.github.ranjandas.kimono.data.apps

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
}
