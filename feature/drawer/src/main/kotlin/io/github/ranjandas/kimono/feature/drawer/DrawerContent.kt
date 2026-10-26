package io.github.ranjandas.kimono.feature.drawer

import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp

/** The drawer's groups, in the order they are listed (PLAN §5). */
enum class DrawerGroup(val label: String) {
    Watch("watch"),
    Listen("listen"),
    Play("play"),
    System("system"),
    ;

    companion object {
        fun of(category: AppCategory): DrawerGroup = when (category) {
            AppCategory.Video -> Watch
            AppCategory.Audio -> Listen
            AppCategory.Game -> Play
            AppCategory.Image, AppCategory.News, AppCategory.Other -> System
        }
    }
}

/** One group of the drawer list, with at least one app in it. */
data class DrawerSection(val group: DrawerGroup, val apps: List<InstalledApp>)

/**
 * Groups [apps] for the drawer, keeping only those matching [query]. Apps keep the order they come in
 * (by label), and a group with nothing in it is left out so the list never shows an empty heading.
 */
fun drawerSections(apps: List<InstalledApp>, query: String = ""): List<DrawerSection> {
    val matching = apps.filter { it.matches(query) }
    return DrawerGroup.entries.mapNotNull { group ->
        matching.filter { DrawerGroup.of(it.category) == group }
            .takeIf { it.isNotEmpty() }
            ?.let { DrawerSection(group, it) }
    }
}

/**
 * Matches the way a search box on a remote has to: a letter typed is a letter anywhere in the name,
 * ignoring case, and spaces in the query are ignored so "appletv" finds "Apple TV".
 */
internal fun InstalledApp.matches(query: String): Boolean {
    val needle = query.filterNot { it.isWhitespace() }.lowercase()
    return needle.isEmpty() || label.filterNot { it.isWhitespace() }.lowercase().contains(needle)
}
