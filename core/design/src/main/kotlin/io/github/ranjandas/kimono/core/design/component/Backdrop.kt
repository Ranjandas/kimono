package io.github.ranjandas.kimono.core.design.component

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The wallpaper behind the screens, as frosted tiles see it: a [blurred] copy, laid out the way the wallpaper
 * fills the window (centred and cropped), under the same [tint]. A tile draws its own crop of it and then its
 * colour at [opacity], so the glass costs a bitmap draw per tile instead of a blur per frame.
 *
 * [visibility] goes from 0 to 1 as the wallpaper fades in. It's read while drawing, so the fade doesn't
 * recompose anything.
 */
@Stable
class Backdrop(val blurred: ImageBitmap, val tint: Color, val opacity: Float, val visibility: () -> Float)

/** The backdrop tiles frost over, or null for plain tiles. */
val LocalBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** The part of a [image]-sized backdrop, cropped to fill [window], that lies under a panel at [origin]. */
internal fun backdropCrop(image: IntSize, window: Size, origin: Offset, panel: Size): Pair<IntOffset, IntSize> {
    val scale = max(window.width / image.width, window.height / image.height)
    val left = (window.width - image.width * scale) / 2
    val top = (window.height - image.height * scale) / 2
    val x = ((origin.x - left) / scale).roundToInt().coerceIn(0, image.width - 1)
    val y = ((origin.y - top) / scale).roundToInt().coerceIn(0, image.height - 1)
    val width = (panel.width / scale).roundToInt().coerceIn(1, image.width - x)
    val height = (panel.height / scale).roundToInt().coerceIn(1, image.height - y)
    return IntOffset(x, y) to IntSize(width, height)
}
