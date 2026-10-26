package io.github.ranjandas.kimono.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ranjandas.kimono.core.design.R

/** JetBrains Mono Nerd Font, subset at build time. Text and icons both use it (PLAN §4). */
val KimonoFontFamily = FontFamily(
    Font(R.font.kimono_mono_regular, FontWeight.Normal),
    Font(R.font.kimono_mono_bold, FontWeight.Bold),
)

/**
 * Type scale for a TV seen from across the room. Sizes are for the 960 × 540 dp canvas a 1080p TV
 * reports, so 1 sp is 2 px at 1080p. Body text at 14 sp is 28 px: in line with tv-material's own body
 * styles, and 1.4 × the mockup's 20 px, which is too small from the sofa.
 */
@Immutable
data class KimonoTypography(
    /** The title in the continue-watching tile. */
    val display: TextStyle,
    /** Screen titles and the episode line under [display]. */
    val headline: TextStyle,
    /** App names on tiles, and buttons. */
    val title: TextStyle,
    /** List rows and most other text. The 10-foot minimum for reading (PLAN §4). */
    val body: TextStyle,
    /** Top bar, tile headers, key hints and metadata next to body text. */
    val label: TextStyle,
) {
    /** Every style at [factor] times its size, for the text size setting. Weights and fonts stay the same. */
    fun scaled(factor: Float): KimonoTypography = if (factor == 1f) {
        this
    } else {
        KimonoTypography(
            display = display.scaled(factor),
            headline = headline.scaled(factor),
            title = title.scaled(factor),
            body = body.scaled(factor),
            label = label.scaled(factor),
        )
    }

    companion object {
        val Default = KimonoTypography(
            display = style(sizeSp = 72, lineHeightSp = 76, weight = FontWeight.Bold),
            headline = style(sizeSp = 20, lineHeightSp = 26),
            title = style(sizeSp = 16, lineHeightSp = 22, weight = FontWeight.Bold),
            body = style(sizeSp = 14, lineHeightSp = 20),
            label = style(sizeSp = 12, lineHeightSp = 16),
        )

        private fun TextStyle.scaled(factor: Float) =
            copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)

        private fun style(sizeSp: Int, lineHeightSp: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
            fontFamily = KimonoFontFamily,
            fontWeight = weight,
            fontSize = sizeSp.sp,
            lineHeight = lineHeightSp.sp,
        )
    }
}
