package io.github.ranjandas.kimono.feature.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.AppOptionsPanel
import io.github.ranjandas.kimono.core.design.component.HintBar
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
import java.time.Clock

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
 * (continue watching in M3, recents in M2). [apps] is null while the app list is still loading. The top bar
 * reads the time from [clock], which tests fix.
 */
@Composable
fun HomeScreen(
    apps: List<InstalledApp>?,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    workspaces: WorkspaceState,
    onSelectWorkspace: (Workspace) -> Unit,
    onOpenDrawer: () -> Unit,
    onTogglePin: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    theme: ThemeId,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.systemDefaultZone(),
) {
    val workspace = workspaces.current
    val workspaceApps = remember(apps, workspace, workspaces) { apps?.forWorkspace(workspace, workspaces) }
    val appsFocus = remember { FocusRequester() }
    var options by remember { mutableStateOf<InstalledApp?>(null) }
    var focusArea by remember { mutableStateOf(HomeArea.Apps) }
    val hasApps = !workspaceApps.isNullOrEmpty()
    // Start on the apps list: it's the one tile with something to do until continue watching lands (M3). Come
    // back to it when its contents change (a workspace switch) and when the options panel closes, so focus is
    // never left on whatever the system picks first.
    val panelOpen = options != null
    LaunchedEffect(hasApps, workspace, panelOpen) {
        if (hasApps && !panelOpen) appsFocus.requestFocusWhenLaidOut()
    }
    // Home is the root of the launcher, so Back never leaves it; it returns focus to the start position.
    BackHandler { runCatching { appsFocus.requestFocus() } }
    Box(modifier = modifier.fillMaxSize()) {
        HomeGrid(
            apps = workspaceApps,
            onLaunchApp = onLaunchApp,
            onShowOptions = { options = it },
            workspace = workspace,
            onSelectWorkspace = onSelectWorkspace,
            onOpenDrawer = onOpenDrawer,
            theme = theme,
            onCycleTheme = onCycleTheme,
            onOpenSettings = onOpenSettings,
            clockFormat = clockFormat,
            appsFocus = appsFocus,
            focusArea = focusArea,
            onFocusArea = { focusArea = it },
            clock = clock,
        )
        options?.let { app ->
            AppOptionsPanel(
                appLabel = app.label,
                workspaceLabel = workspace.label,
                pinned = app.packageName in workspaces.pinsOf(workspace),
                onTogglePin = { onTogglePin(app) },
                onOpenAppInfo = { onOpenAppInfo(app) },
                onUninstall = { onUninstallApp(app) },
                onDismiss = { options = null },
            )
        }
    }
}

/** The grid itself: top bar, the four tiles and the hint bar. */
@Composable
private fun HomeGrid(
    apps: List<InstalledApp>?,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    onShowOptions: (InstalledApp) -> Unit,
    workspace: Workspace,
    onSelectWorkspace: (Workspace) -> Unit,
    onOpenDrawer: () -> Unit,
    theme: ThemeId,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    appsFocus: FocusRequester,
    focusArea: HomeArea,
    onFocusArea: (HomeArea) -> Unit,
    clock: Clock,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .onPreviewKeyEvent { event ->
                event.opensDrawer(onOpenDrawer) ||
                    event.selectsWorkspace(workspace, onSelectWorkspace)
            }
            .padding(horizontal = HomeDimens.ScreenPaddingH, vertical = HomeDimens.ScreenPaddingV),
        verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
    ) {
        TopBar(
            workspace = workspace,
            onSelectWorkspace = onSelectWorkspace,
            theme = theme,
            onCycleTheme = onCycleTheme,
            onOpenSettings = onOpenSettings,
            clockFormat = clockFormat,
            onFocusArea = onFocusArea,
            clock = clock,
            modifier = Modifier.fillMaxWidth().height(HomeDimens.TopBarHeight),
        )
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().tracksFocus(HomeArea.Tiles, onFocusArea),
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
                    apps = apps,
                    onLaunchApp = onLaunchApp,
                    onShowOptions = onShowOptions,
                    workspace = workspace,
                    modifier = Modifier.fillMaxWidth().weight(1f).tracksFocus(HomeArea.Apps, onFocusArea),
                    listFocusRequester = appsFocus,
                )
            }
        }
        HintBar(homeHints(focusArea), Modifier.fillMaxWidth().height(HomeDimens.HintBarHeight))
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
            onOpenDrawer = {},
            onTogglePin = {},
            onOpenAppInfo = {},
            onUninstallApp = {},
            theme = ThemeId.Default,
            onCycleTheme = {},
            onOpenSettings = {},
            clockFormat = ClockFormat.System,
        )
    }
}

/** Tells the hint bar when focus moves into [area], or anywhere inside it. */
internal fun Modifier.tracksFocus(area: HomeArea, onFocusArea: (HomeArea) -> Unit): Modifier =
    onFocusChanged { if (it.hasFocus) onFocusArea(area) }
