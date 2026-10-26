package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppLauncher
import io.github.ranjandas.kimono.data.apps.AppsRepository
import io.github.ranjandas.kimono.feature.home.HomeScreen

class HomeActivity : ComponentActivity() {
    private val appsRepository by lazy { AppsRepository.create(this) }
    private val appLauncher by lazy { AppLauncher.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val apps by appsRepository.apps.collectAsStateWithLifecycle(initialValue = emptyList())
            // Survives config changes and process death only; the settings screen (M1-13) persists it.
            var themeIndex by rememberSaveable { mutableIntStateOf(ThemeId.Default.ordinal) }
            val theme = ThemeId.entries[themeIndex]
            KimonoTheme(theme = theme) {
                HomeScreen(
                    apps = apps,
                    onLaunchApp = appLauncher::launch,
                    theme = theme,
                    onCycleTheme = { themeIndex = (themeIndex + 1) % ThemeId.entries.size },
                )
            }
        }
    }
}
