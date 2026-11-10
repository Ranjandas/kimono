package io.github.ranjandas.kimono

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.wallpaper.DrawnWallpaper
import io.github.ranjandas.kimono.core.data.wallpaper.Wallpaper
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.design.component.Backdrop
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.feature.settings.WallpaperChoice
import java.time.LocalDateTime

/**
 * Each theme's wallpaper, downloaded on first use rather than shipped (see [WallpaperCache]). Links are pinned
 * to a commit so the image can't change under us. A theme without one gets its plain background.
 */
private val ThemeWallpapers = mapOf(
    // Omarchy's Quattro, from its Tokyo Night set.
    ThemeId.TokyoNight to omarchyWallpaper("tokyo-night/backgrounds/2-quattro.webp"),
    ThemeId.Catppuccin to omarchyWallpaper("catppuccin/backgrounds/2-waves.webp"),
    ThemeId.Gruvbox to omarchyWallpaper("gruvbox/backgrounds/1-the-backwater.jpg"),
    ThemeId.RosePine to omarchyWallpaper("rose-pine/backgrounds/1-funky-shapes.webp"),
    ThemeId.Nord to omarchyWallpaper("nord/backgrounds/0-black-moon.jpg"),
    // Hokusai's The Great Wave off Kanagawa.
    ThemeId.Kanagawa to omarchyWallpaper("kanagawa/backgrounds/1-kanagawa.jpg"),
    ThemeId.Everforest to omarchyWallpaper("everforest/backgrounds/1-tree-tops.webp"),
)

/** An image from one of Omarchy's themes, at the commit Quattro was picked from. */
private fun omarchyWallpaper(path: String) =
    "https://raw.githubusercontent.com/omacom/omarchy/097c733fc6f24cd437028e0233910b7143fe83f7/themes/$path"

/** The wallpaper to show for these settings, or null for none. */
internal fun wallpaperUrl(
    settings: LauncherSettings,
    theme: ThemeId,
    now: LocalDateTime = LocalDateTime.now(),
): String? = when {
    !settings.wallpaper -> null
    else -> artworkUrl(settings, theme, now)
}

/**
 * The image the pixel art is made from: the wallpaper, even with it switched off on home, since choosing Kimono
 * as the screensaver is asking for the art. Null if the theme has none and no image of the user's is set.
 */
internal fun artworkUrl(settings: LauncherSettings, theme: ThemeId, now: LocalDateTime = LocalDateTime.now()): String? =
    settings.wallpaperUrl ?: themeWallpaper(theme, now)

/**
 * Kimono's own themes draw their wallpapers: Kimono's Yūgure landscape follows the time of day, and Kimono Sumi
 * shows a different pattern each day. The Omarchy themes download theirs.
 */
private fun themeWallpaper(theme: ThemeId, now: LocalDateTime): String? = when (theme) {
    ThemeId.Kimono -> DrawnWallpaper.yugureAt(now.hour).url
    ThemeId.KimonoSumi -> DrawnWallpaper.forDay(now.toLocalDate().toEpochDay()).url
    else -> ThemeWallpapers[theme]
}

internal fun wallpaperChoice(settings: LauncherSettings): WallpaperChoice = when {
    !settings.wallpaper -> WallpaperChoice.Off
    settings.wallpaperUrl != null -> WallpaperChoice.Custom
    else -> WallpaperChoice.Theme
}

/**
 * Every theme's wallpaper, fetched (or for Kimono's, drawn) ahead so that switching theme never waits for the
 * network or a 2 s draw.
 */
internal val themeWallpaperUrls: Collection<String>
    get() = ThemeWallpapers.values + DrawnWallpaper.entries.map { it.url }

/** A loaded wallpaper and its blurred copy, ready to draw. */
internal class WallpaperImages(val image: ImageBitmap, val blurred: ImageBitmap)

private fun Wallpaper.toImages() = WallpaperImages(image.asImageBitmap(), blurred.asImageBitmap())

/**
 * The images at [url], once they're loaded; null for no wallpaper, or if it can't be fetched. One already in
 * memory shows in the same frame. While another loads, the last one stays, so the screen never flashes bare.
 */
@Composable
internal fun rememberWallpaper(cache: WallpaperCache, url: String?): WallpaperImages? {
    val inMemory = remember(cache, url) { url?.let(cache::peek)?.toImages() }
    val loaded by produceState(initialValue = inMemory, cache, url) {
        value = inMemory ?: url?.let { cache.load(it) }?.toImages()
    }
    return if (url == null) null else inMemory ?: loaded
}

/**
 * How far the wallpaper has faded in, from 0 to 1, so the first one doesn't pop in after the first frame. A
 * switch from one wallpaper to another crossfades instead (see [WallpaperBackground]). A function, read while
 * drawing, so the fade doesn't recompose anything.
 */
@Composable
internal fun rememberFadeIn(wallpaper: WallpaperImages?): () -> Float {
    val visibility = remember(wallpaper != null) { Animatable(0f) }
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
 * tiles stays readable over a bright image. A new wallpaper crossfades in over the last one.
 */
@Composable
internal fun WallpaperBackground(wallpaper: WallpaperImages?, fade: () -> Float, modifier: Modifier = Modifier) {
    val background = KimonoTheme.colors.background
    Box(modifier.fillMaxSize().background(background)) {
        Crossfade(
            targetState = wallpaper,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = fade() },
            animationSpec = tween(SWITCH_FADE_MS),
            label = "wallpaper",
        ) { shown ->
            if (shown != null) {
                // The tint goes on as a colour filter, so the image and its tint are one full-screen draw rather
                // than two. The Streamer's GPU redraws the whole screen on every focus move (#91).
                Image(
                    shown.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(background.copy(alpha = WALLPAPER_TINT), BlendMode.SrcAtop),
                )
            }
        }
    }
}

private const val WALLPAPER_TINT = 0.55f
private const val FADE_IN_MS = 600
private const val SWITCH_FADE_MS = 400
