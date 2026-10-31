package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.design.component.TileDefaults
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import java.time.Clock
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

/**
 * Workspaces on the left, date and time in the middle, network, volume and settings on the right. The theme is
 * changed in settings (#110). Like Omarchy's Waybar, the right side is icons only (#123): the volume shows its
 * level only while it changes.
 * Status values update live. [currentWorkspaceFocus] focuses the current workspace's button.
 */
@Composable
internal fun TopBar(
    workspace: Workspace,
    onSelectWorkspace: (Workspace) -> Unit,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    onFocusArea: (HomeArea) -> Unit,
    clock: Clock,
    currentWorkspaceFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .tracksFocus(HomeArea.Workspaces, onFocusArea)
                .focusRestorer()
                .focusGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Workspace.entries.forEach { candidate ->
                WorkspaceButton(
                    workspace = candidate,
                    selected = candidate == workspace,
                    onClick = { onSelectWorkspace(candidate) },
                    modifier = if (candidate == workspace) Modifier.focusRequester(currentWorkspaceFocus) else Modifier,
                )
            }
            Icon(
                workspace.icon,
                modifier = Modifier.padding(start = 10.dp),
                color = KimonoTheme.colors.textMuted,
                style = KimonoTheme.typography.label,
            )
            Text(
                workspace.label,
                style = KimonoTheme.typography.label,
                color = KimonoTheme.colors.textMuted,
            )
        }
        DateTime(clockFormat, clock, Modifier.align(Alignment.Center))
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkIndicator()
            VolumeIndicator()
            IconButton(
                icon = KimonoIcon.Settings,
                description = "Settings",
                onClick = onOpenSettings,
                modifier = Modifier.tracksFocus(HomeArea.SettingsButton, onFocusArea),
            )
        }
    }
}

@Composable
private fun WorkspaceButton(
    workspace: Workspace,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.small
    Box(
        modifier = modifier
            .size(30.dp)
            .semantics {
                contentDescription = "Workspace ${workspace.number}, ${workspace.label}"
                this.selected = selected
            }
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Tab, onClick = onClick)
            .background(if (selected) colors.accent else Color.Transparent, shape)
            .border(TileDefaults.BorderWidth, if (focused) colors.text else Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            workspace.number.toString(),
            style = KimonoTheme.typography.label,
            color = if (selected) colors.onAccent else colors.textMuted,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DateTime(format: ClockFormat, clock: Clock, modifier: Modifier = Modifier) {
    val now by rememberClock(clock)
    val twentyFourHour = when (format) {
        ClockFormat.System -> is24HourClock()
        ClockFormat.TwelveHour -> false
        ClockFormat.TwentyFourHour -> true
    }
    val time = DateTimeFormatter.ofPattern(if (twentyFourHour) "HH:mm" else "h:mm a")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(now.format(DATE), style = KimonoTheme.typography.label, color = KimonoTheme.colors.textMuted)
        Text(now.format(time), style = KimonoTheme.typography.label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NetworkIndicator(modifier: Modifier = Modifier) {
    val status by rememberNetworkStatus()
    StatusItem(status.icon, label = null, description = "Network, ${status.label}", modifier = modifier)
}

@Composable
private fun VolumeIndicator(modifier: Modifier = Modifier) {
    val volume by rememberVolumePercent()
    volume?.let { VolumeLevel(it, modifier) }
}

/** The volume icon, with [level] beside it for [VOLUME_SHOWN_MS] after it changes. */
@Composable
private fun VolumeLevel(level: Int, modifier: Modifier = Modifier) {
    var last by remember { mutableIntStateOf(level) }
    var changing by remember { mutableStateOf(false) }
    LaunchedEffect(level) {
        if (level != last || changing) {
            last = level
            changing = true
            delay(VOLUME_SHOWN_MS)
            changing = false
        }
    }
    StatusItem(
        icon = if (level == 0) KimonoIcon.VolumeOff else KimonoIcon.Volume,
        label = if (changing) "$level%" else null,
        description = "Volume $level%",
        modifier = modifier,
    )
}

@Composable
private fun StatusItem(icon: KimonoIcon, label: String?, description: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.label)
        if (label != null) Text(label, style = KimonoTheme.typography.label)
    }
}

/** A plain icon that takes focus the same way the workspace numbers do. */
@Composable
private fun IconButton(icon: KimonoIcon, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.small
    Box(
        modifier = modifier
            .size(30.dp)
            .semantics { contentDescription = description }
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .border(TileDefaults.BorderWidth, if (focused) colors.text else Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, color = if (focused) colors.text else colors.textMuted, style = KimonoTheme.typography.label)
    }
}

private const val VOLUME_SHOWN_MS = 3_000L

private val DATE = DateTimeFormatter.ofPattern("EEE d MMM")
