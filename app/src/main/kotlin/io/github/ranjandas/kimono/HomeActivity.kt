package io.github.ranjandas.kimono

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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

    // Counts the Home presses (and launches from the apps row) that reach the running activity. Each one sends
    // the launcher back to its start: home, workspace 1, focus on the first app.
    private var homePresses by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android keeps home activities in their own task, so the first Home press after Kimono was opened some
        // other way (from the apps row, or adb) starts a second instance rather than reaching this one. Treat that
        // start as a Home press too. A restore after process death keeps its state instead.
        if (savedInstanceState == null && intent.hasCategory(Intent.CATEGORY_HOME)) homePresses++
        setContent { KimonoApp(dependencies, homePresses) }
    }

    // singleTask: once running, Home or opening Kimono from the apps row comes back here as a new intent.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) homePresses++
    }
}
