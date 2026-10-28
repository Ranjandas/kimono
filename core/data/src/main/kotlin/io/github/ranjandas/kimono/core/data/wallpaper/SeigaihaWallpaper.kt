// The numbers here size the scales against the screen and set where the pattern fades out.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Seigaiha: rows of overlapping scales, each four nested arcs, rising from the bottom right and fading into black
 * towards the top left. Now and then a scale's outer arc is gold instead of vermilion.
 */
internal fun seigaihaWallpaper(width: Int, height: Int): IntArray {
    val canvas = LineCanvas(width, height)
    val pattern = Seigaiha(width.toDouble(), height.toDouble())
    for (y in 0 until height) {
        for (x in 0 until width) pattern.shade(canvas, x, y)
    }
    return canvas.pixels
}

private class Seigaiha(private val w: Double, h: Double) {
    private val radius = h * 0.065
    private val stroke = h / 540
    private val glowX = w
    private val glowY = h * 1.05

    /** Lights the pixel at ([x], [y]) where an arc of the scale in front crosses it. */
    fun shade(canvas: LineCanvas, x: Int, y: Int) {
        val scale = frontScale(x + 0.5, y + 0.5, radius) ?: return
        val fade = 1 - hypot(scale.x - glowX, scale.y - glowY) / (w * 0.75)
        if (fade <= 0) return
        val distance = hypot(x + 0.5 - scale.x, y + 0.5 - scale.y)
        for (arc in 0 until ARCS) {
            val cover = coverage(abs(distance - radius * (1 - arc.toDouble() / ARCS)), stroke)
            val outer = arc == 0
            val colour = if (outer && Math.floorMod(scale.column * 3 + scale.row * 5, 7) == 0) KIN else SHU
            canvas.plot(x, y, colour, fade.pow(1.6) * (if (outer) 0.9 else 0.55) * cover)
        }
    }
}

private class Scale(val row: Int, val column: Int, val x: Double, val y: Double)

/**
 * The scale in front at ([px], [py]): scales are upper half-discs, rows half a radius apart with alternate rows
 * shifted by a radius, and each row overlaps the one above it. Null where none covers the point.
 */
private fun frontScale(px: Double, py: Double, radius: Double): Scale? {
    val rowStep = radius / 2
    for (row in floor((py + radius) / rowStep).toInt() downTo ceil(py / rowStep).toInt()) {
        val cy = row * rowStep
        val shift = if (Math.floorMod(row, 2) == 1) radius else 0.0
        val column = ((px - shift) / (2 * radius)).roundToInt()
        val cx = column * 2 * radius + shift
        if (py <= cy && hypot(px - cx, py - cy) <= radius) return Scale(row, column, cx, cy)
    }
    return null
}

private const val ARCS = 4
