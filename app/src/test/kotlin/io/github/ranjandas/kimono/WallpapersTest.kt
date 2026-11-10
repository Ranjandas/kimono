package io.github.ranjandas.kimono

import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.wallpaper.DrawnWallpaper
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import java.time.Duration
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WallpapersTest {
    private val settings = LauncherSettings()

    @Test
    fun `Kimono's sky follows the hour`() {
        assertEquals(DrawnWallpaper.YugureNight.url, wallpaperUrl(settings, ThemeId.Kimono, at(hour = 3)))
        assertEquals(DrawnWallpaper.YugureDawn.url, wallpaperUrl(settings, ThemeId.Kimono, at(hour = 7)))
        assertEquals(DrawnWallpaper.YugureDay.url, wallpaperUrl(settings, ThemeId.Kimono, at(hour = 13)))
        assertEquals(DrawnWallpaper.YugureDusk.url, wallpaperUrl(settings, ThemeId.Kimono, at(hour = 19)))
        assertEquals(DrawnWallpaper.YugureNight.url, wallpaperUrl(settings, ThemeId.Kimono, at(hour = 23)))
    }

    @Test
    fun `Kimono Sumi keeps its pattern of the day`() {
        val now = at(hour = 19)
        assertEquals(
            DrawnWallpaper.forDay(now.toLocalDate().toEpochDay()).url,
            wallpaperUrl(settings, ThemeId.KimonoSumi, now),
        )
    }

    @Test
    fun `a wallpaper of the user's own or none wins over the sky`() {
        val own = "https://example.com/wall.webp"
        assertEquals(own, wallpaperUrl(settings.copy(wallpaperUrl = own), ThemeId.Kimono, at(hour = 19)))
        assertNull(wallpaperUrl(settings.copy(wallpaper = false), ThemeId.Kimono, at(hour = 19)))
    }

    @Test
    fun `the clock wakes just after the top of the hour`() {
        val now = LocalDateTime.of(2026, 11, 10, 20, 59, 30)
        assertEquals(Duration.ofSeconds(31), untilNextHour(now))
        assertEquals(Duration.ofMinutes(60).plusSeconds(1), untilNextHour(now.withMinute(0).withSecond(0)))
    }

    private fun at(hour: Int) = LocalDateTime.of(2026, 11, 10, hour, 30)
}
