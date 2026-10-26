package io.github.ranjandas.kimono.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The home screen in every theme × corner style. See docs/DEVELOPING.md for recording new screenshots. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-land-xhdpi")
class HomeScreenshotTest(private val theme: ThemeId, private val cornerStyle: CornerStyle) {
    @Test
    fun home() {
        captureRoboImage("src/test/screenshots/home/${theme.name}-${cornerStyle.name}.png") {
            KimonoTheme(theme = theme, cornerStyle = cornerStyle) {
                HomeScreen(
                    apps = SampleApps,
                    onLaunchApp = { LaunchResult.Launched },
                    workspaces = WorkspaceState(pins = mapOf(Workspace.Home to listOf("pkg.Jellyfin"))),
                    onSelectWorkspace = {},
                    onOpenDrawer = {},
                    onTogglePin = {},
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    theme = theme,
                    onCycleTheme = {},
                    onOpenSettings = {},
                    clockFormat = ClockFormat.TwentyFourHour,
                    recents = RecentApps.Apps(SampleApps.take(2)),
                    onRequestUsageAccess = {},
                    clock = FixedClock,
                )
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}")
        fun parameters(): List<Array<Any>> =
            ThemeId.entries.flatMap { theme -> CornerStyle.entries.map { arrayOf(theme, it) } }

        private val FixedClock = Clock.fixed(Instant.parse("2026-10-26T20:30:00Z"), ZoneOffset.UTC)

        private val SampleApps = listOf(
            "Jellyfin" to AppCategory.Video,
            "Kodi" to AppCategory.Video,
            "Plex" to AppCategory.Video,
            "Spotify" to AppCategory.Audio,
            "Retro" to AppCategory.Game,
            "YouTube" to AppCategory.Video,
        ).map { (label, category) -> InstalledApp("pkg.$label", "pkg.$label.Main", label, category, isTvApp = true) }
    }
}
