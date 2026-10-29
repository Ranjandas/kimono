package io.github.ranjandas.kimono

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * Gets wallpapers ready before they're needed, so a theme switch shows its wallpaper in the same frame: every
 * theme's is downloaded ahead, and once [wallpaper] shows, the themes either side of [theme] (one step left or
 * right on the settings row) are decoded into memory too, since decoding one takes a few frames.
 */
@Composable
internal fun WarmWallpapers(
    cache: WallpaperCache,
    settings: LauncherSettings,
    theme: ThemeId,
    wallpaper: WallpaperImages?,
) {
    LaunchedEffect(cache, settings.wallpaper) {
        if (settings.wallpaper) cache.prefetch(themeWallpaperUrls)
    }
    LaunchedEffect(cache, wallpaper) {
        if (wallpaper == null) return@LaunchedEffect
        for (neighbour in theme.neighbours()) {
            wallpaperUrl(settings, neighbour)?.let { cache.load(it) }
        }
    }
}

private fun ThemeId.neighbours(): List<ThemeId> {
    val themes = ThemeId.entries
    return listOf(themes[(ordinal + 1).mod(themes.size)], themes[(ordinal - 1).mod(themes.size)])
}
