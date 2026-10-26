package io.github.ranjandas.kimono.core.design.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class PaletteTest {
    @ParameterizedTest
    @EnumSource(ThemeId::class)
    fun `body text is legible on surfaces and background`(theme: ThemeId) {
        val p = theme.palette
        assertLegible(p.foreground, p.surface, "foreground on surface")
        assertLegible(p.foreground, p.background, "foreground on background")
    }

    @ParameterizedTest
    @EnumSource(ThemeId::class)
    fun `muted text is legible on surfaces and background`(theme: ThemeId) {
        val p = theme.palette
        val muted = quietestLegible(p.foreground, p.surface)
        assertLegible(muted, p.surface, "muted text on surface")
        assertLegible(muted, p.background, "muted text on background")
    }

    @ParameterizedTest
    @EnumSource(ThemeId::class)
    fun `muted text is quieter than body text`(theme: ThemeId) {
        val p = theme.palette
        val muted = quietestLegible(p.foreground, p.surface)
        assertTrue(contrastRatio(muted, p.surface) < contrastRatio(p.foreground, p.surface))
    }

    @ParameterizedTest
    @EnumSource(ThemeId::class)
    fun `accent shows as a focus border and carries text as a fill`(theme: ThemeId) {
        val p = theme.palette
        val accent = legibleAccent(p)
        assertContrast(accent, p.surface, MIN_UI_CONTRAST, "accent border on surface")
        assertContrast(accent, p.background, MIN_UI_CONTRAST, "accent border on background")
        assertContrast(p.background, accent, MIN_TEXT_CONTRAST, "text on accent fill")
    }

    @Test
    fun `accent is only nudged where the palette needs it`() {
        assertEquals(ThemeId.TokyoNight.palette.accent, legibleAccent(ThemeId.TokyoNight.palette))
        assertTrue(ThemeId.RosePine.palette.accent != legibleAccent(ThemeId.RosePine.palette))
    }

    @Test
    fun `ships the seven Omarchy palettes with Tokyo Night as default`() {
        assertEquals(
            listOf("Tokyo Night", "Catppuccin", "Gruvbox", "Rosé Pine", "Nord", "Kanagawa", "Everforest"),
            ThemeId.entries.map { it.displayName },
        )
        assertEquals(ThemeId.TokyoNight, ThemeId.Default)
    }

    private fun assertLegible(text: Rgb, background: Rgb, what: String) =
        assertContrast(text, background, MIN_TEXT_CONTRAST, what)

    private fun assertContrast(a: Rgb, b: Rgb, min: Double, what: String) {
        val ratio = contrastRatio(a, b)
        assertTrue(ratio >= min, "$what: $a on $b is %.2f:1".format(ratio))
    }
}
