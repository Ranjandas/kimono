package io.github.ranjandas.kimono.feature.home

import io.github.ranjandas.kimono.core.design.component.Hint

/**
 * The parts of the home screen where the remote keys do different things. [TvStatus] is the network and volume
 * icons, which open the TV's own settings (#200).
 */
internal enum class HomeArea {
    SettingsButton,
    TvStatus,
    LastUsed,
    Recents,
    Apps,
    ;

    /** Whether it's on the top bar rather than a page. */
    val inTopBar: Boolean get() = this == SettingsButton || this == TvStatus
}

/** The hints for the focused [area], then how to switch workspace. */
internal fun homeHints(area: HomeArea): List<Hint> = when (area) {
    HomeArea.SettingsButton -> listOf(Hint("◀ ▼", "move focus"), Hint("ok", "open settings"))

    HomeArea.TvStatus -> listOf(Hint("◀ ▶ ▼", "move focus"), Hint("ok", "open tv settings"))

    HomeArea.LastUsed, HomeArea.Recents ->
        listOf(Hint("◀ ▶ ▲ ▼", "move focus"), Hint("ok", "open"), Hint("hold ok", "app options"))

    HomeArea.Apps -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"), Hint("ok", "launch"), Hint("hold ok", "app options"))
} + EdgeHint

/**
 * Switching workspace at the edge of the screen works on every remote. Number, channel and menu keys still work
 * where a remote has them, but most don't (the Google TV remote and its phone app have none), so no hint names
 * them.
 */
internal val EdgeHint = Hint("edge ◀ ▶", "workspace")

/** The hints on watch and play: the grid of apps, Back to home and how to switch workspace. */
internal val GridHints = listOf(
    Hint("◀ ▶ ▲ ▼", "move focus"),
    Hint("ok", "launch"),
    Hint("hold ok", "app options"),
    Hint("back", "home"),
    EdgeHint,
)
