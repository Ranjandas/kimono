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
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.UsageHistory
import io.github.ranjandas.kimono.feature.home.RecentApps

/**
 * The recents row's content, read again each time Kimono comes back to the front: that's when the user has
 * just left an app, or may have just granted usage access.
 */
@Composable
internal fun rememberRecentApps(history: UsageHistory, apps: List<InstalledApp>?): RecentApps {
    var recents by remember { mutableStateOf<RecentApps>(RecentApps.Apps(emptyList())) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(history, apps, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            recents = if (history.hasAccess()) {
                // Only apps Kimono can launch: this drops launchers, Settings panels and uninstalled apps.
                val launchable = apps.orEmpty().associateBy { it.packageName }
                RecentApps.Apps(history.recentPackages().mapNotNull(launchable::get))
            } else {
                RecentApps.NeedsAccess
            }
        }
    }
    return recents
}
