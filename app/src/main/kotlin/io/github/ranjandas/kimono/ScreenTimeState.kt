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
import io.github.ranjandas.kimono.feature.home.AppTime
import io.github.ranjandas.kimono.feature.home.ScreenTimeToday
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay

/**
 * The apps on screen most since midnight, for the top view, read while Kimono is in front and then every minute.
 * Only read while [enabled], that is while the system tile shows the top view.
 */
@Composable
internal fun rememberScreenTimeToday(
    history: UsageHistory,
    apps: List<InstalledApp>?,
    enabled: Boolean,
): ScreenTimeToday {
    var today by remember { mutableStateOf<ScreenTimeToday>(ScreenTimeToday.Apps(emptyList())) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(history, apps, enabled, lifecycle) {
        if (enabled) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    today = readScreenTime(history, apps.orEmpty())
                    delay(SCREEN_TIME_REFRESH_MILLIS)
                }
            }
        }
    }
    return today
}

/** Only apps Kimono can launch, which drops the system's own screens, as the recents row does. */
private suspend fun readScreenTime(history: UsageHistory, apps: List<InstalledApp>): ScreenTimeToday {
    if (!history.hasAccess()) return ScreenTimeToday.NeedsAccess
    val launchable = apps.associateBy { it.packageName }
    val midnight = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
    val times = history.screenTime(since = midnight)
    return ScreenTimeToday.Apps(times.mapNotNull { use -> launchable[use.packageName]?.let { AppTime(it, use.time) } })
}

private const val SCREEN_TIME_REFRESH_MILLIS = 60_000L
