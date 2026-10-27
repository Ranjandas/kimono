package io.github.ranjandas.kimono.feature.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.ranjandas.kimono.core.data.workspace.Workspace

/**
 * Jumps to a workspace on the number keys 1 to 4, to the next or previous one on channel up and down, and to all
 * apps on the menu or search key, wherever focus is. Returns true when the key was a workspace shortcut, so the
 * focused tile never also sees it, and that includes all apps' search: a remote's number keys are for switching.
 * Few remotes have these keys; the screen edges (see [WorkspaceSwitcher]) are the way every remote has.
 */
internal fun KeyEvent.selectsWorkspace(current: Workspace, onSelectWorkspace: (Workspace) -> Unit): Boolean {
    val target = if (type == KeyEventType.KeyDown) workspaceShortcut(current) else null
    target?.let(onSelectWorkspace)
    return target != null
}

private fun KeyEvent.workspaceShortcut(current: Workspace): Workspace? = when (key) {
    Key.ChannelUp -> current.next()
    Key.ChannelDown -> current.previous()
    Key.Menu, Key.Search -> Workspace.AllApps
    else -> NumberKeys[key]
}

private val NumberKeys = mapOf(
    Key.One to Workspace.Home,
    Key.NumPad1 to Workspace.Home,
    Key.Two to Workspace.Watch,
    Key.NumPad2 to Workspace.Watch,
    Key.Three to Workspace.Play,
    Key.NumPad3 to Workspace.Play,
    Key.Four to Workspace.AllApps,
    Key.NumPad4 to Workspace.AllApps,
)
