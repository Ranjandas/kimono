package io.github.ranjandas.kimono

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.ranjandas.kimono.feature.homewatch.StockHome

@Composable
internal fun rememberHomeWatcher(): HomeWatcherState {
    val context = LocalContext.current
    val watcher = remember { HomeWatcherState(HomeWatcher(context), context) }
    LifecycleResumeEffect(watcher) {
        watcher.state = watcher.isOn()
        watcher.googleTvHome = StockHome.isAvailable(context)
        onPauseOrDispose {}
    }
    return watcher
}

/**
 * Kimono as home, as the screens see it: whether this build has it, whether it's [state] on (read again on each
 * return, since it's switched in the system's accessibility settings), and [open] to go there. While it's on, and
 * Google TV's own home is still enabled ([googleTvHome]), [openGoogleTvHome] visits that home (#198).
 */
@Stable
internal class HomeWatcherState(private val watcher: HomeWatcher, private val context: Context) {
    val isInBuild: Boolean get() = watcher.isInBuild
    var state by mutableStateOf(watcher.isOn())
    var googleTvHome by mutableStateOf(StockHome.isAvailable(context))

    fun isOn(): Boolean = watcher.isOn()

    fun open() {
        if (!watcher.openSettings()) {
            Toast.makeText(context, "this TV hides its accessibility settings", Toast.LENGTH_LONG).show()
        }
    }

    /** Opens Google TV's home, with the watcher standing aside until the visit ends. */
    fun openGoogleTvHome() {
        if (!StockHome.open(context)) {
            googleTvHome = false
            Toast.makeText(context, "Google TV's home isn't available", Toast.LENGTH_LONG).show()
        }
    }
}
