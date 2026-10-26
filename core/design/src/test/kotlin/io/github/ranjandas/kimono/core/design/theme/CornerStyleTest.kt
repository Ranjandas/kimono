package io.github.ranjandas.kimono.core.design.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CornerStyleTest {
    @Test
    fun `square is the default and has no radius`() {
        assertEquals(CornerStyle.Square, CornerStyle.Default)
        assertEquals(0, CornerStyle.Square.radiusDp)
    }

    @Test
    fun `nested radius subtracts the padding`() {
        assertEquals(8, CornerStyle.Round.nestedRadiusDp(4))
        assertEquals(2, CornerStyle.Soft.nestedRadiusDp(4))
    }

    @Test
    fun `nested radius never goes below zero`() {
        assertEquals(0, CornerStyle.Soft.nestedRadiusDp(10))
        assertEquals(0, CornerStyle.Square.nestedRadiusDp(4))
    }

    @Test
    fun `reads a stored name, falling back to the default`() {
        assertEquals(CornerStyle.Round, CornerStyle.fromName("Round"))
        assertEquals(CornerStyle.Default, CornerStyle.fromName(null))
        assertEquals(CornerStyle.Default, CornerStyle.fromName("Retired"))
    }
}
