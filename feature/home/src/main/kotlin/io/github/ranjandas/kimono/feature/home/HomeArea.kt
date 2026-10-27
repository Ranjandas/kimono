package io.github.ranjandas.kimono.feature.home

import io.github.ranjandas.kimono.core.design.component.Hint

/** The parts of the home screen where the remote keys do different things. */
internal enum class HomeArea { Workspaces, ThemeButton, SettingsButton, Continue, Recents, Apps }

/** The hints for the focused [area], then how to switch workspace. */
internal fun homeHints(area: HomeArea): List<Hint> = when (area) {
    HomeArea.Workspaces -> listOf(Hint("◀ ▶ ▼", "move focus"), Hint("ok", "switch workspace"))

    HomeArea.ThemeButton -> listOf(Hint("◀ ▶ ▼", "move focus"), Hint("ok", "next theme"))

    HomeArea.SettingsButton -> listOf(Hint("◀ ▼", "move focus"), Hint("ok", "open settings"))

    // Continue watching is a placeholder until M3, so there's nothing to press yet.
    HomeArea.Continue -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"))

    HomeArea.Recents -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"), Hint("ok", "open"))

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
