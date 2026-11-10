package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class DrawnWallpaperTest {
    @ParameterizedTest
    @EnumSource(DrawnWallpaper::class, names = ["Moon", "Seigaiha", "Asanoha"])
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
    fun `takes turns, one a day, among Kimono Sumi's`() {
        val sumi = listOf(DrawnWallpaper.Moon, DrawnWallpaper.Seigaiha, DrawnWallpaper.Asanoha)
        assertEquals(sumi, DrawnWallpaper.entries.filter { it.oled })
        val week = (0L until 6L).map(DrawnWallpaper::forDay)
        assertEquals(sumi + sumi, week)
        assertEquals(DrawnWallpaper.forDay(2), DrawnWallpaper.forDay(-1))
    }

    @Test
    fun `Yugure follows the clock`() {
        val byHour = (0..23).map { DrawnWallpaper.yugureAt(it) }
        assertEquals(List(5) { DrawnWallpaper.YugureNight }, byHour.subList(0, 5))
        assertEquals(List(5) { DrawnWallpaper.YugureDawn }, byHour.subList(5, 10))
        assertEquals(List(6) { DrawnWallpaper.YugureDay }, byHour.subList(10, 16))
        assertEquals(List(5) { DrawnWallpaper.YugureDusk }, byHour.subList(16, 21))
        assertEquals(List(3) { DrawnWallpaper.YugureNight }, byHour.subList(21, 24))
    }

    @ParameterizedTest
    @EnumSource(DrawnWallpaper::class, names = ["YugureDawn", "YugureDay", "YugureDusk", "YugureNight"])
    fun `Yugure fills the screen, darkest at the top and with a bright sun`(wallpaper: DrawnWallpaper) {
        val pixels = wallpaper.pixels(WIDTH, HEIGHT)
        assertEquals(WIDTH * HEIGHT, pixels.size)
        assertTrue(pixels.all { it ushr 24 == 0xFF }, "opaque")
        val top = brightness(pixels[WIDTH / 2])
        // The disc's centre: higher at night, for the moon.
        val sunY = if (wallpaper == DrawnWallpaper.YugureNight) 300 else 520
        val sun = brightness(pixels[sunY * WIDTH + 1450])
        assertTrue(sun > top + 100, "sun $sun should stand out from the top of the sky $top")
    }

    @Test
    fun `Yugure's night is darker than its day`() {
        val night = DrawnWallpaper.YugureNight.pixels(WIDTH / 4, HEIGHT / 4).sumOf(::brightness)
        val day = DrawnWallpaper.YugureDay.pixels(WIDTH / 4, HEIGHT / 4).sumOf(::brightness)
        assertTrue(night < day / 2, "night $night, day $day")
    }

    private fun brightness(argb: Int): Int = ((argb shr 16) and 0xFF) + ((argb shr 8) and 0xFF) + (argb and 0xFF)

    private companion object {
        const val WIDTH = 1920
        const val HEIGHT = 1080
        const val BLACK = 0xFF000000.toInt()
    }
}
