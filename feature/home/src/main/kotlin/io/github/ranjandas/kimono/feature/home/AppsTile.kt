package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.component.TileDefaults
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import kotlinx.coroutines.delay

/**
 * The numbered app list on the home screen. OK opens the focused app; if it can't be opened (uninstalled or
 * disabled meanwhile), the tile says so instead of crashing.
 *
 * Until workspaces (M1-10) and recents (M2-6) exist it shows the first [MAX_APPS] apps by name; the drawer
 * (M1-11) lists them all.
 */
@Composable
fun AppsTile(
    apps: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    modifier: Modifier = Modifier,
    listFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    var status by remember { mutableStateOf<LaunchStatus?>(null) }
    LaunchedEffect(status) {
        if (status != null) {
            delay(STATUS_MILLIS)
            status = null
        }
    }
    Tile(modifier = modifier) {
        SectionHeader("~/apps", trailing = "${apps.size} installed")
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
                // Coming back to the list lands on the row that was focused last.
                .focusRequester(listFocusRequester)
                .focusRestorer()
                .focusGroup(),
        ) {
            itemsIndexed(apps.take(MAX_APPS), key = { _, app -> app.packageName }) { index, app ->
                AppRow(
                    number = index + 1,
                    app = app,
                    opening = (status as? LaunchStatus.Opening)?.app == app,
                    onClick = {
                        status = when (onLaunchApp(app)) {
                            LaunchResult.Launched -> LaunchStatus.Opening(app)
                            is LaunchResult.Unavailable -> LaunchStatus.Unavailable(app)
                        }
                    },
                )
            }
        }
        (status as? LaunchStatus.Unavailable)?.let {
            Text(
                "${it.app.label} can't be opened. It may have been uninstalled or disabled.",
                modifier = Modifier.padding(top = 8.dp),
                style = KimonoTheme.typography.label,
                color = KimonoTheme.colors.error,
            )
        }
    }
}

@Composable
private fun AppRow(
    number: Int,
    app: InstalledApp,
    opening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .background(if (focused) colors.surfaceVariant else colors.surface, shape)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d".format(number), style = KimonoTheme.typography.label, color = colors.textMuted)
        Text(
            if (focused) "›" else " ",
            modifier = Modifier.width(10.dp),
            style = KimonoTheme.typography.body,
            color = colors.accent,
        )
        Text(
            app.label,
            modifier = Modifier.weight(1f),
            style = KimonoTheme.typography.body,
            color = if (focused) colors.accent else colors.text,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (opening) {
            Text("opening…", style = KimonoTheme.typography.label, color = colors.textMuted)
        }
    }
}

private sealed interface LaunchStatus {
    val app: InstalledApp

    data class Opening(override val app: InstalledApp) : LaunchStatus

    data class Unavailable(override val app: InstalledApp) : LaunchStatus
}

private const val MAX_APPS = 20
private const val STATUS_MILLIS = 4_000L
