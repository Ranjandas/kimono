package io.github.ranjandas.kimono.core.data.wallpaper

import android.graphics.Bitmap
import androidx.core.graphics.scale

/**
 * [image] as pixel art in [palette]'s colours, [PIXEL_ART_WIDTH] pixels across, for idle mode to draw scaled up
 * without smoothing. A few milliseconds' work, so it's made per theme rather than cached.
 */
fun pixelArt(image: Bitmap, palette: IntArray): Bitmap {
    val height = image.height * PIXEL_ART_WIDTH / image.width
    val small = image.scale(PIXEL_ART_WIDTH, height)
    val pixels = IntArray(PIXEL_ART_WIDTH * height)
    small.getPixels(pixels, 0, PIXEL_ART_WIDTH, 0, 0, PIXEL_ART_WIDTH, height)
    val art = pixelate(pixels, PIXEL_ART_WIDTH, palette)
    return Bitmap.createBitmap(art, PIXEL_ART_WIDTH, height, Bitmap.Config.ARGB_8888)
}

/** 160 across makes each art pixel 12 screen pixels on a 1080p TV: chunky, but the picture still reads. */
const val PIXEL_ART_WIDTH = 160
