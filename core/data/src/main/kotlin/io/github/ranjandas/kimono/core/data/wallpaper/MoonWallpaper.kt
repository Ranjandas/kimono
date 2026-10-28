// The numbers here place the moon and waves as fractions of the screen and set their strokes and fades.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/** A thin gold moon, high on the right, over vermilion waves that fade out towards both edges. */
internal fun moonWallpaper(width: Int, height: Int): IntArray {
    val canvas = LineCanvas(width, height)
    val w = width.toDouble()
    val h = height.toDouble()
    val stroke = h / 540
    val moonX = w * 0.74
    val moonY = h * 0.30
    val radius = h * 0.17

    canvas.ring(moonX, moonY, radius, stroke * 1.5, KIN, alpha = 1.0)
    canvas.ring(moonX, moonY, radius * 1.08, stroke, KIN, alpha = 0.25)
    canvas.ring(moonX, moonY, radius * 1.18, stroke, KIN, alpha = 0.1)
    for (j in 0 until 9) {
        val baseline = h * (0.70 + j * 0.035)
        val amplitude = h * 0.012 * (1 + j * 0.15)
        val cycles = PI * (5 + j * 0.7) / w
        canvas.wave(stroke, SHU, alpha = 0.9 - j * 0.09) { x ->
            val phase = x * cycles + j * 1.3
            // Brightest a little right of centre, under the moon.
            val fade = exp(-((x / w - 0.62) / 0.33).let { it * it })
            Triple(baseline + amplitude * sin(phase), amplitude * cycles * cos(phase), fade)
        }
    }
    // The moon's reflection on the water.
    for (j in 0 until 7) {
        val half = radius * (0.9 - j * 0.11) / 2
        val y = h * (0.72 + j * 0.04)
        canvas.segment(moonX - half, y, moonX + half, y, stroke, KIN, alpha = 0.6 - j * 0.07)
    }
    return canvas.pixels
}
