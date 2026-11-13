package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
 *
 * With [onOpenTvSettings], Left from the gear reaches the volume and network icons, which open the TV's sound and
 * network settings (#200), since covering Google TV's home hides its quick settings. They only take focus while the
 * gear or one of them has it, so Up from a page still lands on the gear.
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
    onOpenTvSettings: ((TvSetting) -> Unit)? = null,
) {
    var barFocused by remember { mutableStateOf(false) }
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
            modifier = Modifier.align(Alignment.CenterEnd).onFocusChanged { barFocused = it.hasFocus },
            horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shortcut = onOpenTvSettings?.let { open ->
                StatusShortcut(canFocus = barFocused, onOpen = open, onFocus = { onFocusArea(HomeArea.TvStatus) })
            }
            NetworkIndicator(shortcut)
            VolumeIndicator(shortcut)
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
private fun NetworkIndicator(shortcut: StatusShortcut?, modifier: Modifier = Modifier) {
    val status by rememberNetworkStatus()
    StatusItem(
        status.icon,
        label = null,
        description = "Network, ${status.label}",
        modifier = modifier,
        onClick = shortcut?.opening(TvSetting.Network),
        canFocus = shortcut?.canFocus ?: false,
        onFocus = shortcut?.onFocus,
    )
}

@Composable
private fun VolumeIndicator(shortcut: StatusShortcut?, modifier: Modifier = Modifier) {
    val volume by rememberVolumePercent()
    volume?.let { VolumeLevel(it, shortcut, modifier) }
}

/** The volume icon, with [level] beside it for [VOLUME_SHOWN_MS] after it changes. */
@Composable
private fun VolumeLevel(level: Int, shortcut: StatusShortcut?, modifier: Modifier = Modifier) {
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
        onClick = shortcut?.opening(TvSetting.Sound),
        canFocus = shortcut?.canFocus ?: false,
        onFocus = shortcut?.onFocus,
    )
}

/**
 * A status icon, with [label] beside it. With [onClick] it's a button that shows focus with a border like the gear's,
 * and takes focus only while [canFocus]; [onFocus] hears when it does.
 */
@Composable
@Suppress("LongParameterList") // A status item is plain or a button; splitting it would duplicate the drawing.
private fun StatusItem(
    icon: KimonoIcon,
    label: String?,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    canFocus: Boolean = false,
    onFocus: (() -> Unit)? = null,
) {
    // Read from the focus state, not the click's interactions: focus given in the screen's first frame (back from
    // settings) can land before anything collects those, and the ring would never draw (#226).
    var focused by remember { mutableStateOf(false) }
    val colors = KimonoTheme.colors
    val button = if (onClick == null) {
        Modifier
    } else {
        Modifier
            .focusProperties { this.canFocus = canFocus }
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocus?.invoke()
            }
            .clickable(indication = null, interactionSource = null, role = Role.Button, onClick = onClick)
            .border(TileDefaults.BorderWidth, if (focused) colors.text else Color.Transparent, KimonoTheme.shapes.small)
    }
    Row(
        modifier = modifier
            .then(button)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .heightIn(min = 30.dp)
            .padding(horizontal = KimonoSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The description says it all, so TalkBack doesn't also read the glyph or the level.
        val silent = Modifier.clearAndSetSemantics {}
        Icon(
            icon,
            modifier = silent,
            color = if (focused) colors.text else colors.textMuted,
            style = KimonoTheme.typography.label,
        )
        if (label != null) Text(label, modifier = silent, style = KimonoTheme.typography.value)
    }
}

/** What the status icons open, and whether they may take focus now. */
private class StatusShortcut(val canFocus: Boolean, val onOpen: (TvSetting) -> Unit, val onFocus: () -> Unit) {
    fun opening(setting: TvSetting): () -> Unit = { onOpen(setting) }
}

/** A plain icon that shows focus with a border. */
@Composable
private fun IconButton(icon: KimonoIcon, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // From the focus state, like the status icons, so the ring draws even when focus lands in the first frame (#226).
    var focused by remember { mutableStateOf(false) }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.small
    Box(
        modifier = modifier
            .size(30.dp)
            .semantics { contentDescription = description }
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = null, role = Role.Button, onClick = onClick)
            .border(TileDefaults.BorderWidth, if (focused) colors.text else Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, color = if (focused) colors.text else colors.textMuted, style = KimonoTheme.typography.label)
    }
}

private const val VOLUME_SHOWN_MS = 3_000L
