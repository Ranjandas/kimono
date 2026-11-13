package io.github.ranjandas.kimono.data.apps.icon

import kotlin.math.roundToInt

/**
 * Cuts a single-colour glyph out of a colour image, such as an app icon or a TV banner, so it can be tinted.
 *
 * When most of the image is one opaque colour, that colour is the plate the logo sits on (YouTube's red square,
 * Netflix's black one), and the glyph is whatever contrasts with it. Otherwise the image's own transparency
 * already outlines the logo, and the glyph is its alpha, with thin gaps cut where two colours meet, so a
 * many-coloured logo like Google Play's keeps its segments instead of becoming one solid shape.
 */
internal object GlyphMask {
    /**
     * The glyph's alpha, 0 to 255, for each pixel of [argb], an image [width] wide, with [plate] taken out when
     * there is one.
     */
    fun extract(argb: IntArray, width: Int, plate: Int? = plateColour(argb), cutWidth: Int = CUT_WIDTH): IntArray =
        if (plate == null) {
            IntArray(argb.size) { i -> if (meetsOtherColour(argb, width, i, cutWidth)) 0 else argb[i] ushr ALPHA_SHIFT }
        } else {
            IntArray(argb.size) { i -> ((argb[i] ushr ALPHA_SHIFT) * contrast(argb[i], plate)).roundToInt() }
        }

    // True when an opaque pixel a little to the right or below is a clearly different opaque colour. Looking only
    // that way puts each gap on one side of the boundary, cutWidth pixels wide. Smooth gradients never trigger it.
    private fun meetsOtherColour(argb: IntArray, width: Int, i: Int, cutWidth: Int): Boolean {
        val pixel = argb[i]
        if (pixel ushr ALPHA_SHIFT < OPAQUE_ALPHA) return false
        val x = i % width
        return (1..cutWidth).any { step ->
            val right = if (x + step < width) argb[i + step] else 0
            val below = argb.getOrElse(i + step * width) { 0 }
            differs(pixel, right) || differs(pixel, below)
        }
    }

    private fun differs(pixel: Int, other: Int): Boolean =
        other ushr ALPHA_SHIFT >= OPAQUE_ALPHA && distance(pixel, other) > CUT_DISTANCE

    /** The plate colour as 0xRRGGBB, or null when no single colour covers most of the image. */
    fun plateColour(argb: IntArray): Int? {
        // A plain array rather than a map, since a banner has a quarter of a million pixels to count.
        val counts = IntArray(BUCKET_COUNT)
        var opaque = 0
        for (pixel in argb) {
            if (pixel ushr ALPHA_SHIFT < OPAQUE_ALPHA) continue
            opaque++
            counts[bucket(pixel)]++
        }
        val top = counts.indices.maxBy { counts[it] }
        val isPlate = opaque > 0 && opaque >= argb.size * MIN_PLATE_COVER && counts[top] >= opaque * MIN_PLATE_SHARE
        return if (isPlate) meanColour(argb, top) else null
    }

    /** The smallest box holding every pixel of [alpha], an image [width] wide, above [threshold]. */
    fun bounds(alpha: IntArray, width: Int, threshold: Int = INK_THRESHOLD): GlyphBounds? {
        var left = width
        var top = Int.MAX_VALUE
        var right = -1
        var bottom = -1
        alpha.forEachIndexed { i, a ->
            if (a > threshold) {
                val x = i % width
                val y = i / width
                left = minOf(left, x)
                right = maxOf(right, x)
                top = minOf(top, y)
                bottom = maxOf(bottom, y)
            }
        }
        return if (right < 0) null else GlyphBounds(left, top, right + 1, bottom + 1)
    }

    // 0 for the plate's own colour, rising to 1 for anything clearly different, with a soft edge between so
    // anti-aliased outlines stay smooth.
    private fun contrast(pixel: Int, plate: Int): Float =
        ((distance(pixel, plate) - CONTRAST_LOW) / (CONTRAST_HIGH - CONTRAST_LOW)).coerceIn(0f, 1f)

    private fun meanColour(argb: IntArray, key: Int): Int {
        var r = 0L
        var g = 0L
        var b = 0L
        var n = 0
        for (pixel in argb) {
            if (pixel ushr ALPHA_SHIFT < OPAQUE_ALPHA || bucket(pixel) != key) continue
            r += red(pixel)
            g += green(pixel)
            b += blue(pixel)
            n++
        }
        return ((r / n).toInt() shl RED_SHIFT) or ((g / n).toInt() shl GREEN_SHIFT) or (b / n).toInt()
    }

    // Three bits per channel, so a plate with slight noise or dithering still counts as one colour.
    private fun bucket(pixel: Int): Int = ((red(pixel) shr BUCKET_DROP) shl (2 * BUCKET_BITS)) or
        ((green(pixel) shr BUCKET_DROP) shl BUCKET_BITS) or
        (blue(pixel) shr BUCKET_DROP)

    private const val ALPHA_SHIFT = 24
    private const val BUCKET_BITS = 3
    private const val BUCKET_DROP = 8 - BUCKET_BITS
    private const val BUCKET_COUNT = 1 shl (3 * BUCKET_BITS)
    private const val OPAQUE_ALPHA = 200
    private const val INK_THRESHOLD = 16

    // A plate covers at least half the image, and one colour makes up at least 40% of its opaque pixels.
    private const val MIN_PLATE_COVER = 0.5f
    private const val MIN_PLATE_SHARE = 0.4f
    private const val CONTRAST_LOW = 0.08f
    private const val CONTRAST_HIGH = 0.25f

    // Two colour areas of a logo, such as Google Play's blue and green, not two shades of one gradient.
    private const val CUT_DISTANCE = 0.3f

    /** How wide a gap is cut where two colours meet, in pixels, for a 288 px adaptive layer or a 192 px icon. */
    const val CUT_WIDTH = 3
}

/** A box in pixels: [left] and [top] inclusive, [right] and [bottom] exclusive. */
internal data class GlyphBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width get() = right - left
    val height get() = bottom - top
}
