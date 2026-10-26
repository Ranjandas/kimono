package io.github.ranjandas.kimono.core.design.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RgbTest {
    private val black = Rgb.of("#000000")
    private val white = Rgb.of("#ffffff")

    @Test
    fun `parses hex`() {
        val c = Rgb.of("#7aa2f7")
        assertEquals(0x7a, c.red)
        assertEquals(0xa2, c.green)
        assertEquals(0xf7, c.blue)
        assertEquals("#7aa2f7", c.toString())
    }

    @Test
    fun `rejects other formats`() {
        assertThrows<IllegalArgumentException> { Rgb.of("7aa2f7") }
        assertThrows<IllegalArgumentException> { Rgb.of("#7af") }
    }

    @Test
    fun `contrast ratio matches WCAG endpoints`() {
        assertEquals(21.0, contrastRatio(black, white), 0.01)
        assertEquals(1.0, contrastRatio(white, white), 0.0001)
        assertEquals(contrastRatio(black, white), contrastRatio(white, black), 0.0001)
    }

    @Test
    fun `mix interpolates channels`() {
        assertEquals(black, black.mix(white, 0.0))
        assertEquals(white, black.mix(white, 1.0))
        assertEquals(Rgb.of("#808080"), black.mix(white, 0.5))
    }

    @Test
    fun `quietest legible colour sits at the contrast threshold`() {
        val muted = quietestLegible(white, black)
        val ratio = contrastRatio(muted, black)
        assertEquals(true, ratio >= MIN_TEXT_CONTRAST)
        assertEquals(true, ratio < MIN_TEXT_CONTRAST + 0.5)
    }
}
