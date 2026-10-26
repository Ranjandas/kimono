package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
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
            KimonoTheme {
                HomeScreen(apps = apps, onLaunchApp = appLauncher::launch)
            }
        }
    }
}
