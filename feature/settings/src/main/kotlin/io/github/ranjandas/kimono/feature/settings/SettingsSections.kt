package io.github.ranjandas.kimono.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.IdleTimeout
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.settings.SystemPanel
import io.github.ranjandas.kimono.core.data.settings.TextSize
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/*
 * The three sections of the settings panel (#155): how things look, what keeps an OLED panel from burning in, and
 * what home shows.
 */

/** How things look. [firstRow] goes on the theme row, which takes focus when the screen opens. */
@Composable
internal fun LookSection(
    theme: ThemeId,
    onThemeChange: (ThemeId) -> Unit,
    cornerStyle: CornerStyle,
    onCornerStyleChange: (CornerStyle) -> Unit,
    textSize: TextSize,
    onTextSizeChange: (TextSize) -> Unit,
    panels: Panels,
    onPanelsChange: (Panels) -> Unit,
    firstRow: FocusRequester,
) {
    SettingsSection("look") {
        val themeName = theme.displayName.lowercase()
        SettingRow(
            KimonoIcon.Palette,
            "Theme",
            themeName,
            onStep = { onThemeChange(theme.step(it)) },
            modifier = Modifier.focusRequester(firstRow),
        ) {
            Cycle(themeName, it)
        }
        ChoiceRow(KimonoIcon.Corners, "Corners", cornerStyle, CornerStyle.entries, { it.name.lowercase() }) {
            onCornerStyleChange(it)
        }
        ChoiceRow(KimonoIcon.TextSize, "Text size", textSize, TextSize.entries, TextSize::label, onTextSizeChange)
        ChoiceRow(KimonoIcon.Glass, "Panels", panels, Panels.entries, Panels::label, onPanelsChange)
    }
}

/** What keeps an OLED panel from burning in. */
@Composable
internal fun ScreenCareSection(
    idleTimeout: IdleTimeout,
    onIdleTimeoutChange: (IdleTimeout) -> Unit,
    pixelShift: Boolean,
    onPixelShiftChange: (Boolean) -> Unit,
) {
    SettingsSection("screen care") {
        SettingRow(KimonoIcon.Idle, "Dim after", idleTimeout.label, onStep = {
            onIdleTimeoutChange(idleTimeout.step(it))
        }) { Steps(idleTimeout.filledSteps, TIMEOUT_STEPS, idleTimeout.label, it) }
        // Two values each way, so either direction toggles.
        SettingRow(KimonoIcon.Device, "Shift pixels", if (pixelShift) "on" else "off", onStep = {
            onPixelShiftChange(!pixelShift)
        }) { Toggle(pixelShift, it) }
    }
}

/** What home shows. */
@Composable
internal fun HomeSection(
    wallpaper: WallpaperChoice,
    onWallpaperToggle: () -> Unit,
    clockFormat: ClockFormat,
    onClockFormatChange: (ClockFormat) -> Unit,
    systemPanel: SystemPanel,
    onSystemPanelChange: (SystemPanel) -> Unit,
) {
    SettingsSection("home") {
        SettingRow(KimonoIcon.Wallpaper, "Wallpaper", wallpaper.label, onStep = { onWallpaperToggle() }) {
            WallpaperValue(wallpaper, it)
        }
        ChoiceRow(KimonoIcon.Clock, "Clock", clockFormat, ClockFormat.entries, ClockFormat::label, onClockFormatChange)
        ChoiceRow(KimonoIcon.Activity, "System tile", systemPanel, SystemPanel.entries, SystemPanel::label) {
            onSystemPanelChange(it)
        }
    }
}

/** An uppercase [title] over its rows. */
@Composable
private fun SettingsSection(title: String, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(KimonoSpacing.XXS)) {
        SectionHeader(title, modifier = Modifier.padding(bottom = KimonoSpacing.XS))
        rows()
    }
}

/** A setting with a few named [values], shown as chips. */
@Composable
private fun <T> ChoiceRow(
    icon: KimonoIcon,
    name: String,
    value: T,
    values: List<T>,
    label: (T) -> String,
    onChange: (T) -> Unit,
) {
    val index = values.indexOf(value)
    SettingRow(icon, name, label(value), onStep = { onChange(values[(index + it).mod(values.size)]) }) { focused ->
        Chips(values.map(label), index, focused)
    }
}

/** The wallpaper as a switch, with `custom` beside it when the image is one set over adb. */
@Composable
private fun WallpaperValue(wallpaper: WallpaperChoice, focused: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (wallpaper == WallpaperChoice.Custom) ValueText(wallpaper.label, focused)
        Toggle(wallpaper != WallpaperChoice.Off, focused)
    }
}

/** How many of the dim-after bars are lit: one per step up to five minutes, none when it's off. */
private val IdleTimeout.filledSteps: Int get() = if (this == IdleTimeout.Off) 0 else ordinal + 1

private val TIMEOUT_STEPS = IdleTimeout.entries.size - 1
