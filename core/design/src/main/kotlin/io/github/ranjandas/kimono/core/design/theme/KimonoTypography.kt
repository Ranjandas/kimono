package io.github.ranjandas.kimono.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.ranjandas.kimono.core.design.R

/**
 * Noto Sans for what people read: app names, titles and body text. It's easy to read from the sofa, and it covers
 * most scripts an app name might use. Others fall back to the system's fonts.
 */
val KimonoFontFamily = FontFamily(
    Font(R.font.kimono_sans_regular, FontWeight.Normal),
    Font(R.font.kimono_sans_medium, FontWeight.Medium),
    Font(R.font.kimono_sans_bold, FontWeight.Bold),
)

/**
 * JetBrains Mono, Omarchy's own face, for the small things around the names (#152): section labels, values,
 * counts, the clock and the system tile. Never for app names, which stay in [KimonoFontFamily].
 */
val KimonoMonoFontFamily = FontFamily(
    Font(R.font.kimono_mono_regular, FontWeight.Normal),
    Font(R.font.kimono_mono_bold, FontWeight.Bold),
)

/** The Nerd Fonts symbols, cut down at build time to the icons Kimono uses. Only [Icon] draws with it. */
val KimonoIconFontFamily = FontFamily(Font(R.font.kimono_icons, FontWeight.Normal))

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
    /** Top bar, key hints and metadata next to body text. */
    val label: TextStyle,
    /**
     * Tile and section headers: the label size in mono, spaced out, and drawn in UPPERCASE by `SectionHeader`, the
     * way Omarchy's panels label their sections.
     */
    val section: TextStyle,
    /** Values, counts and the clock beside label-size text, in mono. */
    val value: TextStyle,
    /** Body-size mono, for the fetch-style system tile. */
    val data: TextStyle,
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
            section = section.scaled(factor),
            value = value.scaled(factor),
            data = data.scaled(factor),
        )
    }

    companion object {
        val Default = KimonoTypography(
            display = style(sizeSp = 72, lineHeightSp = 76, weight = FontWeight.Bold),
            headline = style(sizeSp = 20, lineHeightSp = 26),
            title = style(sizeSp = 16, lineHeightSp = 22, weight = FontWeight.Bold),
            body = style(sizeSp = 14, lineHeightSp = 20),
            label = style(sizeSp = 12, lineHeightSp = 16),
            section = style(sizeSp = 11, lineHeightSp = 16, family = KimonoMonoFontFamily)
                .copy(letterSpacing = SECTION_TRACKING.em),
            value = style(sizeSp = 12, lineHeightSp = 16, family = KimonoMonoFontFamily),
            data = style(sizeSp = 13, lineHeightSp = 20, family = KimonoMonoFontFamily),
        )

        // Tracking for uppercase section labels, in ems: capitals set tight read as shouting, spaced as a label.
        private const val SECTION_TRACKING = 0.12f

        private fun TextStyle.scaled(factor: Float) =
            copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)

        private fun style(
            sizeSp: Int,
            lineHeightSp: Int,
            weight: FontWeight = FontWeight.Normal,
            family: FontFamily = KimonoFontFamily,
        ) = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = sizeSp.sp,
            lineHeight = lineHeightSp.sp,
        )
    }
}
