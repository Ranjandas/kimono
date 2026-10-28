package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class DrawnWallpaperTest {
    @ParameterizedTest
    @EnumSource(DrawnWallpaper::class)
    fun `leaves most of an OLED panel switched off`(wallpaper: DrawnWallpaper) {
        val pixels = wallpaper.pixels(WIDTH, HEIGHT)
        assertEquals(WIDTH * HEIGHT, pixels.size)
        val lit = pixels.count { it != BLACK }.toDouble() / pixels.size
        assertTrue(lit in 0.01..0.15, "${"%.1f".format(lit * 100)}% of pixels lit")
        assertTrue(pixels.all { it ushr 24 == 0xFF }, "opaque")
    }

    @ParameterizedTest
    @EnumSource(DrawnWallpaper::class)
    fun `is found again by its link`(wallpaper: DrawnWallpaper) {
        assertTrue(wallpaper.url.startsWith("kimono:"))
        assertEquals(wallpaper, DrawnWallpaper.fromUrl(wallpaper.url))
    }

    @Test
    fun `other links aren't drawn`() {
        assertNull(DrawnWallpaper.fromUrl("https://example.com/moon.webp"))
        assertNull(DrawnWallpaper.fromUrl("kimono:retired"))
    }

    @Test
    fun `takes turns, one a day`() {
        val week = (0L until 6L).map(DrawnWallpaper::forDay)
        assertEquals(DrawnWallpaper.entries + DrawnWallpaper.entries, week)
        assertEquals(DrawnWallpaper.forDay(2), DrawnWallpaper.forDay(-1))
    }

    private companion object {
        const val WIDTH = 1920
        const val HEIGHT = 1080
        const val BLACK = 0xFF000000.toInt()
    }
}
