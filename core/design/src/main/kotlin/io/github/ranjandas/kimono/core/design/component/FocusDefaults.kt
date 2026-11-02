package io.github.ranjandas.kimono.core.design.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

/**
 * Kimono's one focus language (#126): a focused card or tile grows a little and its border turns to the accent; a
 * focused row or chip takes an accent-tinted fill. Both ease in over [FocusDefaults.DURATION_MS], so a D-pad move
 * reads as motion rather than a snap.
 */
object FocusDefaults {
    /** How long focus takes to arrive or leave. */
    const val DURATION_MS = 140

    /** How much a focused card grows. Scaled in a graphics layer, so nothing around it moves. */
    const val CARD_SCALE = 1.04f

    /** A focused tile grows less, since tiles are larger. */
    const val TILE_SCALE = 1.015f

    /** The accent's strength in a focused row's fill. */
    const val ROW_TINT_ALPHA = 0.16f

    /** An unfocused tile border's strength, so tiles read as windows without outlining the whole screen. */
    const val QUIET_BORDER_ALPHA = 0.55f
}

/** [target] eased over the focus duration. */
@Composable
internal fun animateFocusColor(target: Color, label: String): State<Color> =
    animateColorAsState(target, tween(FocusDefaults.DURATION_MS), label = label)

/** [focused] as a scale between 1 and [focusedScale], eased over the focus duration. */
@Composable
internal fun animateFocusScale(focused: Boolean, focusedScale: Float, label: String): State<Float> =
    animateFloatAsState(if (focused) focusedScale else 1f, tween(FocusDefaults.DURATION_MS), label = label)
