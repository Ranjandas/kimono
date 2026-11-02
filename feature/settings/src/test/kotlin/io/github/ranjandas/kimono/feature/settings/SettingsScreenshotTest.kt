package io.github.ranjandas.kimono.feature.settings

import com.github.takahirom.roborazzi.captureRoboImage
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.IdleTimeout
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.settings.TextSize
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Settings at each text size, in Kimono's own dark theme and the light one (#45). See docs/DEVELOPING.md for
 * recording new screenshots.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-land-xhdpi")
class SettingsScreenshotTest(private val theme: ThemeId, private val textSize: TextSize) {
    @Test
    fun settings() {
        captureRoboImage("src/test/screenshots/settings/${theme.name}-${textSize.name}.png") {
            KimonoTheme(theme = theme, textScale = textSize.scale) {
                SettingsScreen(
                    theme = theme,
                    onThemeChange = {},
                    cornerStyle = CornerStyle.Default,
                    onCornerStyleChange = {},
                    clockFormat = ClockFormat.System,
                    onClockFormatChange = {},
                    textSize = textSize,
                    onTextSizeChange = {},
                    idleTimeout = IdleTimeout.Default,
                    onIdleTimeoutChange = {},
                    pixelShift = true,
                    onPixelShiftChange = {},
                    wallpaper = WallpaperChoice.Theme,
                    onWallpaperToggle = {},
                    panels = Panels.Default,
                    onPanelsChange = {},
                    onClose = {},
                )
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}")
        fun parameters(): List<Array<Any>> =
            listOf(ThemeId.Kimono, ThemeId.RosePine).flatMap { theme -> TextSize.entries.map { arrayOf<Any>(theme, it) } }
    }
}
