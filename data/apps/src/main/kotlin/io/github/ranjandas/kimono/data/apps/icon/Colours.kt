package io.github.ranjandas.kimono.data.apps.icon

import kotlin.math.sqrt

// Channels of an 0xAARRGGBB pixel, 0 to 255.
internal fun red(pixel: Int) = (pixel shr RED_SHIFT) and CHANNEL

internal fun green(pixel: Int) = (pixel shr GREEN_SHIFT) and CHANNEL

internal fun blue(pixel: Int) = pixel and CHANNEL

/** How far apart two colours are, from 0 for the same to 1 for black and white. */
internal fun distance(a: Int, b: Int): Float {
    val dr = red(a) - red(b)
    val dg = green(a) - green(b)
    val db = blue(a) - blue(b)
    return sqrt((dr * dr + dg * dg + db * db).toFloat()) / MAX_DISTANCE
}

internal const val RED_SHIFT = 16
internal const val GREEN_SHIFT = 8
private const val CHANNEL = 0xFF
private val MAX_DISTANCE = 255f * sqrt(3f)
