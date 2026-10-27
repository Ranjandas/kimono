package io.github.ranjandas.kimono.core.data.wallpaper

/** The part of a source image to keep, in source pixels. */
data class CropRect(val x: Int, val y: Int, val width: Int, val height: Int)

/**
 * The largest power-of-two step that still decodes at least [targetWidth] × [targetHeight], so a 5K image isn't
 * decoded in full only to be scaled down.
 */
fun sampleSize(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Int {
    var size = 1
    while (sourceWidth / (size * 2) >= targetWidth && sourceHeight / (size * 2) >= targetHeight) size *= 2
    return size
}

/** The centred region of a [sourceWidth] × [sourceHeight] image with the target's aspect ratio. */
fun centerCrop(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): CropRect {
    // Compare aspect ratios without floating point: source wider than target means trimming the sides.
    return if (sourceWidth.toLong() * targetHeight > sourceHeight.toLong() * targetWidth) {
        val width = (sourceHeight.toLong() * targetWidth / targetHeight).toInt()
        CropRect((sourceWidth - width) / 2, 0, width, sourceHeight)
    } else {
        val height = (sourceWidth.toLong() * targetHeight / targetWidth).toInt()
        CropRect(0, (sourceHeight - height) / 2, sourceWidth, height)
    }
}
