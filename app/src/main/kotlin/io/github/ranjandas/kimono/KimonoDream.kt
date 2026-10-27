package io.github.ranjandas.kimono

import android.service.dreams.DreamService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * Kimono as the system screensaver: the idle pixel art on black, from the same wallpaper cache and in the same
 * theme colours as home. It builds up block by block, holds, breaks down and builds up again a little to one
 * side, so nothing stays lit in one place on an OLED panel. Any key ends it.
 *
 * A dream isn't an activity, so it hosts Compose itself: it is the lifecycle and saved-state owner the view tree
 * needs, alive from attach to detach and resumed while dreaming.
 */
class KimonoDream :
    DreamService(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedState.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        val settingsStore = SettingsStore.create(this)
        val wallpapers = WallpaperCache.create(this)
        val view = ComposeView(this).apply {
            setContent { DreamArt(settingsStore, wallpapers) }
        }
        // Compose looks for the owners from the root of the window, which is the dream's decor view.
        window.decorView.setViewTreeLifecycleOwner(this)
        window.decorView.setViewTreeSavedStateRegistryOwner(this)
        setContentView(view)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDetachedFromWindow()
    }
}

/** The theme's pixel art cycling on black; just black until it's made, or if there's no image to make it from. */
@Composable
private fun DreamArt(settingsStore: SettingsStore, wallpapers: WallpaperCache) {
    val settings by settingsStore.settings.collectAsStateWithLifecycle(initialValue = null)
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        val loaded = settings ?: return@Box
        val theme = ThemeId.fromName(loaded.themeName)
        val art = rememberPixelArt(rememberWallpaper(wallpapers, artworkUrl(loaded, theme)), theme)
        PixelDissolve(art, idle = true)
    }
}
