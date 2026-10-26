package io.github.ranjandas.kimono.feature.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.ranjandas.kimono.core.data.workspace.Workspace

/** Opens the app drawer on the remote's menu or search key. */
internal fun KeyEvent.opensDrawer(onOpenDrawer: () -> Unit): Boolean {
    val opens = type == KeyEventType.KeyDown && (key == Key.Menu || key == Key.Search)
    if (opens) onOpenDrawer()
    return opens
}

/**
 * Jumps to a workspace on the number keys 1 to 5, and to the next or previous one on channel up and down,
 * wherever focus is. Returns true when the key was a workspace shortcut, so the focused tile never also sees it.
 */
internal fun KeyEvent.selectsWorkspace(current: Workspace, onSelectWorkspace: (Workspace) -> Unit): Boolean {
    val target = if (type == KeyEventType.KeyDown) workspaceShortcut(current) else null
    target?.let(onSelectWorkspace)
    return target != null
}

private fun KeyEvent.workspaceShortcut(current: Workspace): Workspace? = when (key) {
    Key.One, Key.NumPad1 -> Workspace.Home
    Key.Two, Key.NumPad2 -> Workspace.Media
    Key.Three, Key.NumPad3 -> Workspace.Games
    Key.Four, Key.NumPad4 -> Workspace.Apps
    Key.Five, Key.NumPad5 -> Workspace.System
    Key.ChannelUp -> current.next()
    Key.ChannelDown -> current.previous()
    else -> null
}
