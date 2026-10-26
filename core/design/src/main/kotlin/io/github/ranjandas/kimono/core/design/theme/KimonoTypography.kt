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
 * reports, so body text at 20 sp is 40 px tall and about 80 monospace columns fit across the screen.
 */
@Immutable
data class KimonoTypography(
    /** Large clock and empty-state headlines. */
    val display: TextStyle,
    /** Screen titles. */
    val headline: TextStyle,
    /** Tile and section headers. */
    val title: TextStyle,
    /** App names, list rows and most other text. The 10-foot minimum for reading (PLAN §4). */
    val body: TextStyle,
    /** Key hints, status values and metadata next to body text. */
    val label: TextStyle,
) {
    companion object {
        val Default = KimonoTypography(
            display = style(sizeSp = 40, lineHeightSp = 52, weight = FontWeight.Bold),
            headline = style(sizeSp = 28, lineHeightSp = 36, weight = FontWeight.Bold),
            title = style(sizeSp = 24, lineHeightSp = 32, weight = FontWeight.Bold),
            body = style(sizeSp = 20, lineHeightSp = 28),
            label = style(sizeSp = 18, lineHeightSp = 24),
        )

        private fun style(sizeSp: Int, lineHeightSp: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
            fontFamily = KimonoFontFamily,
            fontWeight = weight,
            fontSize = sizeSp.sp,
            lineHeight = lineHeightSp.sp,
        )
    }
}
