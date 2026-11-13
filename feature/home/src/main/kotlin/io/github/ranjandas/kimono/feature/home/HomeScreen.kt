package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import io.github.ranjandas.kimono.core.data.icons.IconStyle
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.SystemPanel
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.AppMarks
import io.github.ranjandas.kimono.core.design.component.AppOptionsPanel
import io.github.ranjandas.kimono.core.design.component.PinChoice
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import io.github.ranjandas.kimono.data.apps.forWorkspace
import java.time.Clock

/** Layout sizes, in dp on the 960 × 540 canvas. They scale the mockup by the same 1.4 × as the type scale. */
internal object HomeDimens {
    val ScreenPaddingH = KimonoSpacing.XL
    val ScreenPaddingV = KimonoSpacing.M
    val Gap = KimonoSpacing.L

    /** The top bar panel (#122). */
    val TopBarHeight = 40.dp

    /** The top bar's inset inside its panel, slimmer than a tile's so its buttons fill the height. */
    val TopBarPaddingH = KimonoSpacing.M
    val HintBarHeight = 22.dp
    val SideColumnWidth = 392.dp
    val RecentsHeight = 140.dp
}

/**
 * The launcher's main screen: the top bar, and under it the current workspace as a page of its own (#98). Home is
 * the grid from the mockup, watch and play are grids of their apps, and all apps is the [allApps] page, the
 * drawer. Left and Right at the edge of a page switch workspace, sliding the next page in. [apps] is null while
 * the app list is still loading. The top bar reads the time from [clock], which tests fix. Coming back from settings,
 * [startOnSettingsButton] puts focus back on the gear it was opened from. While [holdFocus], something drawn over the
 * screen (the first-run welcome) has focus, so the page doesn't take it; it takes it back once that closes.
 * [onOpenTvSettings] makes the top bar's network and volume icons open the TV's own settings (#200), or is null to
 * leave them as plain indicators.
 */
@Composable
fun HomeScreen(
    apps: List<InstalledApp>?,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    workspaces: WorkspaceState,
    onSelectWorkspace: (Workspace) -> Unit,
    onTogglePin: (InstalledApp, Workspace) -> Unit,
    onMovePin: (InstalledApp, Workspace, Int) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    onOpenSettings: () -> Unit,
    clockFormat: ClockFormat,
    recents: RecentApps,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
    marks: AppMarks = AppMarks.None,
    systemPanel: SystemPanel = SystemPanel.Default,
    screenTime: ScreenTimeToday = ScreenTimeToday.Apps(emptyList()),
    iconStyles: Map<String, IconStyle> = emptyMap(),
    onChangeIconStyle: ((InstalledApp, IconStyle) -> Unit)? = null,
    clock: Clock = Clock.systemDefaultZone(),
    startOnSettingsButton: Boolean = false,
    holdFocus: Boolean = false,
    onOpenTvSettings: ((TvSetting) -> Unit)? = null,
    allApps: @Composable () -> Unit,
) {
    val workspace = workspaces.current
    val switcher = rememberWorkspaceSwitcher(workspace, onSelectWorkspace)
    val settingsFocus = remember { FocusRequester() }
    var options by remember { mutableStateOf<InstalledApp?>(null) }
    var focusArea by remember { mutableStateOf(if (startOnSettingsButton) HomeArea.SettingsButton else HomeArea.Apps) }
    // Where the gear last had focus: coming back to home from another page lands on the page, not on a gear that
    // was only passed on the way (#219). Back from settings still returns to it.
    var gearWorkspace by remember { mutableStateOf(workspace) }
    val panelOpen = options != null || holdFocus
    val pageActions = PageActions(onLaunchApp, onShowOptions = { options = it }, switcher, panelOpen, marks)
    Box(modifier = modifier.fillMaxSize()) {
        HomeLayout(workspace, switcher, topBar = { barModifier ->
            TopBar(
                workspace = workspace,
                onOpenSettings = onOpenSettings,
                clockFormat = clockFormat,
                onFocusArea = {
                    focusArea = it
                    gearWorkspace = workspace
                },
                clock = clock,
                settingsFocus = settingsFocus,
                onOpenTvSettings = onOpenTvSettings,
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
                    systemPanel = systemPanel,
                    screenTime = screenTime,
                    focusArea = focusArea.takeUnless { it.inTopBar && gearWorkspace != page }
                        ?: HomeArea.Apps,
                    onFocusArea = { focusArea = it },
                    settingsFocus = settingsFocus,
                )

                Workspace.Watch, Workspace.Play -> AppGridPage(page, pageApps, pageActions)

                Workspace.AllApps -> allApps()
            }
        }
        options?.let {
            WorkspaceOptions(
                app = it,
                workspaces = workspaces,
                onTogglePin = onTogglePin,
                onMovePin = onMovePin,
                onOpenAppInfo = onOpenAppInfo,
                onUninstallApp = onUninstallApp,
                onDismiss = { options = null },
                iconStyle = iconStyles[it.packageName] ?: IconStyle.Default,
                onChangeIconStyle = onChangeIconStyle,
            )
        }
    }
}

/**
 * The top bar over the current workspace's page. The bar sits in a panel of its own, like Omarchy's Waybar, inset
 * from the screen edges like the tiles and frosted, clear or solid with them (#122). Number, channel and menu keys
 * switch workspace from anywhere (see [selectsWorkspace]); Left and Right at the edge of the page do it on any remote
 * (see [WorkspacePages]).
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
        Tile(
            modifier = Modifier.fillMaxWidth().height(HomeDimens.TopBarHeight),
            contentPadding = PaddingValues(horizontal = HomeDimens.TopBarPaddingH),
        ) {
            topBar(Modifier.fillMaxSize())
        }
        WorkspacePages(switcher, workspace, Modifier.fillMaxWidth().weight(1f), page)
    }
}

/**
 * The options for [app]: pin it to any workspace, and move its pin along the current page's pins, which come first
 * on the page in pin order. All apps lists everything alphabetically, so pins don't move there.
 */
@Composable
private fun WorkspaceOptions(
    app: InstalledApp,
    workspaces: WorkspaceState,
    onTogglePin: (InstalledApp, Workspace) -> Unit,
    onMovePin: (InstalledApp, Workspace, Int) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
    iconStyle: IconStyle,
    onChangeIconStyle: ((InstalledApp, IconStyle) -> Unit)?,
) {
    val page = workspaces.current
    val choices = workspaces.pinChoices(app.packageName)
    val pagePins = if (page == Workspace.AllApps) emptyList() else workspaces.pinsOf(page)
    val index = pagePins.indexOf(app.packageName)
    AppOptionsPanel(
        appLabel = app.label,
        pins = choices.map { (workspace, pinned) -> PinChoice(workspace.label, pinned) },
        onTogglePin = { onTogglePin(app, choices[it].first) },
        onOpenAppInfo = { onOpenAppInfo(app) },
        onUninstall = { onUninstallApp(app) },
        onDismiss = onDismiss,
        onMoveUp = if (index > 0) ({ onMovePin(app, page, -1) }) else null,
        onMoveDown = if (index >= 0 && index < pagePins.lastIndex) ({ onMovePin(app, page, 1) }) else null,
        iconStyle = iconStyle.label,
        onNextIconStyle = onChangeIconStyle?.let { change -> { change(app, iconStyle.next) } },
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
    /** Each app's logo for its tile; apps without one show their monogram. */
    val marks: AppMarks = AppMarks.None,
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
            onTogglePin = { _, _ -> },
            onMovePin = { _, _, _ -> },
            onOpenAppInfo = {},
            onUninstallApp = {},
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
