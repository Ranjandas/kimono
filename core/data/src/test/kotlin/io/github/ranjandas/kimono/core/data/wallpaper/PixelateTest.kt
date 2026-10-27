package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PixelateTest {
    private val blackAndWhite = intArrayOf(0x000000, 0xFFFFFF)

    @Test
    fun `colours far from any other palette colour stay as they are`() {
        val pixels = intArrayOf(0x000000, 0xFFFFFF, 0x000000, 0xFFFFFF)
        assertEquals(pixels.map { it or OPAQUE }, pixelate(pixels, 2, blackAndWhite).toList())
    }

    @Test
    fun `mid grey dithers to an even mix of the two nearest colours`() {
        val grey = IntArray(16) { 0x808080 }
        val art = pixelate(grey, 4, blackAndWhite)
        val white = art.count { it == WHITE }
        assertEquals(8, white, "half of a 4×4 block")
        assertTrue(art.all { it == WHITE || it == BLACK })
    }

    @Test
    fun `the darker the grey, the fewer white pixels`() {
        fun whites(level: Int) = pixelate(IntArray(16) { level * 0x010101 }, 4, blackAndWhite).count { it == WHITE }
        assertTrue(whites(0x60) < whites(0x80))
        assertTrue(whites(0x80) < whites(0xA0))
    }

    @Test
    fun `each pixel takes the nearest palette colour`() {
        val palette = intArrayOf(0x000000, 0xFF0000, 0x0000FF)
        val art = pixelate(intArrayOf(0xF01010, 0x1010F0), 2, palette)
        assertEquals(listOf(0xFFFF0000.toInt(), 0xFF0000FF.toInt()), art.toList())
    }

    private companion object {
        const val OPAQUE = 0xFF shl 24
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
    }
}
