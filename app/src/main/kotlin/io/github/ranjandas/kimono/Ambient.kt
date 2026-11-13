package io.github.ranjandas.kimono

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.DpOffset
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.design.component.LocalBackdrop
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * The layers around the screens: the wallpaper and the glass tiles frost over, its pixel-art idle version, and
 * the idle dim. While [idle] with a wallpaper, the screens fade away so the pixel art can be seen, then come
 * back at once on wake. Everything but the wallpaper itself moves by [shift].
 *
 * With [keepAwake], the screen stays on while the pixel art runs, so Google TV's own screensaver doesn't start
 * over it: Google TV has no way to pick another one without adb. Only the art keeps it awake, since that never
 * holds still; the plain idle dim over a static screen doesn't. The TV's energy timer still turns the screen off.
 */
@Composable
internal fun Ambient(
    wallpaper: WallpaperImages?,
    theme: ThemeId,
    panels: Panels,
    idle: Boolean,
    shift: DpOffset,
    modifier: Modifier = Modifier,
    keepAwake: Boolean = false,
    content: @Composable () -> Unit,
) {
    val fade = rememberFadeIn(wallpaper)
    val backdrop = rememberBackdrop(wallpaper, panels, fade)
    val art = rememberPixelArt(wallpaper, theme)
    val showArt = idle && art != null
    KeepScreenOn(showArt && keepAwake)
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

/** Holds the screen on while [on], through the window this composition is in. */
@Composable
private fun KeepScreenOn(on: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, on) {
        view.keepScreenOn = on
        onDispose { view.keepScreenOn = false }
    }
}

private const val SCREENS_FADE_MS = 3_000
