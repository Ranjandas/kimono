package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.hypot

/**
 * An opaque canvas that paints over what's there, for scenes rather than [LineCanvas]'s lines on black. [pixels]
 * is packed ARGB in rows of [width], starting as [background] (RGB).
 */
internal class PaintCanvas(val width: Int, val height: Int, background: Int) {
    val pixels = IntArray(width * height) { OPAQUE or background }

    /** Paints [rgb] over the pixel at ([x], [y]) at [alpha], from 0 to 1. */
    fun blend(x: Int, y: Int, rgb: Int, alpha: Double) {
        if (alpha <= 0) return
        val i = y * width + x
        pixels[i] = OPAQUE or mixRgb(pixels[i], rgb, alpha.coerceAtMost(1.0))
    }

    /** Paints [rgb] at [alpha] across the whole of row [y]. */
    fun blendRow(y: Int, rgb: Int, alpha: Double) {
        if (alpha <= 0 || y !in 0 until height) return
        for (x in 0 until width) blend(x, y, rgb, alpha)
    }

    /** An anti-aliased disc. */
    fun disc(cx: Double, cy: Double, radius: Double, rgb: Int, alpha: Double = 1.0) {
        for (y in rows(cy - radius - 1, cy + radius + 1)) {
            for (x in columns(cx - radius - 1, cx + radius + 1)) {
                val inside = (radius - hypot(x + HALF - cx, y + HALF - cy) + HALF).coerceIn(0.0, 1.0)
                blend(x, y, rgb, alpha * inside)
            }
        }
    }

    /**
     * A line through [points] (x, y pairs) with round ends, [stroke] wide. Each pixel is painted once, at the
     * nearest part of the line, so joints don't double up.
     */
    fun polyline(points: List<Pair<Double, Double>>, stroke: Double, rgb: Int, alpha: Double) {
        val reach = stroke
        for (y in rows(points.minOf { it.second } - reach, points.maxOf { it.second } + reach)) {
            for (x in columns(points.minOf { it.first } - reach, points.maxOf { it.first } + reach)) {
                val distance = points.zipWithNext().minOf { (a, b) -> segmentDistance(x + HALF, y + HALF, a, b) }
                blend(x, y, rgb, alpha * coverage(distance, stroke))
            }
        }
    }

    fun rows(from: Double, to: Double): IntRange = from.toInt().coerceAtLeast(0)..to.toInt().coerceAtMost(height - 1)

    fun columns(from: Double, to: Double): IntRange = from.toInt().coerceAtLeast(0)..to.toInt().coerceAtMost(width - 1)
}

/** How far ([px], [py]) is from the segment [a] to [b]. */
internal fun segmentDistance(px: Double, py: Double, a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
    val dx = b.first - a.first
    val dy = b.second - a.second
    val lengthSquared = (dx * dx + dy * dy).coerceAtLeast(Double.MIN_VALUE)
    val t = (((px - a.first) * dx + (py - a.second) * dy) / lengthSquared).coerceIn(0.0, 1.0)
    return hypot(px - (a.first + t * dx), py - (a.second + t * dy))
}

private const val OPAQUE = 0xFF000000.toInt()
private const val HALF = 0.5
