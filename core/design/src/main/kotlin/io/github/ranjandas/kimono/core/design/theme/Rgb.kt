// The numbers here are sRGB channel masks and the WCAG 2.x luminance and contrast formulas.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.design.theme

import kotlin.math.pow
import kotlin.math.roundToInt

/** An opaque sRGB colour, kept free of Android and Compose so palettes and contrast checks are plain Kotlin. */
@JvmInline
value class Rgb(val value: Int) {
    val red: Int get() = (value shr 16) and 0xFF
    val green: Int get() = (value shr 8) and 0xFF
    val blue: Int get() = value and 0xFF

    /** Relative luminance as defined by WCAG 2.x. */
    val luminance: Double
        get() = 0.2126 * linear(red) + 0.7152 * linear(green) + 0.0722 * linear(blue)

    /** Mixes towards [other] by [fraction] (0 = this colour, 1 = [other]), channel by channel in sRGB. */
    fun mix(other: Rgb, fraction: Double): Rgb {
        fun channel(a: Int, b: Int) = (a + (b - a) * fraction).roundToInt().coerceIn(0, 0xFF)
        return Rgb((channel(red, other.red) shl 16) or (channel(green, other.green) shl 8) or channel(blue, other.blue))
    }

    override fun toString(): String = "#%06x".format(value)

    companion object {
        /** Parses `#rrggbb`, the format used by Omarchy's `colors.toml`. */
        fun of(hex: String): Rgb {
            require(hex.length == 7 && hex[0] == '#') { "Expected #rrggbb, got $hex" }
            return Rgb(hex.substring(1).toInt(16))
        }

        private fun linear(channel: Int): Double {
            val c = channel / 255.0
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
    }
}

/** WCAG contrast ratio between two colours, from 1.0 (identical) to 21.0 (black on white). */
fun contrastRatio(a: Rgb, b: Rgb): Double {
    val lighter = maxOf(a.luminance, b.luminance)
    val darker = minOf(a.luminance, b.luminance)
    return (lighter + 0.05) / (darker + 0.05)
}

/** Minimum contrast for body text (WCAG AA), PLAN design principle 7. */
const val MIN_TEXT_CONTRAST = 4.5

/**
 * The colour closest to [background] on the way from [foreground] that still keeps [minContrast] against it:
 * as quiet as possible while staying legible. Returns [foreground] itself if even that falls short.
 */
fun quietestLegible(foreground: Rgb, background: Rgb, minContrast: Double = MIN_TEXT_CONTRAST): Rgb {
    val steps = 100
    return (steps downTo 0)
        .asSequence()
        .map { foreground.mix(background, it / steps.toDouble()) }
        .firstOrNull { contrastRatio(it, background) >= minContrast }
        ?: foreground
}

/** Minimum contrast for focus indicators and other UI that has to be seen (WCAG 1.4.11). */
const val MIN_UI_CONTRAST = 3.0

/**
 * The first colour on the way from this one to [target] that [passes], or [target] if none does. Used to nudge a
 * palette colour just far enough to pass a contrast check while keeping its hue.
 */
fun Rgb.nudgedTowards(target: Rgb, passes: (Rgb) -> Boolean): Rgb {
    val steps = 100
    return (0..steps)
        .asSequence()
        .map { mix(target, it / steps.toDouble()) }
        .firstOrNull(passes)
        ?: target
}

/**
 * The palette's accent, nudged towards its foreground where needed so that it works as a focus border on tiles
 * and the screen ([MIN_UI_CONTRAST]) and as a fill under [Palette.background]-coloured text ([MIN_TEXT_CONTRAST]).
 */
fun legibleAccent(palette: Palette): Rgb = palette.accent.nudgedTowards(palette.foreground) {
    contrastRatio(it, palette.surface) >= MIN_UI_CONTRAST &&
        contrastRatio(it, palette.background) >= MIN_TEXT_CONTRAST
}
