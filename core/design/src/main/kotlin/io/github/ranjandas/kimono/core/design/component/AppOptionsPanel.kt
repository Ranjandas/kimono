package io.github.ranjandas.kimono.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/**
 * The options for one app, the same from every page: pin it to any workspace or take a pin off ([pins], toggled
 * by index through [onTogglePin]), move its pin along the page it's on ([onMoveUp] and [onMoveDown], null where
 * it can't move that way), open its page in Settings, or uninstall it. Choosing anything closes the panel, so the
 * page underneath is never left behind a stale panel.
 *
 * It takes labels rather than app and workspace types, so the design module stays free of data types.
 */
@Composable
fun AppOptionsPanel(
    appLabel: String,
    pins: List<PinChoice>,
    onTogglePin: (Int) -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    fun choose(action: () -> Unit): () -> Unit = {
        action()
        onDismiss()
    }
    val pinOptions = pins.mapIndexed { index, choice ->
        Option(
            label = if (choice.pinned) "unpin from ${choice.workspaceLabel}" else "pin to ${choice.workspaceLabel}",
            onSelect = choose { onTogglePin(index) },
        )
    }
    val moveOptions = listOfNotNull(
        onMoveUp?.let { Option(label = "move up", detail = "earlier", onSelect = choose(it)) },
        onMoveDown?.let { Option(label = "move down", detail = "later", onSelect = choose(it)) },
    )
    val appOptions = listOf(
        Option(label = "app info", detail = "settings", onSelect = choose(onOpenAppInfo)),
        Option(label = "uninstall", detail = "asks first", onSelect = choose(onUninstall)),
    )
    OptionsPanel(
        title = appLabel,
        options = pinOptions + moveOptions + appOptions,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

/** One workspace an app can be pinned to, by its label, and whether it is pinned there now. */
@Immutable
data class PinChoice(val workspaceLabel: String, val pinned: Boolean)
