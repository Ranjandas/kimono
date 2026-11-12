package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
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
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import java.time.Clock
import kotlinx.coroutines.delay

/**
 * Workspaces on the left, date and time in the middle, network, volume and settings on the right. The theme is
 * changed in settings (#110). Like Omarchy's Waybar, the right side is icons only (#123): the volume shows its
 * level only while it changes.
 * Status values update live. [settingsFocus] focuses the gear.
 *
 * The workspace numbers only show where you are (#219): they never take focus, so Up from any page goes straight to
 * the gear. Left and Right at the edge of a page switch workspace, and the highlight follows.
 */
@Composable
internal fun TopBar(
    workspace: Workspace,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    onFocusArea: (HomeArea) -> Unit,
    clock: Clock,
    settingsFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.XS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Workspace.entries.forEach { candidate ->
                WorkspaceNumber(workspace = candidate, selected = candidate == workspace)
            }
            Icon(
                workspace.icon,
                modifier = Modifier.padding(start = KimonoSpacing.M),
                color = KimonoTheme.colors.textMuted,
                style = KimonoTheme.typography.label,
            )
            Text(
                workspace.label.uppercase(),
                // TalkBack says the new workspace on each switch, since focus stays on the page.
                modifier = Modifier.semantics {
                    contentDescription = workspace.label
                    liveRegion = LiveRegionMode.Polite
                },
                style = KimonoTheme.typography.section,
                color = KimonoTheme.colors.textMuted,
            )
        }
        DateTime(clockFormat, clock, Modifier.align(Alignment.Center))
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.L),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkIndicator()
            VolumeIndicator()
            IconButton(
                icon = KimonoIcon.Settings,
                description = "Settings",
                onClick = onOpenSettings,
                modifier = Modifier.focusRequester(settingsFocus).tracksFocus(HomeArea.SettingsButton, onFocusArea),
            )
        }
    }
}

/** A workspace's number, filled in when it is the current one. It's a tab for TalkBack, but never takes focus. */
@Composable
private fun WorkspaceNumber(workspace: Workspace, selected: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    Box(
        modifier = modifier
            .size(30.dp)
            .semantics {
                contentDescription = "Workspace ${workspace.number}, ${workspace.label}"
                role = Role.Tab
                this.selected = selected
            }
            .background(if (selected) colors.accent else Color.Transparent, KimonoTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            workspace.number.toString(),
            style = KimonoTheme.typography.value,
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
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(locale) { topBarDateFormat(locale) }
    val time = remember(locale, twentyFourHour) { topBarTimeFormat(locale, twentyFourHour) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(now.format(date), style = KimonoTheme.typography.value, color = KimonoTheme.colors.textMuted)
        Text(now.format(time), style = KimonoTheme.typography.value, fontWeight = FontWeight.Bold)
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
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.label)
        if (label != null) Text(label, style = KimonoTheme.typography.value)
    }
}

/** A plain icon that shows focus with a border. */
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
