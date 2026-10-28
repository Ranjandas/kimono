// The numbers here size the lattice against the screen and set where the pattern glows and fades.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Asanoha: a lattice of triangles, each split into three from its centre, which together make six-pointed hemp
 * leaves. It glows vermilion from the bottom left, turns gold further out and fades into black on the right.
 */
internal fun asanohaWallpaper(width: Int, height: Int): IntArray {
    val canvas = LineCanvas(width, height)
    val w = width.toDouble()
    val h = height.toDouble()
    val side = h / 9
    val rowHeight = side * sqrt(3.0) / 2
    val stroke = h / 540
    val glowX = w * 0.15
    val glowY = h * 0.85
    for (row in -1..(h / rowHeight).toInt() + 1) {
        for (column in -2..(2 * w / side).toInt() + 2) {
            val left = column * side / 2
            val top = row * rowHeight
            val corners = if (Math.floorMod(column + row, 2) == 0) {
                listOf(left to top + rowHeight, left + side / 2 to top, left + side to top + rowHeight)
            } else {
                listOf(left to top, left + side to top, left + side / 2 to top + rowHeight)
            }
            val cx = corners.sumOf { it.first } / 3
            val cy = corners.sumOf { it.second } / 3
            val reach = hypot(cx - glowX, cy - glowY)
            val fade = (1 - reach / (w * 0.7)).coerceAtLeast(0.0).pow(1.8)
            if (fade < 0.01) continue
            // Vermilion near the glow, blending to gold further out.
            val colour = mixRgb(SHU, KIN, ((reach - w * 0.17) / (w * 0.1)).coerceIn(0.0, 1.0))
            corners.forEachIndexed { i, (x, y) ->
                val (nextX, nextY) = corners[(i + 1) % corners.size]
                canvas.segment(x, y, nextX, nextY, stroke, colour, fade * 0.5)
                canvas.segment(x, y, cx, cy, stroke, colour, fade * 0.9)
            }
        }
    }
    return canvas.pixels
}
