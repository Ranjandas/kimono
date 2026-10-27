package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * Blurs [pixels], packed ARGB in rows of [width], with [passes] box blurs of [radius] across and down. Three
 * passes come close to a Gaussian. Edges repeat their outermost pixel, so they don't darken.
 */
fun boxBlur(pixels: IntArray, width: Int, height: Int, radius: Int, passes: Int = 3): IntArray {
    val current = pixels.copyOf()
    val scratch = IntArray(pixels.size)
    repeat(passes) {
        blurLines(current, scratch, width, height, radius, across = true)
        blurLines(scratch, current, width, height, radius, across = false)
    }
    return current
}

/** One pass along every row ([across]) or every column, from [source] into [target]. */
@Suppress("LongParameterList")
private fun blurLines(source: IntArray, target: IntArray, width: Int, height: Int, radius: Int, across: Boolean) {
    val lines = if (across) height else width
    val length = if (across) width else height
    val window = 2 * radius + 1
    val sums = IntArray(CHANNELS)
    for (line in 0 until lines) {
        fun index(i: Int): Int {
            val clamped = i.coerceIn(0, length - 1)
            return if (across) line * width + clamped else clamped * width + line
        }
        sums.fill(0)
        for (i in -radius..radius) addPixel(sums, source[index(i)], 1)
        for (i in 0 until length) {
            var pixel = 0
            for (c in 0 until CHANNELS) pixel = pixel or (sums[c] / window shl SHIFTS[c])
            target[index(i)] = pixel
            addPixel(sums, source[index(i + radius + 1)], 1)
            addPixel(sums, source[index(i - radius)], -1)
        }
    }
}

private fun addPixel(sums: IntArray, pixel: Int, sign: Int) {
    for (c in 0 until CHANNELS) sums[c] += sign * (pixel ushr SHIFTS[c] and CHANNEL_MASK)
}

private const val CHANNELS = 4
private const val CHANNEL_MASK = 0xFF

// Alpha, red, green, blue.
private val SHIFTS = intArrayOf(24, 16, 8, 0)
