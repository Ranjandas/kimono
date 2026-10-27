package io.github.ranjandas.kimono.core.design.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MonogramTest {
    @Test
    fun `a single word is its first letter and the next consonant`() {
        assertEquals("Nt", monogram("Netflix"))
        assertEquals("Yt", monogram("YouTube"))
        assertEquals("Pl", monogram("Plex"))
        assertEquals("Kd", monogram("Kodi"))
        assertEquals("Sp", monogram("Spotify"))
    }

    @Test
    fun `several words are the first two initials`() {
        assertEquals("AT", monogram("Apple TV"))
        assertEquals("PV", monogram("Prime Video"))
        assertEquals("4P", monogram("4K Player"))
        assertEquals("DP", monogram("Disney+ Plus"))
    }

    @Test
    fun `short and odd names still get something`() {
        assertEquals("Ae", monogram("Aeiou"))
        assertEquals("X", monogram("X"))
        assertEquals("?", monogram(""))
        assertEquals("?", monogram("+++"))
    }
}
