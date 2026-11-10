package io.github.ranjandas.kimono.data.apps

import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState

/**
 * The apps a workspace lists: its pins first, in pin order, then the apps that belong to it by category
 * (decision O6). Home and all apps list everything, so every app is reachable without pinning anything.
 */
fun List<InstalledApp>.forWorkspace(workspace: Workspace, state: WorkspaceState): List<InstalledApp> {
    val pinned = state.pinsOf(workspace).mapNotNull { pkg -> firstOrNull { it.packageName == pkg } }
    val rest = filter { it !in pinned && workspace.lists(it.category) }
    return pinned + rest
}

/**
 * The apps a workspace shows without any pins. Watch is video only, like the drawer's Watch group, so both count
 * the same apps; music sits under the drawer's Listen (#165).
 */
private fun Workspace.lists(category: AppCategory): Boolean = when (this) {
    Workspace.Home, Workspace.AllApps -> true
    Workspace.Watch -> category == AppCategory.Video
    Workspace.Play -> category == AppCategory.Game
}
