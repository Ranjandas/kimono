package io.github.ranjandas.kimono.core.data.settings

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * What the settings screen changes. The theme and corner style are kept by name, since their types live in
 * `:core:design`; a name that no longer matches anything reads as the default.
 */
data class LauncherSettings(
    val themeName: String? = null,
    val cornerStyleName: String? = null,
    val clockFormat: ClockFormat = ClockFormat.System,
    val textSize: TextSize = TextSize.Default,
    val idleTimeout: IdleTimeout = IdleTimeout.Default,
)

/** How the top bar shows the time. */
enum class ClockFormat(val label: String) {
    /** Whatever the device's own setting says. */
    System("system"),
    TwelveHour("12 h"),
    TwentyFourHour("24 h"),
}

/** How long without a key press before the screen goes idle and dims. */
enum class IdleTimeout(val label: String, val duration: Duration) {
    FifteenSeconds("15 s", 15.seconds),
    ThirtySeconds("30 s", 30.seconds),
    FortyFiveSeconds("45 s", 45.seconds),
    OneMinute("1 min", 1.minutes),
    TwoMinutes("2 min", 2.minutes),
    FiveMinutes("5 min", 5.minutes),
    Off("off", Duration.INFINITE),
    ;

    companion object {
        val Default = FortyFiveSeconds
    }
}

/** A step on top of the type scale, for rooms where the TV is far away. Layout sizes don't change. */
enum class TextSize(val label: String, val scale: Float) {
    Default("default", 1f),
    Large("large", 1.15f),
}
