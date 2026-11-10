package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.text.monogram
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * An app's [monogram] in bold letters, in [color] (the accent by default), so each app has a mark of its own where a
 * category glyph would make many look the same. Like a logo it has no plate behind it (#131). The letters scale with
 * the square, which takes its size from [modifier].
 */
@Composable
fun Monogram(label: String, modifier: Modifier = Modifier, color: Color = KimonoTheme.colors.accent) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        val fontSize = with(LocalDensity.current) { (maxHeight * LETTER_HEIGHT).toSp() }
        Text(
            monogram(label),
            style = KimonoTheme.typography.display.copy(
                fontSize = fontSize,
                lineHeight = fontSize,
                fontWeight = FontWeight.Bold,
            ),
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// Two capitals fill about four fifths of the width at this height, matching a logo's size.
private const val LETTER_HEIGHT = 0.5f
