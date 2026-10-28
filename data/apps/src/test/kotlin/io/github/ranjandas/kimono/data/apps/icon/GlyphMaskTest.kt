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
        val alpha = GlyphMask.extract(image, width = 4)
        assertEquals(listOf(0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 0, 0, 0), alpha.toList())
        assertEquals(0xE62117, GlyphMask.plateColour(image))
    }

    @Test
    fun `a logo on transparency keeps its own outline`() {
        val image = intArrayOf(
            clear, clear, clear, clear,
            clear, purple, purple, clear,
            clear, purple, purple, clear,
            clear, clear, clear, clear,
        )
        assertNull(GlyphMask.plateColour(image))
        val alpha = GlyphMask.extract(image, width = 4)
        assertEquals(listOf(0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 0, 0, 0), alpha.toList())
    }

    @Test
    fun `a gap is cut where two colours of a logo meet`() {
        // Purple on the left, white on the right, on transparency: the purple next to the white is cut away.
        val image = IntArray(6 * 6) { i ->
            val x = i % 6
            val y = i / 6
            when {
                x == 0 || x == 5 || y == 0 || y == 5 -> clear
                x < 3 -> purple
                else -> white
            }
        }
        val alpha = GlyphMask.extract(image, width = 6)
        val row = alpha.slice(12 until 18)
        assertEquals(listOf(0, 0, 0, 255, 255, 0), row)
    }

    @Test
    fun `a smooth gradient isn't cut`() {
        val image = IntArray(16) { i -> 0xFF000000.toInt() or ((i * 4) shl 16) or (i * 4) }
        assertEquals(List(16) { 255 }, GlyphMask.extract(image, width = 4, plate = null).toList())
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
