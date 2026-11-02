package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.layout.layout
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.design.component.AppMarks
import io.github.ranjandas.kimono.core.design.component.ListRow
import io.github.ranjandas.kimono.core.design.component.ListRowDefaults
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.text.count
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import kotlinx.coroutines.delay

/**
 * The numbered app list on the home screen, holding the current workspace's apps. OK opens the focused app and
 * held OK its options. Each row shows the app's logo from [marks], the same one as its tile, or else its brand or
 * category icon (#132). If it can't be opened (uninstalled or disabled meanwhile), the tile says so instead of
 * crashing. [apps] is null while the list is still loading.
 *
 * It shows the first [MAX_APPS] apps, then a last row that opens all apps, which lists them all.
 */
@Composable
fun AppsTile(
    apps: List<InstalledApp>?,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    onShowOptions: (InstalledApp) -> Unit,
    workspace: Workspace,
    onOpenAllApps: () -> Unit,
    modifier: Modifier = Modifier,
    marks: AppMarks = AppMarks.None,
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
        SectionHeader(workspace.label, icon = workspace.icon, trailing = apps?.let { count(it.size, "app") })
        // A different workspace is a different list, so it starts again: at the top, and with focus coming back to
        // its first row rather than to whichever app had focus before. That app may still be in the new list (Home
        // switches workspace after the list has taken focus), and the focus restorer would land on it.
        key(workspace) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = KimonoSpacing.S)
                    .wholeRows()
                    // Coming back to the list lands on the row that was focused last.
                    .focusRequester(listFocusRequester)
                    .focusRestorer(),
            ) {
                itemsIndexed(apps.orEmpty().take(MAX_APPS), key = { _, app -> app.packageName }) { index, app ->
                    ListRow(
                        text = app.label,
                        onClick = {
                            status = when (onLaunchApp(app)) {
                                LaunchResult.Launched -> LaunchStatus.Opening(app)
                                is LaunchResult.Unavailable -> LaunchStatus.Unavailable(app)
                            }
                        },
                        leading = "%02d".format(index + 1),
                        icon = app.icon,
                        mark = marks[app.packageName],
                        onLongClick = { onShowOptions(app) },
                        // The list is the right edge of the screen, so Right switches workspace (see
                        // WorkspaceSwitcher) instead of jumping to the top bar.
                        modifier = Modifier.focusProperties { right = FocusRequester.Cancel },
                        trailing = if ((status as? LaunchStatus.Opening)?.app == app) "opening…" else null,
                    )
                }
                if (!apps.isNullOrEmpty()) {
                    item(key = ALL_APPS_KEY) {
                        ListRow(
                            text = "all apps",
                            onClick = onOpenAllApps,
                            leading = "··",
                            icon = KimonoIcon.Apps,
                            modifier = Modifier.focusProperties { right = FocusRequester.Cancel },
                            trailing = "▶",
                        )
                    }
                }
            }
        }
        AppsNotes(apps, status)
    }
}

/**
 * Makes the list a whole number of rows tall, at the top of the space it's given, so no row is ever cut by the
 * edge. A D-pad move then scrolls by exactly one row, so the rows stay whole as the list scrolls too. (A fade
 * at the edges did the same job, but its offscreen layer cost the TV's GPU about 4 ms a frame.)
 */
private fun Modifier.wholeRows(): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedHeight) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val row = ListRowDefaults.Height.roundToPx()
    // Too short for even one row (a tiny window), it keeps all it has.
    val height = (constraints.maxHeight / row * row).takeIf { it > 0 } ?: constraints.maxHeight
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = height))
    // Takes all the height it was given, so what's below the list stays where it was.
    layout(placeable.width, constraints.maxHeight) { placeable.place(0, 0) }
}

/** What the tile says under the list: that apps are loading or the workspace is empty, or that one won't open. */
@Composable
private fun AppsNotes(apps: List<InstalledApp>?, status: LaunchStatus?) {
    Column {
        val emptyText = when {
            apps == null -> "loading apps…"
            apps.isEmpty() -> "nothing pinned to this workspace yet"
            else -> null
        }
        emptyText?.let {
            Text(
                it,
                modifier = Modifier.padding(top = KimonoSpacing.S),
                style = KimonoTheme.typography.body,
                color = KimonoTheme.colors.textMuted,
            )
        }
        (status as? LaunchStatus.Unavailable)?.let {
            Text(
                "${it.app.label} can't be opened. It may have been uninstalled or disabled.",
                modifier = Modifier.padding(top = KimonoSpacing.S),
                style = KimonoTheme.typography.label,
                color = KimonoTheme.colors.error,
            )
        }
    }
}

private sealed interface LaunchStatus {
    val app: InstalledApp

    data class Opening(override val app: InstalledApp) : LaunchStatus

    data class Unavailable(override val app: InstalledApp) : LaunchStatus
}

private const val MAX_APPS = 20

// Package names have a dot, so this key never clashes with an app's.
private const val ALL_APPS_KEY = "all-apps"

private const val STATUS_MILLIS = 4_000L
