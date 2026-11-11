package io.github.ranjandas.kimono

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeWatcherTest {
    private val pkg = "io.github.ranjandas.kimono"
    private val service = HomeWatcher.SERVICE_CLASS

    @Test
    fun `finds the service written in full or short`() {
        assertTrue(HomeWatcher.isListed("$pkg/$service", pkg, service))
        assertTrue(HomeWatcher.isListed("$pkg/.feature.homewatch.HomeWatcherService", pkg, service))
    }

    @Test
    fun `finds it among other services`() {
        assertTrue(HomeWatcher.isListed("com.example/.Reader:$pkg/$service", pkg, service))
    }

    @Test
    fun `doesn't match another app's service or an empty list`() {
        assertFalse(HomeWatcher.isListed("com.example/.HomeWatcherService", pkg, service))
        assertFalse(HomeWatcher.isListed("", pkg, service))
    }
}
