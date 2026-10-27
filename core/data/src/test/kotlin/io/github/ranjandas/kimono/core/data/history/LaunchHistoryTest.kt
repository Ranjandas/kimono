package io.github.ranjandas.kimono.core.data.history

import app.cash.turbine.test
import io.github.ranjandas.kimono.core.data.InMemoryDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LaunchHistoryTest {
    private val dataStore = InMemoryDataStore()
    private val history = DataStoreLaunchHistory(dataStore)

    @Test
    fun `starts empty`() = runTest {
        history.recent.test { assertEquals(emptyList<String>(), awaitItem()) }
    }

    @Test
    fun `lists launches newest first, each app once`() = runTest {
        history.record("pkg.plex")
        history.record("pkg.kodi")
        history.record("pkg.plex")
        history.recent.test { assertEquals(listOf("pkg.plex", "pkg.kodi"), awaitItem()) }
    }

    @Test
    fun `a reread of the same file sees what was written`() = runTest {
        history.record("pkg.plex")
        DataStoreLaunchHistory(dataStore).recent.test { assertEquals(listOf("pkg.plex"), awaitItem()) }
    }

    @Test
    fun `keeps only the latest launches`() {
        val full = (1..MAX_LAUNCHES).map { "pkg.$it" }
        val updated = withLaunch(full, "pkg.new")
        assertEquals(MAX_LAUNCHES, updated.size)
        assertEquals("pkg.new", updated.first())
        assertEquals(false, "pkg.$MAX_LAUNCHES" in updated)
    }
}
