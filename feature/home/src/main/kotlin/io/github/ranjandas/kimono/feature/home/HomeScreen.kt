package io.github.ranjandas.kimono.feature.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.KeyHint
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import io.github.ranjandas.kimono.data.apps.forWorkspace

/** Layout sizes, in dp on the 960 × 540 canvas. They scale the mockup by the same 1.4 × as the type scale. */
private object HomeDimens {
    val ScreenPaddingH = 20.dp
    val ScreenPaddingV = 12.dp
    val Gap = 14.dp
    val TopBarHeight = 34.dp
    val HintBarHeight = 22.dp
    val SideColumnWidth = 392.dp
    val RecentsHeight = 140.dp
}

/**
 * The home screen grid from the mockup: continue watching and recent apps on the left, system info and the apps
 * list on the right, the top bar above and key hints below. Tiles hold placeholders until their data lands
 * (continue watching in M3, recents in M2).
 */
@Composable
fun HomeScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    workspaces: WorkspaceState,
    onSelectWorkspace: (Workspace) -> Unit,
    theme: ThemeId,
    onCycleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val workspace = workspaces.current
    val workspaceApps = remember(apps, workspace, workspaces) { apps.forWorkspace(workspace, workspaces) }
    val appsFocus = remember { FocusRequester() }
    val hasApps = workspaceApps.isNotEmpty()
    // Start on the apps list: it's the one tile with something to do until continue watching lands (M3).
    LaunchedEffect(hasApps) {
        if (hasApps) appsFocus.requestFocusWhenLaidOut()
    }
    // Home is the root of the launcher, so Back never leaves it; it returns focus to the start position.
    BackHandler { runCatching { appsFocus.requestFocus() } }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .onPreviewKeyEvent { event -> event.selectsWorkspace(workspace, onSelectWorkspace) }
            .padding(horizontal = HomeDimens.ScreenPaddingH, vertical = HomeDimens.ScreenPaddingV),
        verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
    ) {
        TopBar(
            workspace = workspace,
            onSelectWorkspace = onSelectWorkspace,
            theme = theme,
            onCycleTheme = onCycleTheme,
            modifier = Modifier.fillMaxWidth().height(HomeDimens.TopBarHeight),
        )
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
            ) {
                ContinueTile(Modifier.fillMaxWidth().weight(1f))
                RecentsRow(Modifier.fillMaxWidth().height(HomeDimens.RecentsHeight))
            }
            Column(
                modifier = Modifier.width(HomeDimens.SideColumnWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
            ) {
                SystemInfoTile(Modifier.fillMaxWidth())
                AppsTile(
                    apps = workspaceApps,
                    onLaunchApp = onLaunchApp,
                    workspace = workspace,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    listFocusRequester = appsFocus,
                )
            }
        }
        HintBar(Modifier.fillMaxWidth().height(HomeDimens.HintBarHeight))
    }
}

@Composable
private fun ContinueTile(modifier: Modifier = Modifier) {
    // Focusable now so D-pad moves stay the same once it gets resume and details buttons in M3.
    Tile(modifier = modifier, onClick = {}) {
        SectionHeader("~/continue")
        Spacer(Modifier.weight(1f))
        Text("nothing to continue yet", style = KimonoTheme.typography.headline)
        Text(
            "shows here once Trakt is connected",
            style = KimonoTheme.typography.body,
            color = KimonoTheme.colors.textMuted,
        )
    }
}

@Composable
private fun RecentsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.focusRestorer().focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
    ) {
        repeat(RECENT_SLOTS) {
            Tile(modifier = Modifier.weight(1f).fillMaxHeight(), onClick = {}) {
                Icon(
                    KimonoIcon.AppFallback,
                    color = KimonoTheme.colors.textMuted,
                    style = KimonoTheme.typography.headline,
                )
                Spacer(Modifier.weight(1f))
                Text("recent", style = KimonoTheme.typography.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "none yet",
                    style = KimonoTheme.typography.label,
                    color = KimonoTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SystemInfoTile(modifier: Modifier = Modifier) {
    Tile(modifier = modifier) {
        SectionHeader("~/system")
        InfoLine("device", Build.MODEL)
        InfoLine("os", "Android ${Build.VERSION.RELEASE}")
        InfoLine("launcher", "kimono")
        InfoLine("font", "JetBrains Mono")
    }
}

@Composable
private fun InfoLine(key: String, value: String) {
    Row(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            key.padEnd(INFO_KEY_WIDTH),
            style = KimonoTheme.typography.body,
            color = KimonoTheme.colors.accentSecondary,
        )
        Text(value, style = KimonoTheme.typography.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HintBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyHint("◀ ▶ ▲ ▼", "move focus")
        KeyHint("ok", "launch")
        KeyHint("1…5", "workspace")
        KeyHint("hold ok", "app options")
    }
}

/**
 * Jumps to a workspace on the number keys 1 to 5, and to the next or previous one on channel up and down,
 * wherever focus is. Returns true when the key was a workspace shortcut, so the focused tile never also sees it.
 */
private fun KeyEvent.selectsWorkspace(current: Workspace, onSelectWorkspace: (Workspace) -> Unit): Boolean {
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

/** Requests focus, retrying for a few frames while a lazy list composes its first rows. */
private suspend fun FocusRequester.requestFocusWhenLaidOut() {
    repeat(FOCUS_ATTEMPTS) {
        if (runCatching { requestFocus() }.getOrDefault(false)) return
        withFrameNanos { }
    }
}

private const val FOCUS_ATTEMPTS = 10
private const val RECENT_SLOTS = 4
private const val INFO_KEY_WIDTH = 10

@Preview(device = Devices.TV_1080p)
@Composable
private fun HomeScreenPreview() {
    KimonoTheme {
        HomeScreen(
            apps = listOf("Jellyfin", "Kodi", "Plex", "YouTube").map {
                InstalledApp("pkg.$it", "pkg.$it.Main", it, AppCategory.Video, isTvApp = true)
            },
            onLaunchApp = { LaunchResult.Launched },
            workspaces = WorkspaceState(),
            onSelectWorkspace = {},
            theme = ThemeId.Default,
            onCycleTheme = {},
        )
    }
}
