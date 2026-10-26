package io.github.ranjandas.kimono.core.data.settings

/**
 * What the settings screen changes. The theme and corner style are kept by name, since their types live in
 * `:core:design`; a name that no longer matches anything reads as the default.
 */
data class LauncherSettings(
    val themeName: String? = null,
    val cornerStyleName: String? = null,
    val clockFormat: ClockFormat = ClockFormat.System,
    val textSize: TextSize = TextSize.Default,
)

/** How the top bar shows the time. */
enum class ClockFormat(val label: String) {
    /** Whatever the device's own setting says. */
    System("system"),
    TwelveHour("12 h"),
    TwentyFourHour("24 h"),
}

/** A step on top of the type scale, for rooms where the TV is far away. Layout sizes don't change. */
enum class TextSize(val label: String, val scale: Float) {
    Default("default", 1f),
    Large("large", 1.15f),
}
