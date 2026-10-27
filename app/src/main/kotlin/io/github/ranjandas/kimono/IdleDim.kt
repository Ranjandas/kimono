package io.github.ranjandas.kimono

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

/**
 * Darkens the whole screen while idle, slowly so it doesn't startle, and lifts at once on wake. It's black
 * whatever the theme: on an OLED panel black pixels are off, which is the point.
 */
@Composable
internal fun IdleDim(idle: Boolean, modifier: Modifier = Modifier) {
    val dim by animateFloatAsState(
        targetValue = if (idle) IDLE_DIM else 0f,
        animationSpec = tween(durationMillis = if (idle) FADE_OUT_MS else 0),
        label = "idle dim",
    )
    if (dim > 0f) Box(modifier.fillMaxSize().alpha(dim).background(Color.Black))
}

private const val IDLE_DIM = 0.7f
private const val FADE_OUT_MS = 2_000
