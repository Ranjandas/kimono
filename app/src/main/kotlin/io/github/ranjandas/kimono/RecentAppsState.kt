package io.github.ranjandas.kimono

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.ranjandas.kimono.core.data.history.LaunchHistory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.UsageHistory
import io.github.ranjandas.kimono.feature.home.RecentApps
import kotlinx.coroutines.flow.first

/**
 * What the last used tile and the recents row show, read again each time Kimono comes back to the front: that's
 * when the user has just left an app, or may have just granted usage access. With usage access they're the apps
 * opened anywhere, topped up with the apps opened from Kimono ([launches]); without it, just the latter. Only with
 * neither is access asked for, where the TV has a screen for it.
 */
@Composable
internal fun rememberRecentApps(history: UsageHistory, launches: LaunchHistory, apps: List<InstalledApp>?): RecentApps {
    var recents by remember { mutableStateOf<RecentApps>(RecentApps.Apps(emptyList())) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(history, launches, apps, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Only apps Kimono can launch: this drops launchers, Settings panels and uninstalled apps.
            val launchable = apps.orEmpty().associateBy { it.packageName }
            val access = history.hasAccess()
            val system = if (access) history.recentPackages() else emptyList()
            recents = recentAppsOf(
                system,
                launches.recent.first(),
                launchable,
                askForAccess = !access && history.canOpenAccessSettings(),
            )
        }
    }
    return recents
}

/**
 * The recents to show: the [system]'s, then any from [launches] it left out, keeping those in [launchable]. The
 * system's history can hold only an app or two, as on a TV whose clock once jumped ahead (#239), so Kimono's own
 * launches fill the rest of the row rather than leave it empty. With nothing to show, the row asks for usage access
 * only if [askForAccess]: where the TV hides that screen (Google TV), a prompt nobody can act on would greet every
 * new user, so the tiles just say the apps they open will show there (#188).
 */
internal fun recentAppsOf(
    system: List<String>,
    launches: List<String>,
    launchable: Map<String, InstalledApp>,
    askForAccess: Boolean,
): RecentApps {
    val apps = (system + launches).distinct().mapNotNull(launchable::get)
    return if (apps.isEmpty() && askForAccess) RecentApps.NeedsAccess else RecentApps.Apps(apps)
}
