package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Anti-aliased lines on true black, for the wallpapers Kimono draws itself. Overlapping lines keep the brighter of
 * each channel, and pixels no line touches stay exactly black. [pixels] is packed ARGB in rows of [width].
 */
internal class LineCanvas(val width: Int, val height: Int) {
    val pixels = IntArray(width * height) { BLACK }

    @Suppress("LongParameterList")
    fun ring(cx: Double, cy: Double, radius: Double, stroke: Double, colour: Int, alpha: Double) {
        val reach = radius + stroke
        for (y in rows(cy - reach, cy + reach)) {
            for (x in columns(cx - reach, cx + reach)) {
                val distance = abs(hypot(x + HALF - cx, y + HALF - cy) - radius)
                plot(x, y, colour, alpha * coverage(distance, stroke))
            }
        }
    }

    /** A straight line from (x0, y0) to (x1, y1). */
    @Suppress("LongParameterList")
    fun segment(x0: Double, y0: Double, x1: Double, y1: Double, stroke: Double, colour: Int, alpha: Double) {
        val dx = x1 - x0
        val dy = y1 - y0
        val lengthSquared = (dx * dx + dy * dy).coerceAtLeast(Double.MIN_VALUE)
        for (y in rows(minOf(y0, y1) - stroke, maxOf(y0, y1) + stroke)) {
            for (x in columns(minOf(x0, x1) - stroke, maxOf(x0, x1) + stroke)) {
                val px = x + HALF
                val py = y + HALF
                val t = (((px - x0) * dx + (py - y0) * dy) / lengthSquared).coerceIn(0.0, 1.0)
                val distance = hypot(px - (x0 + t * dx), py - (y0 + t * dy))
                plot(x, y, colour, alpha * coverage(distance, stroke))
            }
        }
    }

    /** A curve across the whole screen; [at] gives its y, slope and extra fade at each x. */
    fun wave(stroke: Double, colour: Int, alpha: Double, at: (Double) -> Triple<Double, Double, Double>) {
        for (x in 0 until width) {
            val (curveY, slope, fade) = at(x + HALF)
            val stretch = sqrt(1 + slope * slope)
            for (y in rows(curveY - stroke * stretch, curveY + stroke * stretch)) {
                val distance = abs(y + HALF - curveY) / stretch
                plot(x, y, colour, alpha * fade * coverage(distance, stroke))
            }
        }
    }

    fun rows(from: Double, to: Double): IntRange = from.toInt().coerceAtLeast(0)..to.toInt().coerceAtMost(height - 1)

    fun columns(from: Double, to: Double): IntRange = from.toInt().coerceAtLeast(0)..to.toInt().coerceAtMost(width - 1)

    /** Lights the pixel at ([x], [y]) with [colour] (RGB) at [alpha], unless it's already brighter. */
    fun plot(x: Int, y: Int, colour: Int, alpha: Double) {
        if (alpha <= 0) return
        val i = y * width + x
        var pixel = BLACK
        for (shift in CHANNEL_SHIFTS) {
            val drawn = (((colour shr shift) and BYTE) * alpha.coerceAtMost(1.0)).roundToInt()
            pixel = pixel or (maxOf(drawn, (pixels[i] shr shift) and BYTE) shl shift)
        }
        pixels[i] = pixel
    }
}

/** How much of a pixel whose centre is [distance] from a line's middle that line covers, from 0 to 1. */
internal fun coverage(distance: Double, stroke: Double): Double = (stroke / 2 + HALF - distance).coerceIn(0.0, 1.0)

/** Mixes RGB [a] towards [b] by [fraction]. */
internal fun mixRgb(a: Int, b: Int, fraction: Double): Int {
    var rgb = 0
    for (shift in CHANNEL_SHIFTS) {
        val from = (a shr shift) and BYTE
        val to = (b shr shift) and BYTE
        rgb = rgb or ((from + (to - from) * fraction).roundToInt() shl shift)
    }
    return rgb
}

private const val BLACK = 0xFF000000.toInt()
private const val BYTE = 0xFF
private const val HALF = 0.5
private val CHANNEL_SHIFTS = intArrayOf(16, 8, 0)
