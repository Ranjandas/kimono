package io.github.ranjandas.kimono.core.design.component

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Turns a held OK into a long press. Compose's `clickable` turns the OK key into a click but has no notion
 * of holding it, so the key is handled here: the first repeat (about half a second) is the long press, and
 * the rest of the repeats and the key release are swallowed so no click follows.
 */
internal class HeldOk {
    // Plain state: nothing is drawn from it, so changing it needn't recompose.
    private var held = false

    fun onKey(event: KeyEvent, onLongClick: () -> Unit): Boolean = when {
        event.key !in OkKeys -> false

        event.type == KeyEventType.KeyUp -> held.also { held = false }

        event.nativeKeyEvent.repeatCount == 1 -> {
            held = true
            onLongClick()
            true
        }

        else -> held
    }
}

// The keys a remote or keyboard sends for OK; also used by OptionsPanel.
internal val OkKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)
