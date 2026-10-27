package io.github.ranjandas.kimono.core.data.idle

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Decides when the launcher is idle: after [timeout] without a key press. The first key press while idle only
 * wakes the screen; it and its repeats and release are swallowed, so a press meant to wake never launches
 * anything. Feed it every key event with [onKey].
 */
class IdleController(private val scope: CoroutineScope, timeout: Duration) {
    private val idleState = MutableStateFlow(false)
    val idle: StateFlow<Boolean> = idleState.asStateFlow()

    /** [Duration.INFINITE] never goes idle. A change applies from now. */
    var timeout: Duration = timeout
        set(value) {
            field = value
            if (value.isInfinite()) idleState.value = false
            if (timer != null || value.isInfinite()) restartTimer()
        }

    private var timer: Job? = null

    // The key that woke the screen, until it's released.
    private var swallowing = false

    /** Call for every key event. True when the event only woke the screen and mustn't reach the UI. */
    fun onKey(isDown: Boolean): Boolean {
        val swallow = when {
            idleState.value -> {
                idleState.value = false
                swallowing = isDown
                true
            }

            swallowing -> {
                if (!isDown) swallowing = false
                true
            }

            else -> false
        }
        restartTimer()
        return swallow
    }

    /** The launcher is in front again: awake, with a fresh countdown. */
    fun resume() {
        idleState.value = false
        swallowing = false
        restartTimer()
    }

    /** The launcher is in the background, so it can't go idle. */
    fun pause() {
        timer?.cancel()
        timer = null
    }

    private fun restartTimer() {
        timer?.cancel()
        timer = if (timeout.isInfinite()) {
            null
        } else {
            scope.launch {
                delay(timeout)
                idleState.value = true
            }
        }
    }
}
