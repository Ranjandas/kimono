package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * Pixel art from [pixels], packed RGB in rows of [width]: each pixel snaps to the nearest [palette] colour
 * after a 4×4 Bayer offset, so gradients become an ordered dither instead of flat bands. The result is opaque.
 */
fun pixelate(pixels: IntArray, width: Int, palette: IntArray): IntArray {
    require(palette.isNotEmpty()) { "palette is empty" }
    return IntArray(pixels.size) { i ->
        val threshold = BAYER[(i / width % BAYER_SIZE) * BAYER_SIZE + i % width % BAYER_SIZE]
        // From about -SPREAD/2 to +SPREAD/2, centred on zero so the image's overall brightness doesn't move.
        val offset = ((threshold * 2 + 1) * SPREAD) / (BAYER_LEVELS * 2) - SPREAD / 2
        OPAQUE or nearest(pixels[i], offset, palette)
    }
}

private fun nearest(pixel: Int, offset: Int, palette: IntArray): Int {
    val red = (pixel shr RED_SHIFT and CHANNEL_MASK) + offset
    val green = (pixel shr GREEN_SHIFT and CHANNEL_MASK) + offset
    val blue = (pixel and CHANNEL_MASK) + offset
    return palette.minBy { colour ->
        val dr = red - (colour shr RED_SHIFT and CHANNEL_MASK)
        val dg = green - (colour shr GREEN_SHIFT and CHANNEL_MASK)
        val db = blue - (colour and CHANNEL_MASK)
        dr * dr + dg * dg + db * db
    } and RGB_MASK
}

private const val BAYER_SIZE = 4
private const val BAYER_LEVELS = 16

// How far the dither nudges a channel, in 0–255 steps: enough to mix neighbouring palette colours.
private const val SPREAD = 64
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MASK = 0xFF
private const val RGB_MASK = 0xFFFFFF
private const val OPAQUE = 0xFF shl 24

// The 4×4 ordered-dither matrix, in rows.
private val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
