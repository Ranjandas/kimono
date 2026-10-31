package io.github.ranjandas.kimono.core.design.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import kotlinx.coroutines.delay

/**
 * The row of remote keys along the bottom of a screen, after a remote icon, saying what each one does now. It shows
 * when the screen opens and fades out after [HintBarDefaults.SHOWN_MS] (#124), so the screen isn't left with static
 * chrome; it keeps its space, so nothing moves when it goes.
 */
@Composable
fun HintBar(hints: List<Hint>, modifier: Modifier = Modifier) {
    val shown = remember { Animatable(1f) }
    LaunchedEffect(shown) {
        delay(HintBarDefaults.SHOWN_MS)
        shown.animateTo(0f, tween(HintBarDefaults.FADE_MS))
    }
    Row(
        // Modulated alpha needs no offscreen layer, and the hints never overlap.
        modifier = modifier.graphicsLayer {
            alpha = shown.value
            compositingStrategy = CompositingStrategy.ModulateAlpha
        },
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.XXL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.Remote, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.label)
        hints.forEach { KeyHint(it.key, it.action) }
    }
}

/** One entry in a [HintBar]: a remote [key] and the [action] it does. */
@Immutable
data class Hint(val key: String, val action: String)

object HintBarDefaults {
    /** How long the hints stay after a screen opens. */
    const val SHOWN_MS = 5_000L

    /** How long they take to fade out. */
    const val FADE_MS = 600
}
