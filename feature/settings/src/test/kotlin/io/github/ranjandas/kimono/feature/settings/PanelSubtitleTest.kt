package io.github.ranjandas.kimono.feature.settings

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanelSubtitleTest {
    @Test
    fun `the subtitle stays the same all day and changes the next`() {
        val today = LocalDate.of(2026, 11, 10)
        assertEquals(PanelSubtitle.forDay(today), PanelSubtitle.forDay(today))
        assertNotEquals(PanelSubtitle.forDay(today), PanelSubtitle.forDay(today.plusDays(1)))
    }

    @Test
    fun `every phrase comes round, even before 1970`() {
        val start = LocalDate.of(1969, 12, 20)
        val seen = (0L until PanelSubtitle.phrases.size).map { PanelSubtitle.forDay(start.plusDays(it)) }.toSet()
        assertEquals(PanelSubtitle.phrases.toSet(), seen)
    }

    @Test
    fun `phrases are short lowercase words`() {
        PanelSubtitle.phrases.forEach { phrase ->
            assertEquals(phrase, phrase.lowercase())
            assertTrue("\"$phrase\" is too long for the header", phrase.length <= MAX_LENGTH)
        }
    }

    private companion object {
        const val MAX_LENGTH = 16
    }
}
