package io.github.ranjandas.kimono

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.ranjandas.kimono.core.data.wallpaper.pixelArt
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
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
 * While [idle], the wallpaper turns into [art] block by block, in a fixed scattered order, over a few seconds.
 * On wake it's gone at once, like the dim.
 */
@Composable
internal fun PixelDissolve(art: ImageBitmap?, idle: Boolean, modifier: Modifier = Modifier) {
    if (art == null) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(idle) {
        if (idle) progress.animateTo(1f, tween(DISSOLVE_MS, easing = LinearEasing)) else progress.snapTo(0f)
    }
    // Each block's turn: its place in a shuffle that's the same every time, so the dissolve has a shape.
    val turns = remember {
        val order = (0 until BLOCKS_ACROSS * BLOCKS_DOWN).shuffled(Random(SHUFFLE_SEED))
        IntArray(order.size).also { turns -> order.forEachIndexed { turn, block -> turns[block] = turn } }
    }
    Box(
        modifier.fillMaxSize().drawBehind {
            val shown = (progress.value * turns.size).toInt()
            if (shown == 0) return@drawBehind
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
                drawImage(
                    art,
                    srcOffset = IntOffset(column * srcWidth, row * srcHeight),
                    srcSize = IntSize(srcWidth, srcHeight),
                    dstOffset = IntOffset(left, top),
                    dstSize = IntSize(((column + 1) * dstWidth).toInt() - left, ((row + 1) * dstHeight).toInt() - top),
                    filterQuality = FilterQuality.None,
                )
            }
        },
    )
}

// 16 × 9 blocks of 10 × 10 art pixels: square on a 16:9 screen.
private const val BLOCKS_ACROSS = 16
private const val BLOCKS_DOWN = 9
private const val DISSOLVE_MS = 6_000
private const val SHUFFLE_SEED = 9
