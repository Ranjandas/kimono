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
 * opened anywhere; without it, the apps opened from Kimono ([launches]), and only with neither is access asked for.
 */
@Composable
internal fun rememberRecentApps(history: UsageHistory, launches: LaunchHistory, apps: List<InstalledApp>?): RecentApps {
    var recents by remember { mutableStateOf<RecentApps>(RecentApps.Apps(emptyList())) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(history, launches, apps, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Only apps Kimono can launch: this drops launchers, Settings panels and uninstalled apps.
            val launchable = apps.orEmpty().associateBy { it.packageName }
            val packages = if (history.hasAccess()) history.recentPackages() else launches.recent.first()
            recents = if (packages.isEmpty() && !history.hasAccess()) {
                RecentApps.NeedsAccess
            } else {
                RecentApps.Apps(packages.mapNotNull(launchable::get))
            }
        }
    }
    return recents
}
