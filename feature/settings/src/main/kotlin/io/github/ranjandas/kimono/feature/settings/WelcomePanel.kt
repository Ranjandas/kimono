package io.github.ranjandas.kimono.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.ranjandas.kimono.core.design.component.Option
import io.github.ranjandas.kimono.core.design.component.OptionsPanel

/**
 * The first-run welcome (#187): the two things a new user can't find alone. Google TV keeps opening its own home
 * until Kimono is made the home, which is a row behind the settings gear, and an app's options are behind a held OK,
 * whose hint fades after a few seconds.
 *
 * [homeWatcher] is whether Kimono as home is on, or null when this build doesn't have it. Only when it's off does the
 * welcome offer it, and then the choice goes through the same disclosure as in settings before [onTurnOnHome] opens
 * the system's accessibility settings. Every way out, Back included, calls [onDone], so the welcome is shown once.
 */
@Composable
fun WelcomePanel(
    homeWatcher: Boolean?,
    onTurnOnHome: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var disclosing by rememberSaveable { mutableStateOf(false) }
    // A fresh panel for each step, so the disclosure's first option takes focus as the welcome's did.
    key(disclosing) {
        if (disclosing) {
            HomeWatcherDisclosurePanel(
                onAgree = {
                    onDone()
                    onTurnOnHome()
                },
                onDismiss = onDone,
                modifier = modifier,
            )
        } else {
            val offerHome = homeWatcher == false
            OptionsPanel(
                title = "Welcome to Kimono",
                options = if (offerHome) {
                    listOf(Option("Make Kimono my home") { disclosing = true }, Option("Not now", onSelect = onDone))
                } else {
                    listOf(Option("Got it", onSelect = onDone))
                },
                onDismiss = onDone,
                modifier = modifier,
                message = if (offerHome) WELCOME_HOME + " " + WELCOME_TIPS else WELCOME_TIPS,
            )
        }
    }
}

internal const val WELCOME_HOME =
    "Pressing Home still opens Google TV's own home screen until you make Kimono your home. It takes a minute, " +
        "and you can change your mind at any time."

internal const val WELCOME_TIPS =
    "Hold OK on any app to pin it to a workspace, move it or change its icon. Press Left or Right at the edge " +
        "of the screen to switch workspace."
