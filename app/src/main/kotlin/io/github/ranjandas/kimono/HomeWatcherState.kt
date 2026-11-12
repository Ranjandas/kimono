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

@Composable
internal fun rememberHomeWatcher(): HomeWatcherState {
    val context = LocalContext.current
    val watcher = remember { HomeWatcherState(HomeWatcher(context), context) }
    LifecycleResumeEffect(watcher) {
        watcher.state = watcher.isOn()
        onPauseOrDispose {}
    }
    return watcher
}

/**
 * Kimono as home, as the screens see it: whether this build has it, whether it's [state] on (read again on each
 * return, since it's switched in the system's accessibility settings), and [open] to go there.
 */
@Stable
internal class HomeWatcherState(private val watcher: HomeWatcher, private val context: Context) {
    val isInBuild: Boolean get() = watcher.isInBuild
    var state by mutableStateOf(watcher.isOn())

    fun isOn(): Boolean = watcher.isOn()

    fun open() {
        if (!watcher.openSettings()) {
            Toast.makeText(context, "this TV hides its accessibility settings", Toast.LENGTH_LONG).show()
        }
    }
}
