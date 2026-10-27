package io.github.ranjandas.kimono.core.design.icon

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoIconFontFamily
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * Draws [icon] as text in the icon font at [style]'s size, so it lines up with the label next to it and takes
 * its colour. Icons are decorative: the label carries the meaning, so TalkBack skips them (PLAN §6.5).
 *
 * The font's icons are up to 0.92 em wide but advance only 0.83 em, so text layout would clip their right side.
 * The icon gets a full em instead.
 */
@Composable
fun Icon(
    icon: KimonoIcon,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = KimonoTheme.typography.body,
) {
    val width = if (style.fontSize.isSpecified) {
        Modifier.width(with(LocalDensity.current) { style.fontSize.toDp() } * ICON_WIDTH_EM)
    } else {
        Modifier
    }
    Text(
        text = icon.text,
        modifier = modifier.then(width).clearAndSetSemantics {},
        color = color,
        style = style.copy(fontFamily = KimonoIconFontFamily, fontWeight = FontWeight.Normal),
        maxLines = 1,
        softWrap = false,
    )
}

// Wider than the widest icon in the font (0.918 em), measured from its glyph bounds.
internal const val ICON_WIDTH_EM = 1f
