package io.github.ranjandas.kimono.feature.home

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId

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
private fun InfoLine(icon: KimonoIcon, key: String, value: String) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, color = KimonoTheme.colors.textMuted)
        val style = KimonoTheme.typography.body
        // A column wide enough for the longest key, in ems so it grows with the text size setting.
        Text(
            key,
            modifier = Modifier.width(with(LocalDensity.current) { style.fontSize.toDp() } * INFO_KEY_EMS),
            style = style,
            color = KimonoTheme.colors.accentSecondary,
            maxLines = 1,
        )
        Text(value, style = KimonoTheme.typography.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private const val INFO_KEY_EMS = 4.8f
