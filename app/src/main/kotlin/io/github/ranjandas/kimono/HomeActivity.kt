package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceStore
import io.github.ranjandas.kimono.data.apps.AppActions
import io.github.ranjandas.kimono.data.apps.AppLauncher
import io.github.ranjandas.kimono.data.apps.AppsRepository
import io.github.ranjandas.kimono.data.apps.UsageHistory

class HomeActivity : ComponentActivity() {
    private val dependencies by lazy {
        KimonoDependencies(
            appsRepository = AppsRepository.create(this),
            appLauncher = AppLauncher.create(this),
            appActions = AppActions.create(this),
            workspaceStore = WorkspaceStore.create(this),
            settingsStore = SettingsStore.create(this),
            usageHistory = UsageHistory.create(this),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KimonoApp(dependencies) }
    }
}
