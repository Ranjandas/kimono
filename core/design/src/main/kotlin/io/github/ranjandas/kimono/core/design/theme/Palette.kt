package io.github.ranjandas.kimono.core.design.theme

/**
 * A colour palette, ported from an Omarchy theme's `colors.toml`.
 *
 * Screens never read these directly; [KimonoColors] maps them to semantic tokens.
 */
data class Palette(
    val isLight: Boolean,
    /** Screen background (`background`). */
    val background: Rgb,
    /** Tile background (`lighter_background`). */
    val surface: Rgb,
    /** Selected rows and pressed states (`selection`). */
    val surfaceVariant: Rgb,
    /** Body text (`foreground`). */
    val foreground: Rgb,
    /**
     * The theme's dim text colour (`dark_foreground`). Too faint for text on most palettes, so
     * [KimonoColors.textMuted] is derived from [foreground] instead.
     */
    val mutedForeground: Rgb,
    /** Unfocused tile borders and dividers (`muted`). */
    val line: Rgb,
    /** Focus and primary highlights (`accent`). */
    val accent: Rgb,
    /** Headers and secondary highlights (`magenta`). */
    val accent2: Rgb,
    val red: Rgb,
    val yellow: Rgb,
    val green: Rgb,
    val cyan: Rgb,
    val blue: Rgb,
    val magenta: Rgb,
)
