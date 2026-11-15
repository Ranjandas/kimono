package io.github.ranjandas.kimono

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WallpaperExtraTest {
    private val home = "io.github.ranjandas.kimono.HomeActivity"
    private val link = "https://example.com/wall.jpg"

    @Test
    fun `takes the link only through the adb alias`() {
        assertEquals(link, wallpaperExtra(SET_WALLPAPER_ALIAS, link))
        assertNull(wallpaperExtra(home, link))
        assertNull(wallpaperExtra(null, link))
    }

    @Test
    fun `takes drawn wallpapers and the empty reset`() {
        assertEquals("kimono:moon", wallpaperExtra(SET_WALLPAPER_ALIAS, "kimono:moon"))
        assertEquals("", wallpaperExtra(SET_WALLPAPER_ALIAS, ""))
        assertNull(wallpaperExtra(home, ""))
    }

    @Test
    fun `refuses other links and a missing extra`() {
        assertNull(wallpaperExtra(SET_WALLPAPER_ALIAS, "http://example.com/wall.jpg"))
        assertNull(wallpaperExtra(SET_WALLPAPER_ALIAS, "kimono:nothing"))
        assertNull(wallpaperExtra(SET_WALLPAPER_ALIAS, null))
    }
}
