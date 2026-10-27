package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
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
 * Pass [onClick] for a tile that is a single focus target (a recent app). Leave it null for a tile whose
 * children take focus (the apps list).
 */
@Composable
fun Tile(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    var hasFocus by remember { mutableStateOf(false) }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.tile
    val clickable = onClick?.let { Modifier.clickable(interactionSource = null, indication = null, onClick = it) }
    val backdrop = LocalBackdrop.current
    val glass = remember { GlassPosition() }
    val surface = if (backdrop == null) {
        Modifier.background(colors.surface, shape)
    } else {
        glass.modifier(backdrop, colors.surface, shape)
    }
    Column(
        modifier = modifier
            .onFocusChanged { hasFocus = it.hasFocus }
            .then(clickable ?: Modifier)
            .then(surface)
            .border(TileDefaults.BorderWidth, if (hasFocus) colors.borderFocused else colors.border, shape)
            .padding(TileDefaults.PADDING_DP.dp),
        content = content,
    )
}

/** Where a tile sits in the window, kept as state so the glass redraws when the tile moves. */
private class GlassPosition {
    var origin by mutableStateOf(Offset.Zero)
    var window by mutableStateOf(Size.Zero)

    fun modifier(backdrop: Backdrop, color: Color, shape: Shape): Modifier = Modifier
        .onGloballyPositioned { coordinates ->
            origin = coordinates.positionInRoot()
            window = coordinates.findRootCoordinates().size.toSize()
        }
        .drawBehind {
            if (window == Size.Zero) return@drawBehind
            val visibility = backdrop.visibility()
            val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawBehind)) }
            clipPath(outline) {
                val image = backdrop.blurred
                val (srcOffset, srcSize) = backdropCrop(IntSize(image.width, image.height), window, origin, size)
                drawImage(
                    image,
                    srcOffset,
                    srcSize,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    alpha = visibility,
                    filterQuality = FilterQuality.Low,
                )
                drawRect(backdrop.tint, alpha = visibility)
                // Solid while the wallpaper fades in, reaching the glass opacity once it's fully there.
                drawRect(color, alpha = 1f - visibility * (1f - backdrop.opacity))
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
