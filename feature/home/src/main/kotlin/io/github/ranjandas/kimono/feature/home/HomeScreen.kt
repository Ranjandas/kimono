package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.AppOptionsPanel
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import io.github.ranjandas.kimono.data.apps.forWorkspace
import java.time.Clock

/** Layout sizes, in dp on the 960 × 540 canvas. They scale the mockup by the same 1.4 × as the type scale. */
internal object HomeDimens {
    val ScreenPaddingH = 20.dp
    val ScreenPaddingV = 12.dp
    val Gap = 14.dp
    val TopBarHeight = 34.dp
    val HintBarHeight = 22.dp
    val SideColumnWidth = 392.dp
    val RecentsHeight = 140.dp
}

/**
 * The launcher's main screen: the top bar, and under it the current workspace as a page of its own (#98). Home is
 * the grid from the mockup, watch and play are grids of their apps, and all apps is the [allApps] page, the
 * drawer. Left and Right at the edge of a page switch workspace, sliding the next page in. [apps] is null while
 * the app list is still loading. The top bar reads the time from [clock], which tests fix.
 */
@Composable
fun HomeScreen(
    apps: List<InstalledApp>?,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    workspaces: WorkspaceState,
    onSelectWorkspace: (Workspace) -> Unit,
    onTogglePin: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    theme: ThemeId,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    recents: RecentApps,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.systemDefaultZone(),
    allApps: @Composable () -> Unit,
) {
    val workspace = workspaces.current
    val switcher = rememberWorkspaceSwitcher(workspace, onSelectWorkspace)
    val workspaceFocus = remember { FocusRequester() }
    var options by remember { mutableStateOf<InstalledApp?>(null) }
    var focusArea by remember { mutableStateOf(HomeArea.Apps) }
    val pageActions = PageActions(onLaunchApp, onShowOptions = { options = it }, switcher, panelOpen = options != null)
    Box(modifier = modifier.fillMaxSize()) {
        HomeLayout(workspace, switcher, topBar = { barModifier ->
            TopBar(
                workspace = workspace,
                onSelectWorkspace = switcher::select,
                theme = theme,
                onCycleTheme = onCycleTheme,
                onOpenSettings = onOpenSettings,
                clockFormat = clockFormat,
                onFocusArea = { focusArea = it },
                clock = clock,
                currentWorkspaceFocus = workspaceFocus,
                modifier = barModifier,
            )
        }) { page ->
            val pageApps = remember(apps, workspaces) { apps?.forWorkspace(page, workspaces) }
            when (page) {
                Workspace.Home -> HomePage(
                    apps = pageApps,
                    actions = pageActions,
                    recents = recents,
                    onRequestUsageAccess = onRequestUsageAccess,
                    theme = theme,
                    focusArea = focusArea,
                    onFocusArea = { focusArea = it },
                    emptyFocus = workspaceFocus,
                )

                Workspace.Watch, Workspace.Play -> AppGridPage(page, pageApps, pageActions)

                Workspace.AllApps -> allApps()
            }
        }
        options?.let {
            WorkspaceOptions(it, workspaces, onTogglePin, onOpenAppInfo, onUninstallApp, onDismiss = { options = null })
        }
    }
}

/**
 * The top bar over the current workspace's page. Number, channel and menu keys switch workspace from anywhere
 * (see [selectsWorkspace]); Left and Right at the edge of the page do it on any remote (see [WorkspacePages]).
 */
@Composable
private fun HomeLayout(
    workspace: Workspace,
    switcher: WorkspaceSwitcher,
    topBar: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    page: @Composable (Workspace) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .onPreviewKeyEvent { it.selectsWorkspace(workspace, switcher::select) }
            .padding(horizontal = HomeDimens.ScreenPaddingH, vertical = HomeDimens.ScreenPaddingV),
        verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
    ) {
        topBar(Modifier.fillMaxWidth().height(HomeDimens.TopBarHeight))
        WorkspacePages(switcher, workspace, Modifier.fillMaxWidth().weight(1f), page)
    }
}

/** The options for [app] on the current workspace; pins from all apps go to home. */
@Composable
private fun WorkspaceOptions(
    app: InstalledApp,
    workspaces: WorkspaceState,
    onTogglePin: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
) {
    val target = workspaces.current.pinTarget
    AppOptionsPanel(
        appLabel = app.label,
        workspaceLabel = target.label,
        pinned = app.packageName in workspaces.pinsOf(target),
        onTogglePin = { onTogglePin(app) },
        onOpenAppInfo = { onOpenAppInfo(app) },
        onUninstall = { onUninstallApp(app) },
        onDismiss = onDismiss,
    )
}

/**
 * What a page of apps does beyond showing them: open one, show its options, go to all apps or home. While
 * [panelOpen] the options panel has focus; a page takes focus back when it closes.
 */
internal class PageActions(
    val onLaunchApp: (InstalledApp) -> LaunchResult,
    val onShowOptions: (InstalledApp) -> Unit,
    private val switcher: WorkspaceSwitcher,
    val panelOpen: Boolean,
) {
    fun onOpenAllApps() = switcher.select(Workspace.AllApps)

    fun onGoHome() = switcher.select(Workspace.Home)
}

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
            onTogglePin = {},
            onOpenAppInfo = {},
            onUninstallApp = {},
            theme = ThemeId.Default,
            onCycleTheme = {},
            onOpenSettings = {},
            clockFormat = ClockFormat.System,
            recents = RecentApps.NeedsAccess,
            onRequestUsageAccess = {},
            allApps = { Text("all apps") },
        )
    }
}

/** Tells the hint bar when focus moves into [area], or anywhere inside it. */
internal fun Modifier.tracksFocus(area: HomeArea, onFocusArea: (HomeArea) -> Unit): Modifier =
    onFocusChanged { if (it.hasFocus) onFocusArea(area) }
