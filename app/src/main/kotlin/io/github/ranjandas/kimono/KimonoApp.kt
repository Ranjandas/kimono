package io.github.ranjandas.kimono

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ranjandas.kimono.core.data.idle.IdleController
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppScreen
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.feature.drawer.DrawerScreen
import io.github.ranjandas.kimono.feature.home.HomeScreen
import io.github.ranjandas.kimono.feature.home.RecentApps
import io.github.ranjandas.kimono.feature.settings.SettingsScreen
import kotlinx.coroutines.launch

private enum class Screen { Home, Settings }

/**
 * The launcher: the workspaces (all apps, the drawer, among them) and settings, themed from the stored settings. Nothing is drawn until the
 * settings and workspaces have loaded, so a chosen theme never flashes the default one first. The app list is
 * collected from the start too, so it loads alongside them; it's null until the first list arrives.
 *
 * [homePresses] goes up on each Home press while Kimono is running; see [Screens]. While [idle], everything
 * dims and slowly shifts by a few pixels, and the wallpaper turns to pixel art (see [Ambient]).
 */
@Composable
internal fun KimonoApp(dependencies: KimonoDependencies, idle: IdleController, homePresses: Int = 0) {
    val settings by dependencies.settingsStore.settings.collectAsStateWithLifecycle(initialValue = null)
    val workspaces by dependencies.workspaceStore.state.collectAsStateWithLifecycle(initialValue = null)
    val apps by dependencies.appsRepository.apps.collectAsStateWithLifecycle(initialValue = null)
    val recents = rememberRecentApps(dependencies.usageHistory, apps)
    val isIdle by idle.idle.collectAsStateWithLifecycle()
    val loadedSettings = settings ?: return
    val loadedWorkspaces = workspaces ?: return
    LaunchedEffect(loadedSettings.idleTimeout) { idle.timeout = loadedSettings.idleTimeout.duration }
    val scope = rememberCoroutineScope()
    val updateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit = { change ->
        scope.launch { dependencies.settingsStore.update(change) }
    }
    val theme = ThemeId.fromName(loadedSettings.themeName)
    val cornerStyle = CornerStyle.fromName(loadedSettings.cornerStyleName)
    val wallpaper = rememberWallpaper(dependencies.wallpapers, wallpaperUrl(loadedSettings, theme))
    val shift = rememberPixelShift(isIdle && loadedSettings.pixelShift)
    KimonoTheme(theme, cornerStyle, loadedSettings.textSize.scale) {
        Ambient(wallpaper, theme, loadedSettings.panels, isIdle, shift) {
            KimonoTheme(theme, cornerStyle, loadedSettings.textSize.scale, overWallpaper = wallpaper != null) {
                Screens(dependencies, loadedSettings, updateSettings, loadedWorkspaces, apps, recents, homePresses)
            }
        }
    }
}

@Composable
private fun Screens(
    dependencies: KimonoDependencies,
    settings: LauncherSettings,
    updateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    workspaces: WorkspaceState,
    apps: List<InstalledApp>?,
    recents: RecentApps,
    homePresses: Int,
) {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val actions = appCallbacks(dependencies, workspaces)
    val theme = ThemeId.fromName(settings.themeName)
    // A Home press goes back to the start: the home screen on workspace 1. Everything else (the screen, the
    // workspace) is saved, so it survives the process being killed in the background.
    LaunchedEffect(homePresses) {
        if (homePresses > 0) {
            screen = Screen.Home
            actions.selectWorkspace(Workspace.Default)
        }
    }
    when (screen) {
        // A fresh home screen per press, so focus starts on the first app and the options panel is closed.
        Screen.Home -> key(homePresses) {
            HomeScreen(
                apps = apps,
                onLaunchApp = dependencies.appLauncher::launch,
                workspaces = workspaces,
                onSelectWorkspace = actions.selectWorkspace,
                onTogglePin = actions.togglePin,
                onOpenAppInfo = actions.openInfo,
                onUninstallApp = actions.uninstall,
                theme = theme,
                onCycleTheme = { updateSettings { it.copy(themeName = next(theme).name) } },
                onOpenSettings = { screen = Screen.Settings },
                clockFormat = settings.clockFormat,
                recents = recents,
                onRequestUsageAccess = actions.requestUsageAccess,
            ) {
                DrawerScreen(
                    apps = apps.orEmpty(),
                    onLaunchApp = dependencies.appLauncher::launch,
                    workspaces = workspaces,
                    onTogglePin = actions.togglePin,
                    onOpenAppInfo = actions.openInfo,
                    onUninstallApp = actions.uninstall,
                    onClose = { actions.selectWorkspace(Workspace.Home) },
                )
            }
        }

        Screen.Settings -> Settings(settings, updateSettings, onClose = { screen = Screen.Home })
    }
}

@Composable
private fun Settings(
    settings: LauncherSettings,
    updateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onClose: () -> Unit,
) {
    SettingsScreen(
        theme = ThemeId.fromName(settings.themeName),
        onThemeChange = { new -> updateSettings { it.copy(themeName = new.name) } },
        cornerStyle = CornerStyle.fromName(settings.cornerStyleName),
        onCornerStyleChange = { new -> updateSettings { it.copy(cornerStyleName = new.name) } },
        clockFormat = settings.clockFormat,
        onClockFormatChange = { new -> updateSettings { it.copy(clockFormat = new) } },
        textSize = settings.textSize,
        onTextSizeChange = { new -> updateSettings { it.copy(textSize = new) } },
        idleTimeout = settings.idleTimeout,
        onIdleTimeoutChange = { new -> updateSettings { it.copy(idleTimeout = new) } },
        pixelShift = settings.pixelShift,
        onPixelShiftChange = { new -> updateSettings { it.copy(pixelShift = new) } },
        wallpaper = wallpaperChoice(settings),
        onWallpaperToggle = { updateSettings { it.copy(wallpaper = !it.wallpaper) } },
        panels = settings.panels,
        onPanelsChange = { new -> updateSettings { it.copy(panels = new) } },
        onClose = onClose,
    )
}

/** What the home list and the drawer do to an app, beyond opening it. */
private class AppCallbacks(
    val selectWorkspace: (Workspace) -> Unit,
    val togglePin: (InstalledApp) -> Unit,
    val openInfo: (InstalledApp) -> Unit,
    val uninstall: (InstalledApp) -> Unit,
    val requestUsageAccess: () -> Unit,
)

@Composable
private fun appCallbacks(dependencies: KimonoDependencies, workspaces: WorkspaceState): AppCallbacks {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = dependencies.workspaceStore

    // A TV build can hide parts of Settings, so say so rather than looking broken.
    fun openScreen(screen: AppScreen): (InstalledApp) -> Unit = { app ->
        if (!dependencies.appActions.open(screen, app)) {
            Toast.makeText(context, "that screen isn't available on this device", Toast.LENGTH_SHORT).show()
        }
    }
    return AppCallbacks(
        selectWorkspace = { workspace -> scope.launch { store.select(workspace) } },
        togglePin = { app ->
            // All apps lists everything, so pins made there go to home.
            val workspace = workspaces.current.pinTarget
            scope.launch {
                if (app.packageName in workspaces.pinsOf(workspace)) {
                    store.unpin(workspace, app.packageName)
                } else {
                    store.pin(workspace, app.packageName)
                }
            }
        },
        openInfo = openScreen(AppScreen.Info),
        uninstall = openScreen(AppScreen.Uninstall),
        requestUsageAccess = {
            if (!dependencies.usageHistory.openAccessSettings()) {
                Toast.makeText(context, USAGE_ACCESS_OVER_ADB, Toast.LENGTH_LONG).show()
            }
        },
    )
}

private fun next(theme: ThemeId): ThemeId = ThemeId.entries[(theme.ordinal + 1) % ThemeId.entries.size]

// Google TV hides the usage access screen, so the one way in is adb (docs/DEVELOPING.md).
private const val USAGE_ACCESS_OVER_ADB =
    "this TV hides usage access; grant it with: adb shell appops set io.github.ranjandas.kimono GET_USAGE_STATS allow"
