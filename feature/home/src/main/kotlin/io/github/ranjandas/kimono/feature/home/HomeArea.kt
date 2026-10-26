package io.github.ranjandas.kimono.feature.home

import io.github.ranjandas.kimono.core.design.component.Hint

/** The parts of the home screen where the remote keys do different things. */
internal enum class HomeArea { Workspaces, ThemeButton, SettingsButton, Continue, Recents, Apps }

/** The hints for the focused [area], then the shortcuts that work anywhere on home. */
internal fun homeHints(area: HomeArea): List<Hint> = when (area) {
    HomeArea.Workspaces -> listOf(Hint("◀ ▶ ▼", "move focus"), Hint("ok", "switch workspace"))

    HomeArea.ThemeButton -> listOf(Hint("◀ ▶ ▼", "move focus"), Hint("ok", "next theme"))

    HomeArea.SettingsButton -> listOf(Hint("◀ ▼", "move focus"), Hint("ok", "open settings"))

    // Continue watching is a placeholder until M3, so there's nothing to press yet.
    HomeArea.Continue -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"))

    HomeArea.Recents -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"), Hint("ok", "open"))

    HomeArea.Apps -> listOf(Hint("◀ ▶ ▲ ▼", "move focus"), Hint("ok", "launch"), Hint("hold ok", "app options"))
} + HomeShortcutHints

private val HomeShortcutHints = listOf(Hint("1…5", "workspace"), Hint("menu", "all apps"))
