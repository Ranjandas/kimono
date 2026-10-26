package io.github.ranjandas.kimono.core.design.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KimonoTypographyTest {
    private val type = KimonoTypography.Default

    @Test
    fun `body text meets the 10-foot minimum`() {
        assertTrue(type.body.fontSize.value >= 14f, "body is ${type.body.fontSize}")
    }

    @Test
    fun `nothing is smaller than 12 sp, 24 px at 1080p`() {
        val smallest = listOf(type.display, type.headline, type.title, type.body, type.label).minOf {
            it.fontSize.value
        }
        assertTrue(smallest >= 12f, "smallest is $smallest sp")
    }

    @Test
    fun `styles get smaller from display to label`() {
        val sizes = listOf(type.display, type.headline, type.title, type.body, type.label).map { it.fontSize.value }
        assertEquals(sizes.sortedDescending(), sizes)
        assertEquals(sizes.size, sizes.distinct().size)
    }

    @Test
    fun `every style uses the bundled font`() {
        listOf(type.display, type.headline, type.title, type.body, type.label).forEach {
            assertEquals(KimonoFontFamily, it.fontFamily)
        }
    }
}
