package io.github.ranjandas.kimono.feature.home

import androidx.compose.runtime.Immutable
import io.github.ranjandas.kimono.data.apps.InstalledApp
import java.time.Duration

/** The bottom of the top view: which apps were on screen most today. */
sealed interface ScreenTimeToday {
    /** Usage access isn't granted, and nothing else knows how long an app was open. */
    data object NeedsAccess : ScreenTimeToday

    /** Apps Kimono can launch, longest on screen first; may be empty. */
    @Immutable
    data class Apps(val apps: List<AppTime>) : ScreenTimeToday
}

/** How long [app] was on screen. */
@Immutable
data class AppTime(val app: InstalledApp, val time: Duration)
