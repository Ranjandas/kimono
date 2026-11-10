// The scene is laid out on a 1920 x 1080 grid and scaled to the screen; the numbers place and size its parts.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.data.wallpaper

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Yūgure, the Kimono theme's landscape, in the manner of an ukiyo-e print: a graded sky, a low sun with its glow,
 * long bands of cloud, five ridges of hills fading into haze, a sea of seigaiha waves and three cranes. [time]
 * picks the colours. Drawn here, so there's no image to license or download.
 */
internal fun yugureWallpaper(width: Int, height: Int, time: YugureTime): IntArray {
    val scene = Yugure(PaintCanvas(width, height, time.sky.sky.first()), time.sky)
    scene.sky()
    if (time.sky.night) scene.stars()
    scene.sun()
    scene.clouds()
    scene.hills()
    scene.sea()
    scene.cranes()
    return scene.canvas.pixels
}

private class Yugure(val canvas: PaintCanvas, val colours: YugureSky) {
    private val kx = canvas.width / GRID_WIDTH
    private val ky = canvas.height / GRID_HEIGHT
    private val sunX = 1450 * kx
    private val sunY = (if (colours.night) 300 else 520) * ky
    private val sunRadius = (if (colours.night) 62 else 96) * ky

    /** The sky graded down to the horizon, dithered so the gradient doesn't band, under a darker top edge. */
    fun sky() {
        val stops = doubleArrayOf(0.0, 0.28, 0.58, 0.84, 1.0)
        for (y in 0 until canvas.height) {
            val t = ((y + 0.5) / (720 * ky)).coerceIn(0.0, 1.0)
            val segment = (1 until stops.size).first { t <= stops[it] }
            val local = (t - stops[segment - 1]) / (stops[segment] - stops[segment - 1])
            val rgb = mixRgb(colours.sky[segment - 1], colours.sky[segment], local)
            val shade = 0.55 * (1 - (y + 0.5) / (170 * ky)).coerceAtLeast(0.0)
            for (x in 0 until canvas.width) {
                val dithered = mixRgb(rgb, if (dither(x, y) > 0) WHITE else BLACK, abs(dither(x, y)) / 255)
                canvas.pixels[y * canvas.width + x] = OPAQUE or mixRgb(dithered, TOP_SHADE, shade)
            }
        }
    }

    fun stars() {
        val random = Random(STAR_SEED)
        repeat(140) {
            val x = (random.nextDouble() * canvas.width).toInt()
            val y = (random.nextDouble() * 520 * ky).toInt()
            val alpha = 0.15 + random.nextDouble() * 0.5
            for (dy in 0..1) {
                for (dx in 0..1) {
                    if (x + dx < canvas.width && y + dy < canvas.height) canvas.blend(x + dx, y + dy, STAR, alpha)
                }
            }
        }
    }

    /** A soft glow spreading five radii out, then the disc itself. */
    fun sun() {
        val inner = sunRadius * 0.6
        val outer = sunRadius * 5
        for (y in canvas.rows(sunY - outer, sunY + outer)) {
            for (x in canvas.columns(sunX - outer, sunX + outer)) {
                val t = ((hypot(x + 0.5 - sunX, y + 0.5 - sunY) - inner) / (outer - inner)).coerceIn(0.0, 1.0)
                canvas.blend(x, y, colours.glow, 0.45 * (1 - t))
            }
        }
        canvas.disc(sunX, sunY, sunRadius, colours.sun)
    }

    /** Kasumi: long rounded bands of cloud that fade in and out along their length. */
    fun clouds() {
        for (band in CLOUD_BANDS) {
            val x0 = band.left * kx
            val w = band.length * kx
            val r = band.thickness * ky / 2
            val cy = band.top * ky + r
            for (y in canvas.rows(cy - r - 1, cy + r + 1)) {
                for (x in canvas.columns(x0, x0 + w)) {
                    val along = (x + 0.5 - x0) / w
                    val fade = minOf(1.0, along / 0.25, (1 - along) / 0.25).coerceAtLeast(0.0)
                    val distance = segmentDistance(x + 0.5, y + 0.5, (x0 + r) to cy, (x0 + w - r) to cy)
                    canvas.blend(x, y, colours.cloud, 0.22 * fade * (r - distance + 0.5).coerceIn(0.0, 1.0))
                }
            }
        }
    }

    /** Five ridges, far to near, each with mist rising at its foot. */
    fun hills() {
        HILL_BASES.forEachIndexed { layer, base ->
            val random = Random(HILL_SEED + layer * 13)
            val amplitudes = doubleArrayOf(70.0 - layer * 6, 34.0, 16.0, 7.0)
            val frequencies = doubleArrayOf(
                0.0021 + random.nextDouble() * 0.001,
                0.0058 + random.nextDouble() * 0.002,
                0.013 + random.nextDouble() * 0.004,
                0.03,
            )
            val phases = DoubleArray(4) { random.nextDouble() * 2 * PI }
            for (x in 0 until canvas.width) {
                val gx = (x + 0.5) / kx
                var ridge = base - amplitudes.indices.sumOf { amplitudes[it] * sin(gx * frequencies[it] + phases[it]) }
                ridge -= peak(layer, gx)
                val ridgeY = ridge * ky
                for (y in canvas.rows(ridgeY - 1, canvas.height.toDouble())) {
                    canvas.blend(x, y, colours.hills[layer], (y + 1 - ridgeY).coerceIn(0.0, 1.0))
                }
            }
            mist(base, 0.16 - layer * 0.025)
        }
    }

    /** One taller peak on the left, and a dip in the farthest ridge where the sun sets. */
    private fun peak(layer: Int, gx: Double): Double = when (layer) {
        0 -> -50 * exp(-((gx - 1450) / 220).pow(2))
        1 -> 150 * exp(-((gx - 520) / 230).pow(2))
        else -> 0.0
    }

    private fun mist(base: Double, strength: Double) {
        for (y in canvas.rows((base - 40) * ky, (base + 70) * ky)) {
            val t = ((y + 0.5) / ky - (base - 40)) / 110
            val alpha = if (t < 0.6) t / 0.6 else (1 - t) / 0.4
            canvas.blendRow(y, colours.mist, strength * alpha.coerceIn(0.0, 1.0))
        }
    }

    /** The sea along the bottom, in rows of seigaiha scales, with the sun's path on it. */
    fun sea() {
        val shore = SHORE * ky
        for (y in canvas.rows(shore, canvas.height.toDouble())) {
            for (x in 0 until canvas.width) canvas.blend(x, y, colours.water, 1.0)
        }
        val radius = 34 * ky
        for (row in 0 until 5) {
            val cy = shore + row * radius * 0.55 + radius * 0.9
            var cx = -radius + if (row % 2 == 1) radius else 0.0
            while (cx < canvas.width + radius) {
                scale(cx, cy, radius)
                cx += radius * 2
            }
        }
        val pathAlpha = colours.sunPath
        for (i in 0 until 6) {
            val half = (90 - i * 12) * kx / 2
            val top = (990 + i * 15) * ky
            for (y in canvas.rows(top, top + 3 * ky - 1)) {
                for (x in canvas.columns(sunX - half, sunX + half)) canvas.blend(x, y, colours.sun, pathAlpha)
            }
        }
    }

    /** One scale: an upper half-disc of sea that covers the scales behind it, with three nested arcs. */
    private fun scale(cx: Double, cy: Double, radius: Double) {
        val stroke = 2.2 * ky
        for (y in canvas.rows(cy - radius - 1, cy)) {
            for (x in canvas.columns(cx - radius - 1, cx + radius + 1)) {
                val distance = hypot(x + 0.5 - cx, y + 0.5 - cy)
                canvas.blend(x, y, colours.water, (radius - distance + 0.5).coerceIn(0.0, 1.0))
                for (k in ARC_RADII) {
                    canvas.blend(x, y, colours.wave, coverage(abs(distance - radius * k), stroke))
                }
            }
        }
    }

    /** Three cranes in flight, each two shallow curved strokes. */
    fun cranes() {
        for ((x, y, size) in CRANES) {
            val cx = x * kx
            val cy = y * ky
            val s = size * ky
            val left = quadratic(cx - 26 * s to cy - 8 * s, cx - 10 * s to cy - 14 * s, cx to cy)
            val right = quadratic(cx to cy, cx + 10 * s to cy - 16 * s, cx + 30 * s to cy - 12 * s)
            canvas.polyline(left + right.drop(1), 3.2 * ky, colours.cranes, 0.9)
        }
    }
}

/** Points along the quadratic curve from [a] to [c] that bends towards [b]. */
private fun quadratic(
    a: Pair<Double, Double>,
    b: Pair<Double, Double>,
    c: Pair<Double, Double>,
): List<Pair<Double, Double>> = (0..CURVE_STEPS).map { i ->
    val t = i.toDouble() / CURVE_STEPS
    val u = 1 - t
    (u * u * a.first + 2 * u * t * b.first + t * t * c.first) to
        (u * u * a.second + 2 * u * t * b.second + t * t * c.second)
}

/** A tiny, fixed per-pixel offset from -0.5 to 0.5 of a step, which breaks up banding in smooth gradients. */
private fun dither(x: Int, y: Int): Double = (((x * 73_856_093) xor (y * 19_349_663)) and 0xFF) / 255.0 - 0.5

private const val GRID_WIDTH = 1920.0
private const val GRID_HEIGHT = 1080.0
private const val SHORE = 975.0
private const val CURVE_STEPS = 10
private const val STAR_SEED = 7
private const val HILL_SEED = 31
private const val OPAQUE = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFF
private const val BLACK = 0x000000
private const val STAR = 0xE6E4DC
private const val TOP_SHADE = 0x040610
private val HILL_BASES = doubleArrayOf(600.0, 680.0, 760.0, 850.0, 935.0)
private val ARC_RADII = doubleArrayOf(1.0, 0.72, 0.44)

private class CloudBand(val left: Double, val top: Double, val length: Double, val thickness: Double)

private val CLOUD_BANDS = listOf(
    CloudBand(1040.0, 420.0, 760.0, 26.0),
    CloudBand(1180.0, 470.0, 620.0, 18.0),
    CloudBand(140.0, 360.0, 680.0, 22.0),
    CloudBand(300.0, 300.0, 420.0, 14.0),
    CloudBand(1500.0, 360.0, 360.0, 14.0),
)

// x, y, size
private val CRANES = listOf(listOf(1210.0, 250.0, 1.0), listOf(1290.0, 215.0, 0.8), listOf(1150.0, 205.0, 0.65))
