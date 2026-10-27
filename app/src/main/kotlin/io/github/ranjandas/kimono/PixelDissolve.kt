package io.github.ranjandas.kimono

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.ranjandas.kimono.core.data.wallpaper.pixelArt
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** [wallpaper] as pixel art in [theme]'s colours, once it's made; null without a wallpaper. */
@Composable
internal fun rememberPixelArt(wallpaper: WallpaperImages?, theme: ThemeId): ImageBitmap? {
    val art by produceState<ImageBitmap?>(initialValue = null, wallpaper, theme) {
        value = wallpaper?.let {
            withContext(Dispatchers.Default) {
                pixelArt(it.image.asAndroidBitmap(), theme.paletteColours()).asImageBitmap()
            }
        }
    }
    return art
}

/** Every colour of the theme, so the pixel art looks like it belongs to it. */
private fun ThemeId.paletteColours(): IntArray = with(palette) {
    listOf(
        background, surface, surfaceVariant, foreground, mutedForeground, line,
        accent, accent2, red, yellow, green, cyan, blue, magenta,
    ).map { it.value }.toIntArray()
}

/**
 * While [idle], the wallpaper turns into [art] block by block, in a scattered order, over a few seconds. So that
 * no pixel shows the same thing for long on an OLED panel, the art then cycles: it holds for a while, breaks
 * down to black, and builds up again a little to one side, in a new order. On wake it's gone at once.
 */
@Composable
internal fun PixelDissolve(art: ImageBitmap?, idle: Boolean, modifier: Modifier = Modifier) {
    if (art == null) return
    val progress = remember { Animatable(0f) }
    var cycle by remember { mutableIntStateOf(0) }
    // Black under the blocks once the art has covered the wallpaper, so breaking down shows black, not the photo.
    var overBlack by remember { mutableStateOf(false) }
    LaunchedEffect(idle) {
        progress.snapTo(0f)
        cycle = 0
        overBlack = false
        while (idle) {
            progress.animateTo(1f, tween(DISSOLVE_MS, easing = LinearEasing))
            overBlack = true
            delay(HOLD_MS)
            progress.animateTo(0f, tween(DISSOLVE_MS, easing = LinearEasing))
            delay(REST_MS)
            cycle++
        }
    }
    // Each block's turn: its place in a shuffle, new each cycle, so the dissolve doesn't wear a pattern in either.
    val turns = remember(cycle) {
        val order = (0 until BLOCKS_ACROSS * BLOCKS_DOWN).shuffled(Random(SHUFFLE_SEED + cycle))
        IntArray(order.size).also { turns -> order.forEachIndexed { turn, block -> turns[block] = turn } }
    }
    val nudge = Nudges[cycle % Nudges.size]
    Box(
        modifier.fillMaxSize().drawBehind {
            // A little past the edges, so pixel shift doesn't uncover a sliver of photo.
            if (overBlack) {
                drawRect(
                    Color.Black,
                    Offset(-BLACK_BLEED, -BLACK_BLEED),
                    Size(size.width + 2 * BLACK_BLEED, size.height + 2 * BLACK_BLEED),
                )
            }
            drawBlocks(art, turns, shown = (progress.value * turns.size).toInt(), nudge)
        },
    )
}

/** The blocks whose turn is before [shown], with the art moved by [nudge] art pixels. */
private fun DrawScope.drawBlocks(art: ImageBitmap, turns: IntArray, shown: Int, nudge: IntOffset) {
    val srcWidth = art.width / BLOCKS_ACROSS
    val srcHeight = art.height / BLOCKS_DOWN
    val dstWidth = size.width / BLOCKS_ACROSS
    val dstHeight = size.height / BLOCKS_DOWN
    for (block in turns.indices) {
        if (turns[block] >= shown) continue
        val column = block % BLOCKS_ACROSS
        val row = block / BLOCKS_ACROSS
        // Rounded edges on both sides, so neighbouring blocks meet without gaps.
        val left = (column * dstWidth).toInt()
        val top = (row * dstHeight).toInt()
        // Blocks at the edge can't move past the art, so they repeat a sliver; nobody looks at the edge.
        val srcX = (column * srcWidth - nudge.x).coerceIn(0, art.width - srcWidth)
        val srcY = (row * srcHeight - nudge.y).coerceIn(0, art.height - srcHeight)
        drawImage(
            art,
            srcOffset = IntOffset(srcX, srcY),
            srcSize = IntSize(srcWidth, srcHeight),
            dstOffset = IntOffset(left, top),
            dstSize = IntSize(((column + 1) * dstWidth).toInt() - left, ((row + 1) * dstHeight).toInt() - top),
            filterQuality = FilterQuality.None,
        )
    }
}

// Where the art sits each cycle, in art pixels (12 screen pixels each on a 1080p TV).
private val Nudges = listOf(IntOffset(0, 0), IntOffset(2, 1), IntOffset(-1, 2), IntOffset(-2, -1), IntOffset(1, -2))

// 16 × 9 blocks of 10 × 10 art pixels: square on a 16:9 screen.
private const val BLOCKS_ACROSS = 16
private const val BLOCKS_DOWN = 9
private const val DISSOLVE_MS = 6_000
private const val HOLD_MS = 120_000L
private const val REST_MS = 5_000L
private const val SHUFFLE_SEED = 9
private const val BLACK_BLEED = 32f
