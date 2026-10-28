package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * Each app's mark by package name: its logo or wordmark as a shape to tint, shown in place of its [Monogram]
 * (#116). Apps without one, or not made yet, have none.
 */
@Immutable
class AppMarks(private val byPackage: Map<String, ImageBitmap>) {
    operator fun get(packageName: String): ImageBitmap? = byPackage[packageName]

    companion object {
        val None = AppMarks(emptyMap())
    }
}

/**
 * An app's [mark] in the accent on the same tinted square as a [Monogram], so a tile looks the same either way, or
 * its monogram while there is no mark. The square takes its size from [modifier].
 */
@Composable
fun AppMark(label: String, mark: ImageBitmap?, modifier: Modifier = Modifier) {
    if (mark == null) {
        Monogram(label, modifier)
    } else {
        val colors = KimonoTheme.colors
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(colors.accent.copy(alpha = TINT_ALPHA), KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = mark,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(MARK_SIZE),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(colors.accent),
            )
        }
    }
}

// A little smaller than the square, like a monogram's letters, so a tall logo doesn't touch its edges.
private const val MARK_SIZE = 0.62f
