package io.github.ranjandas.kimono.core.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PinEncodingTest {
    @Test
    fun `round-trips a pin list`() {
        val pins = listOf("pkg.plex", "pkg.kodi", "pkg.jellyfin")
        assertEquals(pins, decodePins(encodePins(pins)))
    }

    @Test
    fun `reads no pins from a missing or blank value`() {
        assertEquals(emptyList<String>(), decodePins(null))
        assertEquals(emptyList<String>(), decodePins(""))
        assertEquals(emptyList<String>(), decodePins("\n\n"))
    }
}
