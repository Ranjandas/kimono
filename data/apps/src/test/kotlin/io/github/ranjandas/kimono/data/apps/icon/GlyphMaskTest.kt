package io.github.ranjandas.kimono.data.apps.icon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GlyphMaskTest {
    private val clear = 0x00000000
    private val red = 0xFFE62117.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val purple = 0xFF7B4FD6.toInt()

    @Test
    fun `a logo on a solid plate keeps only the logo`() {
        // A red square with a white mark in the middle and see-through corners, like YouTube's icon.
        val image = intArrayOf(
            clear, red, red, clear,
            red, white, white, red,
            red, white, white, red,
            clear, red, red, clear,
        )
        val alpha = GlyphMask.extract(image)
        assertEquals(listOf(0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 0, 0, 0), alpha.toList())
        assertEquals(0xE62117, GlyphMask.plateColour(image))
    }

    @Test
    fun `a logo on transparency keeps its own outline`() {
        val image = intArrayOf(
            clear, clear, clear, clear,
            clear, purple, white, clear,
            clear, white, purple, clear,
            clear, clear, clear, clear,
        )
        assertNull(GlyphMask.plateColour(image))
        assertEquals(listOf(0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 0, 0, 0), GlyphMask.extract(image).toList())
    }

    @Test
    fun `many colours with none dominant is not a plate`() {
        val image = intArrayOf(red, white, purple, 0xFF00FF00.toInt(), 0xFF0000FF.toInt())
        assertNull(GlyphMask.plateColour(image))
    }

    @Test
    fun `bounds trim to the inked pixels`() {
        val alpha = intArrayOf(
            0, 0, 0, 0,
            0, 0, 200, 0,
            0, 255, 0, 0,
            0, 0, 0, 0,
        )
        assertEquals(GlyphBounds(left = 1, top = 1, right = 3, bottom = 3), GlyphMask.bounds(alpha, width = 4))
    }

    @Test
    fun `an empty image has no bounds`() {
        assertNull(GlyphMask.bounds(IntArray(16), width = 4))
    }
}
