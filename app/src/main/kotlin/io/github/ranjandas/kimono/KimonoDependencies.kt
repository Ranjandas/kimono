package io.github.ranjandas.kimono

import io.github.ranjandas.kimono.core.data.history.LaunchHistory
import io.github.ranjandas.kimono.core.data.icons.IconStyleStore
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.core.data.wallpaper.WallpaperCache
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceStore
import io.github.ranjandas.kimono.data.apps.AppActions
import io.github.ranjandas.kimono.data.apps.AppLauncher
import io.github.ranjandas.kimono.data.apps.AppsRepository
import io.github.ranjandas.kimono.data.apps.UsageHistory
import io.github.ranjandas.kimono.data.apps.icon.AppMarkCache

/** Everything the screens read from or act on, created once by the activity. */
@Suppress("LongParameterList") // A plain holder: grouping these would only add indirection.
class KimonoDependencies(
    val appsRepository: AppsRepository,
    val appLauncher: AppLauncher,
    val appActions: AppActions,
    val workspaceStore: WorkspaceStore,
    val settingsStore: SettingsStore,
    val usageHistory: UsageHistory,
    val launchHistory: LaunchHistory,
    val wallpapers: WallpaperCache,
    val iconStyleStore: IconStyleStore,
    val appMarks: AppMarkCache,
)
