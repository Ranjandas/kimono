package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/** What a tile shows as an app's logo, tinted in the accent (#116). */
@Immutable
sealed interface Mark {
    /** A shape cut from what the app ships: its logo or banner wordmark. */
    @Immutable
    data class Picture(val bitmap: ImageBitmap) : Mark

    /** A brand icon from Kimono's icon font, the same one the lists show. */
    @Immutable
    data class Symbol(val icon: KimonoIcon) : Mark
}

/**
 * Each app's [Mark] by package name, shown in place of its [Monogram]. Apps without one, or whose mark isn't made
 * yet, have none.
 */
@Immutable
class AppMarks(private val byPackage: Map<String, Mark>) {
    operator fun get(packageName: String): Mark? = byPackage[packageName]

    companion object {
        val None = AppMarks(emptyMap())
    }
}

/**
 * An app's [mark] in the accent on the same tinted square as a [Monogram], so a tile looks the same either way, or
 * its monogram while there is no mark. The square takes its size from [modifier].
 */
@Composable
fun AppMark(label: String, mark: Mark?, modifier: Modifier = Modifier) {
    if (mark == null) {
        Monogram(label, modifier)
    } else {
        val colors = KimonoTheme.colors
        BoxWithConstraints(
            modifier = modifier
                .aspectRatio(1f)
                .background(colors.accent.copy(alpha = TINT_ALPHA), KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)),
            contentAlignment = Alignment.Center,
        ) {
            when (mark) {
                is Mark.Picture -> Image(
                    bitmap = mark.bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(MARK_SIZE),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(colors.accent),
                )

                is Mark.Symbol -> {
                    val fontSize = with(LocalDensity.current) { (maxHeight * MARK_SIZE).toSp() }
                    Icon(
                        mark.icon,
                        color = colors.accent,
                        style = TextStyle(fontSize = fontSize, lineHeight = fontSize, textAlign = TextAlign.Center),
                    )
                }
            }
        }
    }
}

// A little smaller than the square, like a monogram's letters, so a tall logo doesn't touch its edges.
private const val MARK_SIZE = 0.62f
