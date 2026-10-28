package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.AppMark
import io.github.ranjandas.kimono.core.design.component.AppMarks
import io.github.ranjandas.kimono.core.design.component.Mark
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.InstalledApp

/** What the last used tile and the recents row show. */
sealed interface RecentApps {
    /** Usage access isn't granted and Kimono hasn't opened anything yet, so the row asks for access instead. */
    data object NeedsAccess : RecentApps

    /** The apps opened most recently, newest first; may be empty. */
    @Immutable
    data class Apps(val apps: List<InstalledApp>) : RecentApps
}

/** The newest recent app, the one the last used tile shows. */
internal val RecentApps.lastUsed: InstalledApp?
    get() = (this as? RecentApps.Apps)?.apps?.firstOrNull()

/**
 * Home's big tile: the app opened last, as its large logo (or monogram) and its name, so getting back to it is one OK
 * (#108). Held OK opens its options. Before anything has been opened it says where the tile's content comes from.
 */
@Composable
internal fun LastUsedTile(
    app: InstalledApp?,
    onLaunchApp: (InstalledApp) -> Unit,
    onShowOptions: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    marks: AppMarks = AppMarks.None,
) {
    Tile(
        modifier = modifier,
        // Focusable even when empty, so D-pad moves around home stay the same once something has been opened.
        onClick = { app?.let(onLaunchApp) },
        onLongClick = app?.let { { onShowOptions(it) } },
    ) {
        SectionHeader("~/last used", icon = KimonoIcon.Continue)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app == null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("nothing opened yet", style = KimonoTheme.typography.headline)
                    Text(
                        "the app you open last shows here, one press away",
                        style = KimonoTheme.typography.body,
                        color = KimonoTheme.colors.textMuted,
                    )
                }
            } else {
                AppMark(app.label, marks[app.packageName], Modifier.fillMaxHeight())
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(app.label, style = HeroNameStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("press ok to open", style = KimonoTheme.typography.body, color = KimonoTheme.colors.textMuted)
                }
            }
        }
    }
}

/**
 * The four tiles under last used: the apps opened before it, or a tile asking for usage access when Kimono can't
 * read them and hasn't opened anything itself. Empty slots stay as placeholders so the row keeps its shape.
 */
@Composable
internal fun RecentsRow(
    recents: RecentApps,
    onLaunchApp: (InstalledApp) -> Unit,
    onShowOptions: (InstalledApp) -> Unit,
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier,
    marks: AppMarks = AppMarks.None,
) {
    // The newest is in the last used tile above.
    val apps = (recents as? RecentApps.Apps)?.apps.orEmpty().drop(1).take(RECENT_SLOTS)
    Row(
        modifier = modifier.focusRestorer().focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(TileGap),
    ) {
        repeat(RECENT_SLOTS) { slot ->
            val tileModifier = Modifier.weight(1f).fillMaxHeight()
            val app = apps.getOrNull(slot)
            when {
                recents is RecentApps.NeedsAccess && slot == 0 ->
                    PlaceholderTile("allow access", "press ok", onRequestAccess, tileModifier)

                app != null ->
                    RecentTile(app, marks[app.packageName], { onLaunchApp(app) }, { onShowOptions(app) }, tileModifier)

                else -> PlaceholderTile("recent", "none yet", {}, tileModifier)
            }
        }
    }
}

private val TileTitleStyle: TextStyle
    @Composable get() = KimonoTheme.typography.body.copy(fontWeight = FontWeight.Bold)

// Big enough to lead the tile, small enough that most app names fit beside the monogram on two lines at most.
private val HeroNameStyle: TextStyle
    @Composable get() = KimonoTheme.typography.headline.let {
        it.copy(
            fontSize = it.fontSize * HERO_SCALE,
            lineHeight = it.lineHeight * HERO_SCALE,
            fontWeight = FontWeight.Bold,
        )
    }

private const val HERO_SCALE = 1.8f

/** A recent app: its logo or monogram filling the tile, and its name under it, since a mark alone can be ambiguous. */
@Composable
private fun RecentTile(
    app: InstalledApp,
    mark: Mark?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tile(modifier = modifier, onClick = onClick, onLongClick = onLongClick) {
        AppMark(app.label, mark, Modifier.weight(1f))
        // A quarter-width tile fits about nine characters of body text.
        Text(
            app.label,
            modifier = Modifier.padding(top = 4.dp),
            style = TileTitleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlaceholderTile(title: String, detail: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Tile(modifier = modifier, onClick = onClick) {
        Icon(KimonoIcon.Recent, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.headline)
        Spacer(Modifier.weight(1f))
        Text(title, style = TileTitleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
