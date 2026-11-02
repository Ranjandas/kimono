package io.github.ranjandas.kimono.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.design.component.AppCard
import io.github.ranjandas.kimono.core.design.component.AppMarks
import io.github.ranjandas.kimono.core.design.component.HintBar
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.TextButton
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.text.count
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult

/**
 * A workspace that is just its apps, watch or play: a grid of their logos (or monograms) filling the page, pins
 * first. OK opens one and held OK its options; Back goes home. An empty workspace offers all apps instead, so
 * there's always something to press. [apps] is null while the app list is still loading.
 */
@Composable
internal fun AppGridPage(
    workspace: Workspace,
    apps: List<InstalledApp>?,
    actions: PageActions,
    modifier: Modifier = Modifier,
) {
    val gridFocus = remember { FocusRequester() }
    val emptyFocus = remember { FocusRequester() }
    var unavailable by remember { mutableStateOf<InstalledApp?>(null) }
    val hasApps = !apps.isNullOrEmpty()
    val loaded = apps != null
    // Also when the page's apps change: an app unpinned from a page it doesn't belong on leaves it, and focus would
    // otherwise go with it, up to the top bar.
    LaunchedEffect(hasApps, loaded, actions.panelOpen, apps?.size) {
        when {
            actions.panelOpen || !loaded -> Unit
            hasApps -> gridFocus.requestFocusWhenLaidOut()
            else -> emptyFocus.requestFocusWhenLaidOut()
        }
    }
    BackHandler(onBack = actions::onGoHome)
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap)) {
        Tile(modifier = Modifier.fillMaxWidth().weight(1f)) {
            SectionHeader(workspace.label, icon = workspace.icon, trailing = apps?.let { count(it.size, "app") })
            when {
                !loaded -> Note("loading apps…")

                hasApps -> AppGrid(
                    apps = apps.orEmpty(),
                    onClickApp = { app ->
                        unavailable = app.takeIf { actions.onLaunchApp(it) is LaunchResult.Unavailable }
                    },
                    onShowOptions = actions.onShowOptions,
                    marks = actions.marks,
                    modifier = Modifier.weight(1f).focusRequester(gridFocus),
                )

                else -> EmptyWorkspace(workspace, actions::onOpenAllApps, emptyFocus)
            }
            unavailable?.let {
                Text(
                    "${it.label} can't be opened. It may have been uninstalled or disabled.",
                    modifier = Modifier.padding(top = KimonoSpacing.S),
                    style = KimonoTheme.typography.label,
                    color = KimonoTheme.colors.error,
                )
            }
        }
        HintBar(GridHints, Modifier.fillMaxWidth().height(HomeDimens.HintBarHeight))
    }
}

@Composable
private fun AppGrid(
    apps: List<InstalledApp>,
    onClickApp: (InstalledApp) -> Unit,
    onShowOptions: (InstalledApp) -> Unit,
    marks: AppMarks,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        // Coming back to the grid lands on the app that was focused last.
        modifier = modifier.focusRestorer(),
        // Inside the grid rather than around it: the grid clips its content, so a focused card growing at an edge
        // (most visibly the top row) needs room within the grid or its border is cut off.
        contentPadding = PaddingValues(KimonoSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(
                label = app.label,
                onClick = { onClickApp(app) },
                onLongClick = { onShowOptions(app) },
                mark = marks[app.packageName],
            )
        }
    }
}

@Composable
private fun EmptyWorkspace(
    workspace: Workspace,
    onOpenAllApps: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(KimonoSpacing.M)) {
        Spacer(Modifier.height(KimonoSpacing.S))
        Note("nothing in ${workspace.label} yet")
        TextButton(
            "all apps",
            onClick = onOpenAllApps,
            modifier = Modifier.focusRequester(focusRequester),
            icon = KimonoIcon.Apps,
            primary = true,
        )
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = KimonoSpacing.S),
        style = KimonoTheme.typography.body,
        color = KimonoTheme.colors.textMuted,
    )
}

// Six monograms a row fill the page at about the size of a recent tile's.
private const val GRID_COLUMNS = 6
