package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoxBlurTest {
    @Test
    fun `a flat image stays the same, edges included`() {
        val flat = IntArray(6 * 4) { 0xFF336699.toInt() }
        assertArrayEquals(flat, boxBlur(flat, 6, 4, radius = 2))
    }

    @Test
    fun `a bright pixel spreads to its neighbours and fades`() {
        val pixels = IntArray(9 * 9) { BLACK }
        pixels[4 * 9 + 4] = WHITE
        val blurred = boxBlur(pixels, 9, 9, radius = 1, passes = 1)
        val centre = blurred[4 * 9 + 4] and 0xFF
        assertEquals(255 / 9, centre)
        assertEquals(centre, blurred[3 * 9 + 3] and 0xFF)
        assertEquals(0, blurred[0] and 0xFF)
        assertTrue(blurred.all { it ushr 24 == 0xFF }, "alpha stays opaque")
    }

    @Test
    fun `the input is left alone`() {
        val pixels = intArrayOf(BLACK, WHITE, BLACK)
        boxBlur(pixels, 3, 1, radius = 1)
        assertArrayEquals(intArrayOf(BLACK, WHITE, BLACK), pixels)
    }

    private companion object {
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }
}
