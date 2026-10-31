package io.github.ranjandas.kimono.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

/** Semantic colour tokens. Components read these, never a [Palette] or a literal colour. */
@Immutable
data class KimonoColors(
    val isLight: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val text: Color,
    /** Secondary text: as quiet as the palette allows while keeping 4.5:1 on [surface]. */
    val textMuted: Color,
    val border: Color,
    val borderFocused: Color,
    /**
     * Focus and primary highlights: the palette accent, nudged where needed to keep 3:1 against [surface] and
     * 4.5:1 under [onAccent] text.
     */
    val accent: Color,
    val accentSecondary: Color,
    /** Text and icons drawn on an [accent] fill. */
    val onAccent: Color,
    val error: Color,
    val warning: Color,
    val success: Color,
) {
    companion object {
        fun from(palette: Palette): KimonoColors {
            val accent = legibleAccent(palette).toColor()
            return KimonoColors(
                isLight = palette.isLight,
                background = palette.background.toColor(),
                surface = palette.surface.toColor(),
                surfaceVariant = palette.surfaceVariant.toColor(),
                text = palette.foreground.toColor(),
                textMuted = quietestLegible(palette.foreground, palette.surface).toColor(),
                border = palette.line.toColor(),
                borderFocused = accent,
                accent = accent,
                accentSecondary = palette.accent2.toColor(),
                onAccent = palette.background.toColor(),
                error = palette.red.toColor(),
                warning = palette.yellow.toColor(),
                success = palette.green.toColor(),
            )
        }
    }
}

/** Shape tokens derived from the [CornerStyle] setting. */
@Immutable
data class KimonoShapes(val cornerStyle: CornerStyle) {
    /** Tiles, panels and their focus borders. */
    val tile: Shape = RoundedCornerShape(cornerStyle.radiusDp.dp)

    /** Small chips such as key hints, at half the tile radius. */
    val small: Shape = RoundedCornerShape((cornerStyle.radiusDp / 2).dp)

    /** An element inset [paddingDp] inside a [tile], with its radius reduced so the curves stay parallel. */
    fun nested(paddingDp: Int): Shape = RoundedCornerShape(cornerStyle.nestedRadiusDp(paddingDp).dp)
}

private val LocalKimonoColors = staticCompositionLocalOf { KimonoColors.from(ThemeId.Default.palette) }
private val LocalKimonoShapes = staticCompositionLocalOf { KimonoShapes(CornerStyle.Default) }
private val LocalKimonoTypography = staticCompositionLocalOf { KimonoTypography.Default }

/**
 * Applies a palette, corner style and the type scale, at [textScale] times its size, to [content].
 * Read the tokens through [KimonoTheme.colors], [KimonoTheme.shapes] and [KimonoTheme.typography].
 * With [overWallpaper], screens draw no background of their own, so a wallpaper behind them shows through.
 */
@Composable
fun KimonoTheme(
    theme: ThemeId = ThemeId.Default,
    cornerStyle: CornerStyle = CornerStyle.Default,
    textScale: Float = 1f,
    overWallpaper: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = remember(theme, overWallpaper) {
        KimonoColors.from(theme.palette).let { if (overWallpaper) it.copy(background = Color.Transparent) else it }
    }
    val shapes = remember(cornerStyle) { KimonoShapes(cornerStyle) }
    val typography = remember(textScale) { KimonoTypography.Default.scaled(textScale) }
    val colorScheme = remember(colors) { colors.toTvColorScheme() }
    MaterialTheme(colorScheme = colorScheme, typography = TvTypography) {
        CompositionLocalProvider(
            LocalKimonoColors provides colors,
            LocalKimonoShapes provides shapes,
            LocalKimonoTypography provides typography,
            LocalContentColor provides colors.text,
            content = content,
        )
    }
}

object KimonoTheme {
    val colors: KimonoColors
        @Composable @ReadOnlyComposable
        get() = LocalKimonoColors.current

    val shapes: KimonoShapes
        @Composable @ReadOnlyComposable
        get() = LocalKimonoShapes.current

    val typography: KimonoTypography
        @Composable @ReadOnlyComposable
        get() = LocalKimonoTypography.current
}

private const val OPAQUE = 0xFF000000

internal fun Rgb.toColor(): Color = Color(OPAQUE or value.toLong())

// Lets stock tv-material components that aren't wrapped yet pick up the palette.
private fun KimonoColors.toTvColorScheme() = if (isLight) {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        secondary = accentSecondary,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = textMuted,
        border = border,
        error = error,
    )
} else {
    darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        secondary = accentSecondary,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = textMuted,
        border = border,
        error = error,
    )
}

// Stock tv-material components keep their own sizes but switch to Kimono's font. Plain Text
// picks up bodyLarge, which is Kimono's body style.
private val TvTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = KimonoFontFamily),
        displayMedium = displayMedium.copy(fontFamily = KimonoFontFamily),
        displaySmall = displaySmall.copy(fontFamily = KimonoFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = KimonoFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = KimonoFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = KimonoFontFamily),
        titleLarge = titleLarge.copy(fontFamily = KimonoFontFamily),
        titleMedium = titleMedium.copy(fontFamily = KimonoFontFamily),
        titleSmall = titleSmall.copy(fontFamily = KimonoFontFamily),
        bodyLarge = KimonoTypography.Default.body,
        bodyMedium = bodyMedium.copy(fontFamily = KimonoFontFamily),
        bodySmall = bodySmall.copy(fontFamily = KimonoFontFamily),
        labelLarge = labelLarge.copy(fontFamily = KimonoFontFamily),
        labelMedium = labelMedium.copy(fontFamily = KimonoFontFamily),
        labelSmall = labelSmall.copy(fontFamily = KimonoFontFamily),
    )
}
