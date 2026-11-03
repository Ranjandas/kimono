package io.github.ranjandas.kimono.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.IdleTimeout
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.settings.SystemPanel
import io.github.ranjandas.kimono.core.data.settings.TextSize
import io.github.ranjandas.kimono.core.design.component.Hint
import io.github.ranjandas.kimono.core.design.component.HintBar
import io.github.ranjandas.kimono.core.design.component.ListRow
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * Kimono's settings: one row per setting, showing its value. Left and Right step a setting back and forward
 * (OK steps forward too), and the change applies at once, so the screen itself is the preview. The theme lives
 * here rather than on home's top bar, where one stray press changed it (#110). Back returns home.
 */
@Composable
fun SettingsScreen(
    theme: ThemeId,
    onThemeChange: (ThemeId) -> Unit,
    cornerStyle: CornerStyle,
    onCornerStyleChange: (CornerStyle) -> Unit,
    clockFormat: ClockFormat,
    onClockFormatChange: (ClockFormat) -> Unit,
    textSize: TextSize,
    onTextSizeChange: (TextSize) -> Unit,
    idleTimeout: IdleTimeout,
    onIdleTimeoutChange: (IdleTimeout) -> Unit,
    pixelShift: Boolean,
    onPixelShiftChange: (Boolean) -> Unit,
    wallpaper: WallpaperChoice,
    onWallpaperToggle: () -> Unit,
    panels: Panels,
    onPanelsChange: (Panels) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    systemPanel: SystemPanel = SystemPanel.Default,
    onSystemPanelChange: (SystemPanel) -> Unit = {},
) {
    val firstRow = remember { FocusRequester() }
    // Retried like home and the drawer, in case the screen draws before it can take focus.
    LaunchedEffect(Unit) { firstRow.requestFocusWhenLaidOut() }
    BackHandler(onBack = onClose)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .padding(horizontal = KimonoSpacing.XL, vertical = KimonoSpacing.M),
        verticalArrangement = Arrangement.spacedBy(KimonoSpacing.L),
    ) {
        Text("settings", style = KimonoTheme.typography.headline, color = KimonoTheme.colors.accent)
        Tile(modifier = Modifier.width(SettingsWidth)) {
            SectionHeader("appearance", icon = KimonoIcon.Settings)
            Column(
                modifier = Modifier.padding(top = KimonoSpacing.S),
                verticalArrangement = Arrangement.spacedBy(KimonoSpacing.XXS),
            ) {
                SettingRow(
                    KimonoIcon.Palette,
                    "theme",
                    theme.displayName.lowercase(),
                    onStep = { onThemeChange(theme.step(it)) },
                    modifier = Modifier.focusRequester(firstRow),
                )
                SettingRow(KimonoIcon.Corners, "corners", cornerStyle.name.lowercase(), onStep = {
                    onCornerStyleChange(cornerStyle.step(it))
                })
                SettingRow(KimonoIcon.TextSize, "text size", textSize.label, onStep = {
                    onTextSizeChange(textSize.step(it))
                })
                SettingRow(KimonoIcon.Clock, "clock", clockFormat.label, onStep = {
                    onClockFormatChange(clockFormat.step(it))
                })
                SettingRow(KimonoIcon.Idle, "dim after", idleTimeout.label, onStep = {
                    onIdleTimeoutChange(idleTimeout.step(it))
                })
                // Two values each way, so either direction toggles.
                SettingRow(KimonoIcon.Device, "shift pixels", if (pixelShift) "on" else "off", onStep = {
                    onPixelShiftChange(!pixelShift)
                })
                SettingRow(KimonoIcon.Wallpaper, "wallpaper", wallpaper.label, onStep = { onWallpaperToggle() })
                SettingRow(KimonoIcon.Glass, "panels", panels.label, onStep = { onPanelsChange(panels.step(it)) })
                SettingRow(KimonoIcon.Activity, "system tile", systemPanel.label, onStep = {
                    onSystemPanelChange(systemPanel.step(it))
                })
            }
        }
        // Pinned to the bottom, where home and the drawer keep theirs.
        Spacer(Modifier.weight(1f))
        HintBar(SettingsHints)
    }
}

/**
 * A setting and its value. On the focused row the value sits between accent arrows, which is what Left and Right
 * do (#134); elsewhere the arrows keep their room but don't show, so values don't shift as focus moves. [onStep]
 * gets -1 for Left and 1 for Right, and OK steps forward too.
 */
@Composable
private fun SettingRow(
    icon: KimonoIcon,
    name: String,
    value: String,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListRow(
        text = name,
        onClick = { onStep(1) },
        modifier = modifier
            .onPreviewKeyEvent { event ->
                val step = SideSteps[event.key]
                if (step != null && event.type == KeyEventType.KeyDown) onStep(step)
                step != null
            }
            // TalkBack reads "theme, kimono" with the value as the row's state, and offers Left and Right as its
            // "previous value" and "next value" actions (#45).
            .semantics {
                stateDescription = value
                customActions = SideSteps.values.map { step ->
                    CustomAccessibilityAction(if (step < 0) "previous value" else "next value") {
                        onStep(step)
                        true
                    }
                }
            },
        icon = icon,
        trailingContent = { focused -> SettingValue(value, focused) },
    )
}

/** [value] between chevrons that show only while the row is [focused]. */
@Composable
private fun SettingValue(value: String, focused: Boolean) {
    val colors = KimonoTheme.colors
    val arrows = if (focused) colors.accent else Color.Transparent
    Row(
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.ChevronLeft, color = arrows)
        Text(
            value,
            // Read as the row's state instead.
            modifier = Modifier.clearAndSetSemantics {},
            style = KimonoTheme.typography.label,
            color = if (focused) colors.text else colors.textMuted,
        )
        Icon(KimonoIcon.ChevronRight, color = arrows)
    }
}

private val SideSteps = mapOf(Key.DirectionLeft to -1, Key.DirectionRight to 1)

/** The entry [by] places on, wrapping round, so either direction reaches every value of a setting. */
private inline fun <reified T : Enum<T>> T.step(by: Int): T {
    val values = enumValues<T>()
    return values[(ordinal + by).mod(values.size)]
}

private val SettingsHints = listOf(
    Hint("▲ ▼", "choose"),
    Hint("◀ ▶", "change"),
    Hint("ok", "next value"),
    Hint("back", "home"),
)

/** What the wallpaper row shows: the theme's image, one set over adb, or none. */
enum class WallpaperChoice(val label: String) { Theme("theme"), Custom("custom"), Off("off") }

private val SettingsWidth = 480.dp

@Preview(device = Devices.TV_1080p)
@Composable
private fun SettingsScreenPreview() {
    KimonoTheme {
        SettingsScreen(
            theme = ThemeId.Default,
            onThemeChange = {},
            cornerStyle = CornerStyle.Default,
            onCornerStyleChange = {},
            clockFormat = ClockFormat.System,
            onClockFormatChange = {},
            textSize = TextSize.Default,
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
