package io.github.ranjandas.kimono

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.github.ranjandas.kimono.core.data.history.LaunchHistory
import io.github.ranjandas.kimono.core.data.idle.IdleController
import io.github.ranjandas.kimono.core.data.settings.IdleTimeout
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.core.data.wallpaper.DrawnWallpaper
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceStore
import io.github.ranjandas.kimono.data.apps.AppActions
import io.github.ranjandas.kimono.data.apps.AppLauncher
import io.github.ranjandas.kimono.data.apps.AppsRepository
import io.github.ranjandas.kimono.data.apps.UsageHistory
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    private val dependencies by lazy {
        KimonoDependencies(
            appsRepository = AppsRepository.create(this),
            appLauncher = AppLauncher.create(this),
            appActions = AppActions.create(this),
            workspaceStore = WorkspaceStore.create(this),
            settingsStore = SettingsStore.create(this),
            usageHistory = UsageHistory.create(this),
            launchHistory = LaunchHistory.create(this),
            wallpapers = WallpaperCache.create(this),
        )
    }

    // Counts the Home presses (and launches from the apps row) that reach the running activity. Each one sends
    // the launcher back to its start: home, workspace 1, focus on the first app.
    private var homePresses by mutableIntStateOf(0)

    // Sees every key before the UI does, so the press that wakes the screen goes no further.
    private val idle by lazy { IdleController(lifecycleScope, IdleTimeout.Default.duration) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android keeps home activities in their own task, so the first Home press after Kimono was opened some
        // other way (from the apps row, or adb) starts a second instance rather than reaching this one. Treat that
        // start as a Home press too. A restore after process death keeps its state instead.
        if (savedInstanceState == null && intent.hasCategory(Intent.CATEGORY_HOME)) homePresses++
        applyWallpaperExtra(intent)
        setContent { KimonoApp(dependencies, idle, homePresses) }
    }

    // singleTask: once running, Home or opening Kimono from the apps row comes back here as a new intent.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) homePresses++
        applyWallpaperExtra(intent)
        idle.resume()
    }

    override fun onStart() {
        super.onStart()
        idle.resume()
    }

    override fun onStop() {
        idle.pause()
        super.onStop()
    }

    // Activity.dispatchKeyEvent is public API; lint only objects because androidx.core's ComponentActivity
    // re-declares it as restricted.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        idle.onKey(isDown = event.action == KeyEvent.ACTION_DOWN) || super.dispatchKeyEvent(event)

    // Sets a wallpaper of the user's own, since typing a URL with a remote is painful (docs/DEVELOPING.md). An
    // empty value goes back to the theme's. Only https links are taken, and the kimono: links of the wallpapers
    // Kimono draws, which pin one of those under any theme.
    private fun applyWallpaperExtra(intent: Intent) {
        val url = intent.getStringExtra(WALLPAPER_EXTRA) ?: return
        if (url.isNotEmpty() && !url.startsWith("https://") && DrawnWallpaper.fromUrl(url) == null) return
        lifecycleScope.launch {
            dependencies.settingsStore.update { it.copy(wallpaperUrl = url.ifEmpty { null }, wallpaper = true) }
        }
    }

    private companion object {
        const val WALLPAPER_EXTRA = "kimono.wallpaper"
    }
}
