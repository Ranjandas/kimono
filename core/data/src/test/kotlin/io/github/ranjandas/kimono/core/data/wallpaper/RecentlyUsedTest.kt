package io.github.ranjandas.kimono.core.data.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RecentlyUsedTest {
    @Test
    fun `the value used longest ago makes room for a new one`() {
        val recent = RecentlyUsed<String, Int>(capacity = 2)
        recent["a"] = 1
        recent["b"] = 2
        recent["a"]
        recent["c"] = 3
        assertEquals(1, recent["a"])
        assertNull(recent["b"])
        assertEquals(3, recent["c"])
    }

    @Test
    fun `only the files past the limit are dropped, oldest first`() {
        val ages = mapOf("new" to 30L, "old" to 10L, "oldest" to 5L, "mid" to 20L)
        assertEquals(listOf("oldest", "old"), oldestBeyond(ages.keys.toList(), limit = 2) { ages.getValue(it) })
    }

    @Test
    fun `nothing is dropped within the limit`() {
        assertEquals(emptyList<String>(), oldestBeyond(listOf("a", "b"), limit = 2) { 0L })
    }
}
