package io.github.ranjandas.kimono.core.design.component

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/**
 * Requests focus, retrying for a few frames while the target isn't laid out yet: a lazy list composing its
 * first rows, or a screen restored after process death before its window has focus.
 */
suspend fun FocusRequester.requestFocusWhenLaidOut() {
    repeat(FOCUS_ATTEMPTS) {
        if (runCatching { requestFocus() }.getOrDefault(false)) return
        withFrameNanos { }
    }
}

private const val FOCUS_ATTEMPTS = 10
