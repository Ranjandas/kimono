package io.github.ranjandas.kimono.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * [produceState] for a value that's read again and again, such as the top view's stats: [producer] runs only while
 * Kimono is on screen, and starts again from the top each time it comes back. Home stays composed while another app
 * plays, so a plain loop would keep reading for hours behind a film (#190).
 */
// [producer] sets the value, which lint can't see into.
@Suppress("ProduceStateDoesNotAssignValue")
@Composable
internal fun <T> produceStateWhileStarted(
    initialValue: T,
    vararg keys: Any?,
    producer: suspend ProduceStateScope<T>.() -> Unit,
): State<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentProducer by rememberUpdatedState(producer)
    return produceState(initialValue, lifecycle, *keys) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { currentProducer(this@produceState) }
    }
}
