package io.github.ranjandas.kimono.feature.home

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.ranjandas.kimono.core.data.settings.SystemPanel
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon

/** Home's system tile, as the fetch view or the top view, as chosen in settings. */
@Composable
internal fun SystemTile(panel: SystemPanel, screenTime: ScreenTimeToday, modifier: Modifier = Modifier) {
    when (panel) {
        SystemPanel.Fetch -> SystemInfoTile(modifier)
        SystemPanel.Top -> SystemTopTile(screenTime, modifier)
    }
}

/**
 * The fetch view: what the box is and how it's doing, in rows set like Settings' (a quiet icon, a plain name, the
 * value at the right edge), with storage as a bar.
 */
@Composable
internal fun SystemInfoTile(modifier: Modifier = Modifier) {
    val uptime by rememberUptime()
    val storage by rememberStorage()
    val network by rememberNetworkStatus()
    Tile(modifier = modifier) {
        SectionHeader("system", icon = KimonoIcon.Info)
        SystemRow(KimonoIcon.Device, Build.MODEL, "Android ${Build.VERSION.RELEASE}")
        SystemRow(network.icon, "Network", network.label, strong = true)
        SystemRow(KimonoIcon.Uptime, "Up for", formatUptime(uptime), strong = true)
        SystemRule()
        val free = storage?.let(::formatFree) ?: "…"
        SystemRow(KimonoIcon.Storage, "Storage", spoken = free) {
            MeterValue(storage?.usedFraction ?: 0f, free)
        }
    }
}

/** [count] ems of this style's text, as a width. */
@Composable
internal fun TextStyle.ems(count: Float): Dp = with(LocalDensity.current) { fontSize.toDp() } * count
