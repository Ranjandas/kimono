package io.github.ranjandas.kimono.core.design.icon

import java.awt.Font
import java.awt.font.FontRenderContext
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KimonoIconTest {
    private val font = File(System.getProperty("kimono.fontDir"), ICON_FONT).inputStream().use {
        Font.createFont(Font.TRUETYPE_FONT, it).deriveFont(EM)
    }

    @Test
    fun `every icon is in the icon font`() {
        val missing = KimonoIcon.entries.filterNot { font.canDisplay(it.codePoint) }
        assertTrue(missing.isEmpty(), "Not in $ICON_FONT: $missing")
    }

    @Test
    fun `every icon fits in the width Icon gives it`() {
        val context = FontRenderContext(null, false, false)
        val tooWide = KimonoIcon.entries.filter {
            font.createGlyphVector(context, it.text).visualBounds.maxX > EM * ICON_WIDTH_EM
        }
        assertTrue(tooWide.isEmpty(), "Wider than $ICON_WIDTH_EM em: $tooWide")
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

    private companion object {
        const val EM = 1000f
        const val ICON_FONT = "SymbolsNerdFont-Regular.ttf"
    }
}
