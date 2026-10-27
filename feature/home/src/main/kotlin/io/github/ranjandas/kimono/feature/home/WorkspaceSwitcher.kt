package io.github.ranjandas.kimono.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import io.github.ranjandas.kimono.core.data.workspace.Workspace

/**
 * Switches workspaces and remembers which way the last switch went, so the next page slides in from that side.
 * Going past the last workspace wraps to the first, and back.
 */
@Stable
internal class WorkspaceSwitcher(current: Workspace, private val onSelect: (Workspace) -> Unit) {
    var current: Workspace = current
        internal set

    /** True when the last switch moved right (to a later workspace, or by wrapping past the last). */
    var forward by mutableStateOf(true)
        private set

    fun next() = go(current.next(), forward = true)

    fun previous() = go(current.previous(), forward = false)

    /** Jumps straight to [target], sliding the way the numbers go. */
    fun select(target: Workspace) = go(target, forward = target.ordinal >= current.ordinal)

    private fun go(target: Workspace, forward: Boolean) {
        if (target == current) return
        this.forward = forward
        onSelect(target)
    }

    /**
     * Handles Left and Right inside a page: they move focus as usual, and where focus can't go any further that
     * way (the edge of the screen) they switch to the previous or next workspace. A held key stops at the edge;
     * only a fresh press switches, so holding Left along a row never flies through the workspaces.
     */
    fun onPageKey(event: KeyEvent, focusManager: FocusManager): Boolean {
        val direction = when (event.key) {
            Key.DirectionLeft -> FocusDirection.Left
            Key.DirectionRight -> FocusDirection.Right
            else -> return false
        }
        if (event.type != KeyEventType.KeyDown) return false
        if (!focusManager.moveFocus(direction) && event.nativeKeyEvent.repeatCount == 0) {
            if (direction == FocusDirection.Left) previous() else next()
        }
        return true
    }
}

@Composable
internal fun rememberWorkspaceSwitcher(current: Workspace, onSelect: (Workspace) -> Unit): WorkspaceSwitcher {
    val select by rememberUpdatedState(onSelect)
    val switcher = remember { WorkspaceSwitcher(current) { select(it) } }
    switcher.current = current
    return switcher
}

/**
 * The current workspace's page, sliding in from the side the switch went to. Left and Right never leave the page
 * for the top bar: at the edge of the page they switch workspace instead (see [WorkspaceSwitcher.onPageKey]).
 */
@Composable
internal fun WorkspacePages(
    switcher: WorkspaceSwitcher,
    workspace: Workspace,
    modifier: Modifier = Modifier,
    page: @Composable (Workspace) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    AnimatedContent(
        targetState = workspace,
        modifier = modifier
            .onKeyEvent { switcher.onPageKey(it, focusManager) }
            .focusProperties {
                onExit = { if (requestedFocusDirection in Sideways) cancelFocusChange() }
            }
            .focusGroup(),
        transitionSpec = {
            val side = if (switcher.forward) 1 else -1
            val enter = slideInHorizontally(tween(SLIDE_MILLIS)) { side * it / SLIDE_FRACTION }
            val exit = slideOutHorizontally(tween(SLIDE_MILLIS)) { -side * it / SLIDE_FRACTION }
            (enter + fadeIn(tween(SLIDE_MILLIS))) togetherWith (exit + fadeOut(tween(SLIDE_MILLIS)))
        },
        label = "workspace",
    ) { target ->
        Box { page(target) }
    }
}

private val Sideways = setOf(FocusDirection.Left, FocusDirection.Right)

private const val SLIDE_MILLIS = 220

// Pages slide a quarter of the screen while they fade, which reads as a move without a long sweep.
private const val SLIDE_FRACTION = 4
