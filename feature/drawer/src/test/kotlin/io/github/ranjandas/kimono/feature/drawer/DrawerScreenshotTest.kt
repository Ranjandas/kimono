package io.github.ranjandas.kimono.feature.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The app drawer in every theme × corner style. See docs/DEVELOPING.md for recording new screenshots. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-land-xhdpi")
class DrawerScreenshotTest(private val theme: ThemeId, private val cornerStyle: CornerStyle) {
    @Test
    fun drawer() {
        captureRoboImage("src/test/screenshots/drawer/${theme.name}-${cornerStyle.name}.png") {
            KimonoTheme(theme = theme, cornerStyle = cornerStyle) {
                // The launcher draws the background and padding around the all apps page.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(KimonoTheme.colors.background)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    DrawerScreen(
                        apps = SampleApps,
                        onLaunchApp = { LaunchResult.Launched },
                        workspaces = WorkspaceState(pins = mapOf(Workspace.Home to listOf("pkg.Jellyfin"))),
                        onTogglePin = {},
                        onOpenAppInfo = {},
                        onUninstallApp = {},
                        onClose = {},
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}")
        fun parameters(): List<Array<Any>> =
            ThemeId.entries.flatMap { theme -> CornerStyle.entries.map { arrayOf(theme, it) } }

        private val SampleApps = listOf(
            InstalledApp("pkg.Jellyfin", "pkg.Jellyfin.Main", "Jellyfin", AppCategory.Video, isTvApp = true),
            InstalledApp("pkg.Plex", "pkg.Plex.Main", "Plex", AppCategory.Video, isTvApp = true),
            InstalledApp("pkg.Spotify", "pkg.Spotify.Main", "Spotify", AppCategory.Audio, isTvApp = true),
            InstalledApp("pkg.Retro", "pkg.Retro.Main", "Retro", AppCategory.Game, isTvApp = true),
            InstalledApp("pkg.Files", "pkg.Files.Main", "Files", AppCategory.Other, isTvApp = false),
            InstalledApp("pkg.Settings", "pkg.Settings.Main", "Settings", AppCategory.Other, isTvApp = true),
        )
    }
}
