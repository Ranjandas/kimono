package io.github.ranjandas.kimono

import android.provider.Settings
import io.github.ranjandas.kimono.feature.settings.TvScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TvSettingsTest {
    @Test
    fun `every screen has somewhere to go`() {
        TvScreen.entries.forEach { assertTrue(TvSettings.actions(it).isNotEmpty()) }
    }

    @Test
    fun `display and sound tries Google TV's shared screen first`() {
        // Google TV has no display settings screen; its sound screen shows both (checked on the Streamer).
        assertEquals(
            listOf(Settings.ACTION_SOUND_SETTINGS, Settings.ACTION_DISPLAY_SETTINGS),
            TvSettings.actions(TvScreen.DisplaySound),
        )
    }
}
