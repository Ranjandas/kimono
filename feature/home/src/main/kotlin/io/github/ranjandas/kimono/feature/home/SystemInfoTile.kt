package io.github.ranjandas.kimono.feature.home

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.SystemPanel
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId

/** Home's system tile, as the fetch view or the top view, as chosen in settings. */
@Composable
internal fun SystemTile(
    panel: SystemPanel,
    theme: ThemeId,
    screenTime: ScreenTimeToday,
    modifier: Modifier = Modifier,
) {
    when (panel) {
        SystemPanel.Fetch -> SystemInfoTile(theme, modifier)
        SystemPanel.Top -> SystemTopTile(screenTime, modifier)
    }
}

/** The fetch-style tile: what the box is and how it's doing, one `key value` line each. */
@Composable
internal fun SystemInfoTile(theme: ThemeId, modifier: Modifier = Modifier) {
    val uptime by rememberUptime()
    val storage by rememberStorage()
    val network by rememberNetworkStatus()
    Tile(modifier = modifier) {
        SectionHeader("~/system", icon = KimonoIcon.Info)
        InfoLine(KimonoIcon.Device, "device", Build.MODEL)
        InfoLine(KimonoIcon.Os, "os", "Android ${Build.VERSION.RELEASE}")
        InfoLine(KimonoIcon.Uptime, "uptime", formatUptime(uptime))
        InfoLine(KimonoIcon.Storage, "storage", storage?.let(::formatStorage) ?: "…")
        InfoLine(network.icon, "network", network.label)
        InfoLine(KimonoIcon.Palette, "theme", theme.displayName.lowercase())
        InfoLine(KimonoIcon.Font, "font", "Noto Sans")
    }
}

@Composable
internal fun InfoLine(icon: KimonoIcon, key: String, value: String, modifier: Modifier = Modifier) {
    InfoLine(icon, key, modifier) {
        Text(value, style = KimonoTheme.typography.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * An icon, a [key] in the theme's second accent and the [value] after it. A null [icon] leaves its space blank, for
 * a line that carries on the one above.
 */
@Composable
internal fun InfoLine(
    icon: KimonoIcon?,
    key: String,
    modifier: Modifier = Modifier,
    value: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // A blank icon still takes its width, so the keys stay in a column.
        Icon(icon ?: KimonoIcon.Info, color = if (icon == null) Color.Transparent else KimonoTheme.colors.textMuted)
        val style = KimonoTheme.typography.body
        // A column wide enough for the longest key, in ems so it grows with the text size setting.
        Text(
            key,
            modifier = Modifier.width(style.ems(INFO_KEY_EMS)),
            style = style,
            color = KimonoTheme.colors.accentSecondary,
            maxLines = 1,
        )
        value()
    }
}

/** [count] ems of this style's text, as a width. */
@Composable
internal fun TextStyle.ems(count: Float): Dp = with(LocalDensity.current) { fontSize.toDp() } * count

private const val INFO_KEY_EMS = 4.8f
