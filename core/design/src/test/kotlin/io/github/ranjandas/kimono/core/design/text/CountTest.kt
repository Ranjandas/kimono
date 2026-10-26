package io.github.ranjandas.kimono.core.design.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CountTest {
    @Test
    fun `uses the singular for one only`() {
        assertEquals("0 apps", count(0, "app"))
        assertEquals("1 app", count(1, "app"))
        assertEquals("8 apps", count(8, "app"))
        assertEquals("2 matches", count(2, "match", "matches"))
    }
}
