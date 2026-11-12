package io.github.ranjandas.kimono.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import io.github.ranjandas.kimono.core.design.component.Option
import io.github.ranjandas.kimono.core.design.component.OptionsPanel
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut

/**
 * What "Kimono as home" uses and why, shown before Kimono sends anyone to the accessibility settings to turn it on.
 * Play asks an app that isn't an accessibility tool to say this in the app, and to have the user agree, before it
 * uses the accessibility API (docs/PLAY-STORE.md). Not now, or Back, leaves it off. Either way focus goes back to
 * the row it came from, or the D-pad would have nothing to move. [ToggleHint] sketches the switch to turn on next.
 */
@Composable
internal fun HomeWatcherDisclosure(state: DisclosureState, onAgree: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(state.refocus) {
        if (state.refocus) state.row.requestFocusWhenLaidOut()
        state.refocus = false
    }
    if (!state.showing) return
    HomeWatcherDisclosurePanel(
        onAgree = {
            state.close()
            onAgree()
        },
        onDismiss = state::close,
        modifier = modifier,
    )
}

/**
 * The disclosure itself: what the service does, and the switch to turn on next. [onAgree] goes on to the system's
 * accessibility settings; Not now, or Back, calls [onDismiss]. Settings and the first-run welcome both show it.
 */
@Composable
internal fun HomeWatcherDisclosurePanel(onAgree: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    OptionsPanel(
        title = "Kimono as home",
        options = listOf(
            Option("Agree and continue", onSelect = onAgree),
            Option("Not now", onSelect = onDismiss),
        ),
        onDismiss = onDismiss,
        modifier = modifier,
        message = HOME_WATCHER_DISCLOSURE,
        illustration = { ToggleHint() },
    )
}

/** Whether the disclosure is showing, and the row it returns focus to. */
@Stable
internal class DisclosureState {
    val row = FocusRequester()
    var showing by mutableStateOf(false)
        private set
    var refocus by mutableStateOf(false)

    /** Turning it off needs no disclosure, so [openSettings] goes straight to the system settings when it's [on]. */
    fun open(on: Boolean, openSettings: () -> Unit) {
        if (on) openSettings() else showing = true
    }

    fun close() {
        showing = false
        refocus = true
    }
}

internal const val HOME_WATCHER_DISCLOSURE =
    "Google TV always opens its own home when you press Home. To open Kimono instead, Kimono uses an Android " +
        "accessibility service. It only notices when Google TV's home comes to the front, and then opens Kimono. " +
        "It doesn't read what's on screen, what you type or anything in other apps, and nothing leaves your TV. " +
        "On the next screen, go down to Services, choose Kimono as home and turn it on. You can turn it off " +
        "there at any time."
