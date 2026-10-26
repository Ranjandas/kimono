package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.InstalledApp

/** What the recents row shows. */
sealed interface RecentApps {
    /** Usage access isn't granted, so the row asks for it instead. */
    data object NeedsAccess : RecentApps

    /** The apps opened most recently, newest first; may be empty. */
    @Immutable
    data class Apps(val apps: List<InstalledApp>) : RecentApps
}

/**
 * The four tiles under continue watching: the last apps opened, or a tile asking for usage access when Kimono
 * can't read them. Empty slots stay as placeholders so the row keeps its shape.
 */
@Composable
internal fun RecentsRow(
    recents: RecentApps,
    onLaunchApp: (InstalledApp) -> Unit,
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val apps = (recents as? RecentApps.Apps)?.apps.orEmpty().take(RECENT_SLOTS)
    Row(
        modifier = modifier.focusRestorer().focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(TileGap),
    ) {
        repeat(RECENT_SLOTS) { slot ->
            val tileModifier = Modifier.weight(1f).fillMaxHeight()
            val app = apps.getOrNull(slot)
            when {
                recents is RecentApps.NeedsAccess && slot == 0 ->
                    RecentTile(KimonoIcon.Recent, "allow access", "press ok", onRequestAccess, tileModifier)

                app != null -> RecentTile(app.icon, app.label, "recent", { onLaunchApp(app) }, tileModifier)

                else -> RecentTile(KimonoIcon.Recent, "recent", "none yet", {}, tileModifier)
            }
        }
    }
}

private val TileTitleStyle: TextStyle
    @Composable get() = KimonoTheme.typography.body.copy(fontWeight = FontWeight.Bold)

@Composable
private fun RecentTile(
    icon: KimonoIcon,
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tile(modifier = modifier, onClick = onClick) {
        Icon(icon, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.headline)
        Spacer(Modifier.weight(1f))
        // Body size over two lines: a quarter-width tile only fits about nine characters a line.
        Text(title, style = TileTitleStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            detail,
            style = KimonoTheme.typography.label,
            color = KimonoTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val RECENT_SLOTS = 4

// The same gap as between home's other tiles.
private val TileGap = 14.dp
