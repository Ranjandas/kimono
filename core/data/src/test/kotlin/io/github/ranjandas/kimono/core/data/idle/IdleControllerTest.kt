package io.github.ranjandas.kimono.core.data.idle

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IdleControllerTest {
    @Test
    fun `goes idle once the timeout passes without a key`() = runTest {
        val idle = started(45.seconds)

        advanceTimeBy(44.seconds)
        assertFalse(idle.idle.value)
        advanceTimeBy(2.seconds)
        assertTrue(idle.idle.value)
    }

    @Test
    fun `a key press starts the countdown again`() = runTest {
        val idle = started(45.seconds)

        advanceTimeBy(40.seconds)
        idle.press()
        advanceTimeBy(40.seconds)
        assertFalse(idle.idle.value)
    }

    @Test
    fun `the first key while idle only wakes, along with its repeats and release`() = runTest {
        val idle = started(45.seconds)
        advanceTimeBy(46.seconds)

        assertTrue(idle.onKey(isDown = true))
        assertFalse(idle.idle.value)
        assertTrue(idle.onKey(isDown = true))
        assertTrue(idle.onKey(isDown = false))
        assertFalse(idle.onKey(isDown = true))
        assertFalse(idle.onKey(isDown = false))
    }

    @Test
    fun `keys go through while awake`() = runTest {
        val idle = started(45.seconds)

        assertFalse(idle.onKey(isDown = true))
        assertFalse(idle.onKey(isDown = false))
    }

    @Test
    fun `off never goes idle`() = runTest {
        val idle = started(Duration.INFINITE)

        advanceTimeBy(3_600.seconds)
        assertFalse(idle.idle.value)
    }

    @Test
    fun `turning it off wakes the screen`() = runTest {
        val idle = started(45.seconds)
        advanceTimeBy(46.seconds)

        idle.timeout = Duration.INFINITE
        assertFalse(idle.idle.value)
    }

    @Test
    fun `a new timeout applies from now`() = runTest {
        val idle = started(45.seconds)

        idle.timeout = 15.seconds
        advanceTimeBy(16.seconds)
        assertTrue(idle.idle.value)
    }

    @Test
    fun `it can't go idle in the background, and comes back awake`() = runTest {
        val idle = started(45.seconds)
        advanceTimeBy(46.seconds)

        idle.pause()
        idle.resume()
        assertFalse(idle.idle.value)
        idle.pause()
        advanceTimeBy(100.seconds)
        assertFalse(idle.idle.value)
    }

    private fun TestScope.started(timeout: Duration) = IdleController(backgroundScope, timeout).also {
        it.resume()
        runCurrent()
    }

    private fun IdleController.press() {
        onKey(isDown = true)
        onKey(isDown = false)
    }
}
