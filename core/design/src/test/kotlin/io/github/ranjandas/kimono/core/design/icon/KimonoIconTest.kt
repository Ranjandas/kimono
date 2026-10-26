package io.github.ranjandas.kimono.core.design.icon

import java.awt.Font
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KimonoIconTest {
    @ParameterizedTest
    @ValueSource(strings = ["JetBrainsMonoNerdFont-Regular.ttf", "JetBrainsMonoNerdFont-Bold.ttf"])
    fun `every icon is in the source font`(fileName: String) {
        val font = File(System.getProperty("kimono.fontDir"), fileName).inputStream().use {
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
        val missing = KimonoIcon.entries.filterNot { font.canDisplay(it.codePoint) }
        assertTrue(missing.isEmpty(), "Not in $fileName: $missing")
    }

    @Test
    fun `icons are private-use code points, so the subsetting task keeps them`() {
        val outside = KimonoIcon.entries.filterNot { it.codePoint in 0xE000..0xF8FF || it.codePoint >= 0xF0000 }
        assertTrue(outside.isEmpty(), "Not private use: $outside")
    }

    @Test
    fun `no two icons share a glyph`() {
        assertEquals(KimonoIcon.entries.size, KimonoIcon.entries.distinctBy { it.codePoint }.size)
    }

    @Test
    fun `text encodes code points above the BMP as a surrogate pair`() {
        assertEquals("󰋜", KimonoIcon.Home.text)
    }
}
