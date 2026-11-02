package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality.Companion.Low
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import kotlin.math.roundToInt

/**
 * A home-screen tile: a surface with a border that turns to the accent colour while the tile, or anything
 * inside it, has focus. The border follows the corner style. Over a wallpaper ([LocalBackdrop]) the surface
 * is frosted glass.
 *
 * Pass [onClick] for a tile that is a single focus target (a recent app), and [onLongClick] for held OK on it
 * (the app's options). Leave [onClick] null for a tile whose children take focus (the apps list). A panel with a
 * slimmer inset, such as the top bar, passes its own [contentPadding].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tile(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(TileDefaults.PADDING_DP.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    var hasFocus by remember { mutableStateOf(false) }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.tile
    val heldOk = remember { HeldOk() }
    val clickable = onClick?.let {
        val held = onLongClick?.let { long -> Modifier.onPreviewKeyEvent { heldOk.onKey(it, long) } } ?: Modifier
        held.combinedClickable(interactionSource = null, indication = null, onLongClick = onLongClick, onClick = it)
    }
    val backdrop = LocalBackdrop.current
    val glass = remember { GlassPosition() }
    val surface = if (backdrop == null) {
        Modifier.background(colors.surface, shape)
    } else {
        Modifier.frosted(backdrop, glass, colors.surface, shape)
    }
    Column(
        modifier = modifier
            .onFocusChanged { hasFocus = it.hasFocus }
            .then(clickable ?: Modifier)
            .then(surface)
            .border(TileDefaults.BorderWidth, if (hasFocus) colors.borderFocused else colors.border, shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Where a tile sits in the window, kept as state so the glass redraws when the tile moves. */
private class GlassPosition {
    var origin by mutableStateOf(Offset.Zero)
    var window by mutableStateOf(Size.Zero)
}

/** [color] over [backdrop]'s crop under this tile, at the backdrop's opacity: frosted glass. */
private fun Modifier.frosted(backdrop: Backdrop, position: GlassPosition, color: Color, shape: Shape): Modifier = this
    .onGloballyPositioned { coordinates ->
        position.origin = coordinates.positionInRoot()
        position.window = coordinates.findRootCoordinates().size.toSize()
    }
    .drawWithCache {
        val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        val dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        onDrawBehind {
            val window = position.window
            if (window == Size.Zero) return@onDrawBehind
            val visibility = backdrop.visibility()
            val image = backdrop.blurred
            val (srcOffset, srcSize) = backdropCrop(IntSize(image.width, image.height), window, position.origin, size)
            // The tint and the tile colour in one fill, which matters on the Streamer's GPU. The tile colour is
            // solid while the wallpaper fades in and reaches the glass opacity once it's fully there.
            val tint = backdrop.tint.copy(alpha = backdrop.tint.alpha * visibility)
            val overlay = color.copy(alpha = 1f - visibility * (1f - backdrop.opacity)).compositeOver(tint)
            clipPath(outline) {
                if (visibility < 1f) {
                    // Fading in, the image is see-through, and the tile colour has to cover the gaps.
                    drawImage(image, srcOffset, srcSize, dstSize = dstSize, alpha = visibility, filterQuality = Low)
                    drawRect(overlay)
                } else {
                    // Once the wallpaper is in, the overlay goes on as a colour filter: one draw per tile (#91).
                    val filter = ColorFilter.tint(overlay, BlendMode.SrcAtop)
                    drawImage(image, srcOffset, srcSize, dstSize = dstSize, filterQuality = Low, colorFilter = filter)
                }
            }
        }
    }

object TileDefaults {
    /** Border width, focused or not, so focus changes colour without moving anything. */
    val BorderWidth = 2.dp

    /**
     * Inset between a tile's edge and its content. Anything inside with its own background or border uses
     * `KimonoTheme.shapes.nested(PADDING_DP)` so its corners stay parallel to the tile's.
     */
    const val PADDING_DP = 20
}
