package io.github.ranjandas.kimono

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.feature.settings.WallpaperChoice

/**
 * Each theme's wallpaper, downloaded on first use rather than shipped (see [WallpaperCache]). Links are pinned
 * to a commit so the image can't change under us. A theme without one gets its plain background.
 */
private val ThemeWallpapers = mapOf(
    // Omarchy's Quattro, from its Tokyo Night set.
    ThemeId.TokyoNight to "https://raw.githubusercontent.com/basecamp/omarchy/" +
        "097c733fc6f24cd437028e0233910b7143fe83f7/themes/tokyo-night/backgrounds/2-quattro.webp",
)

/** The wallpaper to show for these settings, or null for none. */
internal fun wallpaperUrl(settings: LauncherSettings, theme: ThemeId): String? = when {
    !settings.wallpaper -> null
    else -> settings.wallpaperUrl ?: ThemeWallpapers[theme]
}

internal fun wallpaperChoice(settings: LauncherSettings): WallpaperChoice = when {
    !settings.wallpaper -> WallpaperChoice.Off
    settings.wallpaperUrl != null -> WallpaperChoice.Custom
    else -> WallpaperChoice.Theme
}

/** The image at [url], once it's loaded; null while loading, for no wallpaper, or if it can't be fetched. */
@Composable
internal fun rememberWallpaper(cache: WallpaperCache, url: String?): ImageBitmap? {
    val wallpaper by produceState<ImageBitmap?>(initialValue = null, url) {
        value = url?.let { cache.load(it)?.asImageBitmap() }
    }
    return wallpaper
}

/**
 * The theme background, with [wallpaper] over it under a tint of the same colour, so text on and between the
 * tiles stays readable over a bright image.
 */
@Composable
internal fun WallpaperBackground(wallpaper: ImageBitmap?, modifier: Modifier = Modifier) {
    val background = KimonoTheme.colors.background
    Box(modifier.fillMaxSize().background(background)) {
        if (wallpaper != null) {
            Image(
                wallpaper,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(background.copy(alpha = WALLPAPER_TINT)))
        }
    }
}

private const val WALLPAPER_TINT = 0.55f
