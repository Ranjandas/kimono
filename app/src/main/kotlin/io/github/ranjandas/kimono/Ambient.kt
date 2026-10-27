package io.github.ranjandas.kimono

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.design.component.LocalBackdrop
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * The layers around the screens: the wallpaper and the glass tiles frost over, its pixel-art idle version, and
 * the idle dim. While [idle] with a wallpaper, the screens fade away so the pixel art can be seen, then come
 * back at once on wake. Everything but the wallpaper itself moves by [shift].
 */
@Composable
internal fun Ambient(
    wallpaper: WallpaperImages?,
    theme: ThemeId,
    panels: Panels,
    idle: Boolean,
    shift: DpOffset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val fade = rememberFadeIn(wallpaper)
    val backdrop = rememberBackdrop(wallpaper, panels, fade)
    val art = rememberPixelArt(wallpaper, theme)
    val showArt = idle && art != null
    val screensAlpha = animateFloatAsState(
        targetValue = if (showArt) 0f else 1f,
        animationSpec = tween(durationMillis = if (showArt) SCREENS_FADE_MS else 0),
        label = "screens",
    )
    Box(modifier) {
        WallpaperBackground(wallpaper, fade)
        PixelDissolve(art, idle, Modifier.offset(shift.x, shift.y))
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            Box(Modifier.offset(shift.x, shift.y).graphicsLayer { alpha = screensAlpha.value }) { content() }
        }
        IdleDim(idle)
    }
}

private const val SCREENS_FADE_MS = 3_000
