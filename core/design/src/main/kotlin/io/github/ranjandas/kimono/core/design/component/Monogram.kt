package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.text.monogram
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * An app's [monogram] in bold accent letters on a quiet square, so each app has a mark of its own
 * where a category glyph would make many look the same. The letters scale with the square, which takes its size
 * from [modifier]; its corners nest inside a tile's.
 */
@Composable
fun Monogram(label: String, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .background(colors.surfaceVariant, KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)),
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
            color = colors.accent,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// Two capitals fill about two thirds of the width at this height.
private const val LETTER_HEIGHT = 0.42f
