package io.github.ranjandas.kimono.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
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
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import java.time.LocalDate

/**
 * Kimono's settings: one panel of rows in three sections, each row drawing its value as chips, a switch or a stepped
 * bar, after Omarchy's panels (#155, #156). Left and Right step a setting back and forward
 * (OK steps forward too), and the change applies at once, so the screen itself is the preview. The theme lives
 * here rather than on home's top bar, where one stray press changed it (#110). Back returns home.
 *
 * [homeWatcher] is whether Kimono opens over Google TV's home (#26): null leaves the row out, as in the Play build,
 * which doesn't have it. Its switch lives in the system's accessibility settings, so the row opens those.
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
    homeWatcher: Boolean? = null,
    onHomeWatcherClick: () -> Unit = {},
    subtitle: String = PanelSubtitle.forDay(LocalDate.now()),
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
        verticalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
    ) {
        SettingsTitle(subtitle)
        // One panel split by rules, like Omarchy's (#155). The box takes all the height down to the hint bar, so
        // the panel grows to fit its rows and only scrolls when they don't fit (a large text size); focus brings
        // the row into view. While there are rows past an edge, that edge fades out, so a cut row reads as more to
        // scroll to rather than as a glitch (#165).
        Box(Modifier.weight(1f)) {
            Tile(modifier = Modifier.width(SettingsWidth)) {
                val scroll = rememberScrollState()
                Column(Modifier.fadingEdges(scroll).verticalScroll(scroll)) {
                    LookSection(
                        theme, onThemeChange, cornerStyle, onCornerStyleChange, textSize, onTextSizeChange, panels,
                        onPanelsChange, firstRow,
                    )
                    Rule()
                    ScreenCareSection(idleTimeout, onIdleTimeoutChange, pixelShift, onPixelShiftChange)
                    Rule()
                    HomeSection(
                        wallpaper,
                        onWallpaperToggle,
                        clockFormat,
                        onClockFormatChange,
                        systemPanel,
                        onSystemPanelChange,
                        homeWatcher,
                        onHomeWatcherClick,
                    )
                }
            }
        }
        HintBar(SettingsHints)
    }
}

/**
 * The screen's head, like an Omarchy panel's: the settings icon in the accent, a bold title and under it the day's
 * [subtitle] in small caps (#157). TalkBack reads the subtitle as written, not letter by letter.
 */
@Composable
private fun SettingsTitle(subtitle: String, modifier: Modifier = Modifier) {
    val style = KimonoTheme.typography.headline
    val colors = KimonoTheme.colors
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.Settings, color = colors.accent, style = style)
        Column {
            Text("Settings", style = style, fontWeight = FontWeight.Bold, color = colors.text)
            Text(
                subtitle.uppercase(),
                modifier = Modifier.semantics { contentDescription = subtitle },
                style = KimonoTheme.typography.section,
                color = colors.accentSecondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * A setting, its [value] for TalkBack and a [control] that draws it, knowing whether the row has focus. [onStep]
 * gets -1 for Left and 1 for Right, and OK steps forward too.
 */
@Composable
internal fun SettingRow(
    icon: KimonoIcon,
    name: String,
    value: String,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
    control: @Composable (focused: Boolean) -> Unit,
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
            // TalkBack reads "Theme, kimono" with the value as the row's state, and offers Left and Right as its
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
        trailingContent = control,
    )
}

private val SideSteps = mapOf(Key.DirectionLeft to -1, Key.DirectionRight to 1)

/** The entry [by] places on, wrapping round, so either direction reaches every value of a setting. */
internal inline fun <reified T : Enum<T>> T.step(by: Int): T {
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
