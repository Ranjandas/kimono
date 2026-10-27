package io.github.ranjandas.kimono.core.data.idle

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PixelShiftTest {
    private val cycle = (0 until 9).map(PixelShift::offsetAt)

    @Test
    fun `starts where the screen normally sits`() {
        assertEquals(0 to 0, PixelShift.offsetAt(0))
    }

    @Test
    fun `never moves further than the limit`() {
        cycle.forEach { (x, y) -> assertTrue(abs(x) <= PixelShift.MAX_DP && abs(y) <= PixelShift.MAX_DP) }
    }

    @Test
    fun `visits a different position at every step of a cycle`() {
        assertEquals(cycle.size, cycle.distinct().size)
    }

    @Test
    fun `repeats after a cycle`() {
        assertEquals(cycle, (9 until 18).map(PixelShift::offsetAt))
    }
}
