package io.github.ranjandas.kimono

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.design.component.Backdrop
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.feature.settings.WallpaperChoice

/**
 * Each theme's wallpaper, downloaded on first use rather than shipped (see [WallpaperCache]). Links are pinned
 * to a commit so the image can't change under us. A theme without one gets its plain background.
 */
private val ThemeWallpapers = mapOf(
    // Omarchy's Quattro, from its Tokyo Night set.
    ThemeId.TokyoNight to "https://raw.githubusercontent.com/omacom/omarchy/" +
        "097c733fc6f24cd437028e0233910b7143fe83f7/themes/tokyo-night/backgrounds/2-quattro.webp",
)

/** The wallpaper to show for these settings, or null for none. */
internal fun wallpaperUrl(settings: LauncherSettings, theme: ThemeId): String? = when {
    !settings.wallpaper -> null
    else -> artworkUrl(settings, theme)
}

/**
 * The image the pixel art is made from: the wallpaper, even with it switched off on home, since choosing Kimono
 * as the screensaver is asking for the art. Null if the theme has none and no image of the user's is set.
 */
internal fun artworkUrl(settings: LauncherSettings, theme: ThemeId): String? =
    settings.wallpaperUrl ?: ThemeWallpapers[theme]

internal fun wallpaperChoice(settings: LauncherSettings): WallpaperChoice = when {
    !settings.wallpaper -> WallpaperChoice.Off
    settings.wallpaperUrl != null -> WallpaperChoice.Custom
    else -> WallpaperChoice.Theme
}

/** A loaded wallpaper and its blurred copy, ready to draw. */
internal class WallpaperImages(val image: ImageBitmap, val blurred: ImageBitmap)

/** The images at [url], once they're loaded; null while loading, for no wallpaper, or if it can't be fetched. */
@Composable
internal fun rememberWallpaper(cache: WallpaperCache, url: String?): WallpaperImages? {
    val wallpaper by produceState<WallpaperImages?>(initialValue = null, url) {
        val loaded = url?.let { cache.load(it) }
        value = loaded?.let { WallpaperImages(it.image.asImageBitmap(), it.blurred.asImageBitmap()) }
    }
    return wallpaper
}

/**
 * How far [wallpaper] has faded in, from 0 to 1, so it doesn't pop in after the first frame. A function, read
 * while drawing, so the fade doesn't recompose anything.
 */
@Composable
internal fun rememberFadeIn(wallpaper: WallpaperImages?): () -> Float {
    val visibility = remember(wallpaper) { Animatable(0f) }
    LaunchedEffect(visibility) { visibility.animateTo(1f, tween(FADE_IN_MS)) }
    return remember(visibility) { { visibility.value } }
}

/** What tiles frost over; null without a wallpaper, or with solid panels. */
@Composable
internal fun rememberBackdrop(wallpaper: WallpaperImages?, panels: Panels, fade: () -> Float): Backdrop? {
    val tint = KimonoTheme.colors.background.copy(alpha = WALLPAPER_TINT)
    return remember(wallpaper, panels, tint, fade) {
        when {
            wallpaper == null || panels == Panels.Solid -> null
            else -> Backdrop(wallpaper.blurred, tint, panels.opacity, fade)
        }
    }
}

/**
 * The theme background, with the wallpaper over it under a tint of the same colour, so text on and between the
 * tiles stays readable over a bright image.
 */
@Composable
internal fun WallpaperBackground(wallpaper: WallpaperImages?, fade: () -> Float, modifier: Modifier = Modifier) {
    val background = KimonoTheme.colors.background
    Box(modifier.fillMaxSize().background(background)) {
        if (wallpaper != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = fade() }) {
                Image(
                    wallpaper.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(Modifier.fillMaxSize().background(background.copy(alpha = WALLPAPER_TINT)))
            }
        }
    }
}

private const val WALLPAPER_TINT = 0.55f
private const val FADE_IN_MS = 600
