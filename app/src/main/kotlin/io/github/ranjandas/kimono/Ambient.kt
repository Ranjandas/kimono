package io.github.ranjandas.kimono

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.DpOffset
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.design.component.LocalBackdrop
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import kotlin.time.Duration
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

/**
 * The layers around the screens: the wallpaper and the glass tiles frost over, its pixel-art idle version, and
 * the idle dim. While [idle] with a wallpaper, the screens fade away so the pixel art can be seen, then come
 * back at once on wake. Everything but the wallpaper itself moves by [shift].
 *
 * For [keepAwakeFor] after the pixel art starts, the screen stays on, so Google TV's own screensaver doesn't
 * start over it: Google TV has no way to pick another one without adb. Only the art keeps it awake, since that
 * never holds still; the plain idle dim over a static screen doesn't. Holding the screen on also holds off the
 * TV's sleep timer, so the hold ends when that timer would have run out (see [tvSleepTimeout]).
 */
@Composable
internal fun Ambient(
    wallpaper: WallpaperImages?,
    theme: ThemeId,
    panels: Panels,
    idle: Boolean,
    shift: DpOffset,
    modifier: Modifier = Modifier,
    keepAwakeFor: Duration = Duration.ZERO,
    content: @Composable () -> Unit,
) {
    val fade = rememberFadeIn(wallpaper)
    val backdrop = rememberBackdrop(wallpaper, panels, fade)
    val art = rememberPixelArt(wallpaper, theme)
    val showArt = idle && art != null
    KeepScreenOn(showArt, keepAwakeFor)
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

/** Holds the screen on through this composition's window from when [on] turns true, for at most [limit]. */
@Composable
private fun KeepScreenOn(on: Boolean, limit: Duration) {
    val view = LocalView.current
    LaunchedEffect(view, on, limit) {
        if (!on || !limit.isPositive()) return@LaunchedEffect
        view.keepScreenOn = true
        Log.d("Kimono", "Holding the screen on for $limit")
        try {
            waitFor(limit)
        } finally {
            view.keepScreenOn = false
            Log.d("Kimono", "Let the screen go")
        }
    }
}

/** Waits for [limit], or forever when it's infinite, until cancelled. */
private suspend fun waitFor(limit: Duration) {
    if (limit.isInfinite()) awaitCancellation() else delay(limit)
}

private const val SCREENS_FADE_MS = 3_000
