package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * An app's [mark] in the accent, or its monogram while there is no mark. There's no plate behind it: inside a
 * bordered tile or card a plate made three nested rectangles (#131), so the logo stands on the tile itself and
 * fills more of it. The square takes its size from [modifier].
 */
@Composable
fun AppMark(label: String, mark: Mark?, modifier: Modifier = Modifier) {
    // Omarchy keeps its accent small and sparing (#159): a resting tile's mark is softer and comes up on focus.
    val accent = KimonoTheme.colors.accent
    val color by animateFocusColor(
        if (LocalMarkFocused.current) accent else accent.copy(alpha = FocusDefaults.RESTING_MARK_ALPHA),
        label = "mark color",
    )
    if (mark == null) {
        Monogram(label, modifier, color)
    } else {
        Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
            MarkGlyph(mark, color, Modifier.fillMaxSize(MARK_SIZE))
        }
    }
}

/**
 * [mark] alone in [color], fitted to the size [modifier] gives it: the logo itself, as a list row shows it next to
 * the app's name (#132) and an [AppMark] at tile size.
 */
@Composable
fun MarkGlyph(mark: Mark, color: Color, modifier: Modifier = Modifier) {
    when (mark) {
        is Mark.Picture -> Image(
            bitmap = mark.bitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color),
        )

        is Mark.Symbol -> BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
            val fontSize = with(LocalDensity.current) { maxHeight.toSp() }
            Icon(
                mark.icon,
                color = color,
                style = TextStyle(fontSize = fontSize, lineHeight = fontSize, textAlign = TextAlign.Center),
            )
        }
    }
}

// Most of the square, leaving a margin so a tall logo doesn't touch the card's edges.
private const val MARK_SIZE = 0.8f
