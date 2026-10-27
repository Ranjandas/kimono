package io.github.ranjandas.kimono.data.media

import app.cash.turbine.test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MediaRepositoryTest {
    @Test
    fun `shows the newest items first, up to the limit`() = runTest {
        val cache = InMemoryMediaCache(listOf(item("a", "1", 10), item("b", "2", 30), item("a", "3", 20)))
        val repository = MediaRepository(emptyList(), cache, limit = 2)

        repository.continueWatching.test {
            assertEquals(listOf("2", "3"), awaitItem().map { it.id })
        }
    }

    @Test
    fun `refresh replaces each source's items`() = runTest {
        val jellyfin = FakeMediaSource("jellyfin", listOf(item("jellyfin", "new", 5)))
        val cache = InMemoryMediaCache(listOf(item("jellyfin", "old", 1)))
        val repository = MediaRepository(listOf(jellyfin), cache)

        val result = repository.refresh()

        assertEquals(setOf("jellyfin"), result.refreshed)
        assertTrue(result.failed.isEmpty())
        assertEquals(listOf("new"), cache.state.value.map { it.id })
    }

    @Test
    fun `a source that fails keeps its cached items and the others still refresh`() = runTest {
        val jellyfin = FakeMediaSource("jellyfin").apply { error = IOException("offline") }
        val trakt = FakeMediaSource("trakt", listOf(item("trakt", "t2", 9)))
        val cache = InMemoryMediaCache(listOf(item("jellyfin", "j1", 1), item("trakt", "t1", 2)))
        val repository = MediaRepository(listOf(jellyfin, trakt), cache)

        val result = repository.refresh()

        assertEquals(setOf("jellyfin"), result.failed)
        assertEquals(setOf("trakt"), result.refreshed)
        assertEquals(setOf("j1", "t2"), cache.state.value.mapTo(mutableSetOf()) { it.id })
    }

    @Test
    fun `items from a disconnected source are dropped`() = runTest {
        val trakt = FakeMediaSource("trakt", listOf(item("trakt", "t1", 2)))
        val cache = InMemoryMediaCache(listOf(item("jellyfin", "j1", 1)))

        MediaRepository(listOf(trakt), cache).refresh()

        assertEquals(listOf("t1"), cache.state.value.map { it.id })
    }

    @Test
    fun `cancellation isn't treated as a failed source`() = runTest {
        val jellyfin = FakeMediaSource("jellyfin").apply { error = CancellationException("stopped") }
        val trakt = FakeMediaSource("trakt")
        val repository = MediaRepository(listOf(jellyfin, trakt), InMemoryMediaCache())

        assertThrows<CancellationException> { repository.refresh() }
        assertEquals(0, trakt.calls)
    }

    @Test
    fun `hasSources is false with nothing connected`() {
        assertFalse(MediaRepository(emptyList(), InMemoryMediaCache()).hasSources)
    }
}
