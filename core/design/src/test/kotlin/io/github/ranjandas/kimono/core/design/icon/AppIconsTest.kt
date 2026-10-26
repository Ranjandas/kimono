package io.github.ranjandas.kimono.core.design.icon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppIconsTest {
    @Test
    fun `a known app gets its brand icon`() {
        assertEquals(KimonoIcon.YouTube, appIcon("com.google.android.youtube.tv", KimonoIcon.Media))
        assertEquals(KimonoIcon.Plex, appIcon("com.plexapp.android", KimonoIcon.Media))
    }

    @Test
    fun `an unknown app gets the fallback`() {
        assertEquals(KimonoIcon.Media, appIcon("org.jellyfin.androidtv", KimonoIcon.Media))
    }

    @Test
    fun `matching is by the whole package name`() {
        assertEquals(KimonoIcon.AppFallback, appIcon("com.google.android.youtube.tv.extra", KimonoIcon.AppFallback))
    }
}
