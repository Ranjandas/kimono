package io.github.ranjandas.kimono.feature.home

import com.github.takahirom.roborazzi.captureRoboImage
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.TextSize
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

/**
 * The home screen in every theme × corner style, and once with large text. See docs/DEVELOPING.md for recording new
 * screenshots.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-land-xhdpi")
class HomeScreenshotTest(
    private val theme: ThemeId,
    private val cornerStyle: CornerStyle,
    private val textSize: TextSize,
) {
    @Test
    fun home() {
        val suffix = if (textSize == TextSize.Default) "" else "-${textSize.name}"
        captureRoboImage("src/test/screenshots/home/${theme.name}-${cornerStyle.name}$suffix.png") {
            KimonoTheme(theme = theme, cornerStyle = cornerStyle, textScale = textSize.scale) {
                HomeScreen(
                    apps = SampleApps,
                    onLaunchApp = { LaunchResult.Launched },
                    workspaces = WorkspaceState(pins = mapOf(Workspace.Home to listOf("pkg.Jellyfin"))),
                    onSelectWorkspace = {},
                    onTogglePin = { _, _ -> },
                    onMovePin = { _, _, _ -> },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    theme = theme,
                    onOpenSettings = {},
                    clockFormat = ClockFormat.TwentyFourHour,
                    recents = RecentApps.Apps(SampleApps.take(4)),
                    onRequestUsageAccess = {},
                    clock = FixedClock,
                    allApps = {},
                )
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}-{2}")
        fun parameters(): List<Array<Any>> =
            ThemeId.entries.flatMap { theme -> CornerStyle.entries.map { arrayOf<Any>(theme, it, TextSize.Default) } } +
                // Large text once, in the default look (#45).
                listOf(arrayOf<Any>(ThemeId.Default, CornerStyle.Default, TextSize.Large))

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
