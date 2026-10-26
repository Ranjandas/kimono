package io.github.ranjandas.kimono.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The options for one app, the same from the home list and the drawer: pin it to the workspace being
 * looked at (or take the pin off), open its page in Settings, or uninstall it. Choosing anything closes
 * the panel, so the list underneath is never left behind a stale panel.
 *
 * It takes labels rather than app and workspace types, so the design module stays free of data types.
 */
@Composable
fun AppOptionsPanel(
    appLabel: String,
    workspaceLabel: String,
    pinned: Boolean,
    onTogglePin: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        Option(
            label = if (pinned) "unpin from $workspaceLabel" else "pin to $workspaceLabel",
            onSelect = {
                onTogglePin()
                onDismiss()
            },
        ),
        Option(label = "app info", detail = "settings", onSelect = {
            onOpenAppInfo()
            onDismiss()
        }),
        Option(label = "uninstall", detail = "asks first", onSelect = {
            onUninstall()
            onDismiss()
        }),
    )
    OptionsPanel(title = appLabel, options = options, onDismiss = onDismiss, modifier = modifier)
}
