package io.github.ranjandas.kimono.feature.settings

import java.time.LocalDate

/**
 * The small caps line under the settings title, like Omarchy's "Audio / STEADY GROOVE" (#157): a calm,
 * Japanese-flavoured phrase that changes once a day rather than on every open, so it feels like a mood, not noise.
 */
object PanelSubtitle {
    /** Lowercase; the header sets them in caps. */
    val phrases: List<String> = listOf(
        "quiet tatami",
        "paper lantern",
        "steady kettle",
        "moss garden",
        "slow tide",
        "folded crane",
        "cedar shade",
        "plum blossom",
        "rain on shoji",
        "warm hearth",
        "low moon",
        "ink and brush",
        "still pond",
        "evening bell",
    )

    /** The phrase for [day]: the same all day, the next one tomorrow. */
    fun forDay(day: LocalDate): String = phrases[Math.floorMod(day.toEpochDay(), phrases.size.toLong()).toInt()]
}
