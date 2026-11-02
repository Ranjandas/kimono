package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId

@Preview(device = Devices.TV_1080p)
@Composable
private fun ComponentsSquarePreview() {
    ComponentSampler(ThemeId.TokyoNight, CornerStyle.Square)
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun ComponentsRoundPreview() {
    ComponentSampler(ThemeId.RosePine, CornerStyle.Round)
}

/** Every base component in one theme and corner style. Also shown by the placeholder home screen. */
@Composable
fun ComponentSampler(theme: ThemeId, cornerStyle: CornerStyle, modifier: Modifier = Modifier) {
    KimonoTheme(theme = theme, cornerStyle = cornerStyle) {
        Column(
            modifier = modifier.background(KimonoTheme.colors.background).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(KimonoSpacing.XXL),
        ) {
            Tile(modifier = Modifier.width(560.dp)) {
                SectionHeader("continue", trailing = theme.displayName)
                Text("Severance", style = KimonoTheme.typography.display)
                Text("s02 · e04 — Woe's Hollow", color = KimonoTheme.colors.textMuted)
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
                ) {
                    TextButton("resume", onClick = {}, icon = KimonoIcon.Play, primary = true)
                    TextButton("from start", onClick = {})
                    TextButton("details", onClick = {})
                }
            }
            Tile(modifier = Modifier.width(560.dp)) {
                SectionHeader("apps", trailing = "3 apps")
                Column(verticalArrangement = Arrangement.spacedBy(KimonoSpacing.XXS)) {
                    listOf("Jellyfin", "Plex", "YouTube").forEachIndexed { index, label ->
                        ListRow(text = label, onClick = {}, leading = "%02d".format(index + 1))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                KeyHint("◀ ▶ ▲ ▼", "move focus")
                KeyHint("ok", "launch")
                KeyHint("hold ok", "app options")
            }
        }
    }
}
