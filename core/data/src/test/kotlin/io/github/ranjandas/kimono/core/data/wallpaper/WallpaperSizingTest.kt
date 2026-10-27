package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WallpaperSizingTest {
    @Test
    fun `a 5K image decodes at half size for a 1080p screen`() {
        assertEquals(2, sampleSize(5120, 2880, 1920, 1080))
    }

    @Test
    fun `an image no bigger than the screen decodes in full`() {
        assertEquals(1, sampleSize(1920, 1080, 1920, 1080))
        assertEquals(1, sampleSize(1280, 720, 1920, 1080))
    }

    @Test
    fun `a 16 by 9 image isn't cropped`() {
        assertEquals(CropRect(0, 0, 2560, 1440), centerCrop(2560, 1440, 1920, 1080))
    }

    @Test
    fun `a wider image loses its sides equally`() {
        assertEquals(CropRect(280, 0, 1920, 1080), centerCrop(2480, 1080, 1920, 1080))
    }

    @Test
    fun `a taller image loses its top and bottom equally`() {
        assertEquals(CropRect(0, 420, 1920, 1080), centerCrop(1920, 1920, 1920, 1080))
    }
}
