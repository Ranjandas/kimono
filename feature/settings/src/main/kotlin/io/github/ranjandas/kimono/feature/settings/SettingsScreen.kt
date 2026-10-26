package io.github.ranjandas.kimono.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.TextSize
import io.github.ranjandas.kimono.core.design.component.Hint
import io.github.ranjandas.kimono.core.design.component.HintBar
import io.github.ranjandas.kimono.core.design.component.ListRow
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/**
 * Kimono's settings: one row per setting, showing its value. OK moves a setting to its next value, and the
 * change applies at once, so the screen itself is the preview. Back returns home.
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
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstRow = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstRow.requestFocus() } }
    BackHandler(onBack = onClose)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("~/settings", style = KimonoTheme.typography.headline, color = KimonoTheme.colors.accent)
        Tile(modifier = Modifier.width(SettingsWidth)) {
            SectionHeader("appearance", icon = KimonoIcon.Settings)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                SettingRow(
                    KimonoIcon.Palette,
                    "theme",
                    theme.displayName.lowercase(),
                    onClick = { onThemeChange(theme.next()) },
                    modifier = Modifier.focusRequester(firstRow),
                )
                SettingRow(KimonoIcon.Corners, "corners", cornerStyle.name.lowercase(), onClick = {
                    onCornerStyleChange(cornerStyle.next())
                })
                SettingRow(KimonoIcon.TextSize, "text size", textSize.label, onClick = {
                    onTextSizeChange(textSize.next())
                })
                SettingRow(KimonoIcon.Clock, "clock", clockFormat.label, onClick = {
                    onClockFormatChange(clockFormat.next())
                })
            }
        }
        // Pinned to the bottom, where home and the drawer keep theirs.
        Spacer(Modifier.weight(1f))
        HintBar(listOf(Hint("▲ ▼", "choose"), Hint("ok", "next value"), Hint("back", "home")))
    }
}

@Composable
private fun SettingRow(
    icon: KimonoIcon,
    name: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListRow(text = name, onClick = onClick, modifier = modifier, icon = icon, trailing = "‹ $value ›")
}

/** The next entry, wrapping round, so OK steps through every value of a setting. */
private inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    return values[(ordinal + 1) % values.size]
}

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
            onClose = {},
        )
    }
}
