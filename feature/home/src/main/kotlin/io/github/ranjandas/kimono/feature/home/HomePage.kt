package io.github.ranjandas.kimono.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.design.component.HintBar
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.InstalledApp

/**
 * The home workspace: the last used app and the ones before it on the left, system info and the apps list on the right,
 * key hints below. [apps] is null while the app list is still loading. With no apps at all, focus goes to
 * [emptyFocus] (the workspace's button in the top bar) so the remote still has something to move from.
 */
@Composable
internal fun HomePage(
    apps: List<InstalledApp>?,
    actions: PageActions,
    recents: RecentApps,
    onRequestUsageAccess: () -> Unit,
    theme: ThemeId,
    focusArea: HomeArea,
    onFocusArea: (HomeArea) -> Unit,
    emptyFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val appsFocus = remember { FocusRequester() }
    val hasApps = !apps.isNullOrEmpty()
    val loaded = apps != null
    // Start on the apps list, the one tile that always has something to do. Come back to it when the options panel
    // closes, so focus is never left on whatever the system picks first.
    LaunchedEffect(hasApps, loaded, actions.panelOpen) {
        when {
            actions.panelOpen || !loaded -> Unit
            hasApps -> appsFocus.requestFocusWhenLaidOut()
            else -> emptyFocus.requestFocusWhenLaidOut()
        }
    }
    // Home is the root of the launcher, so Back never leaves it; it returns focus to the start position.
    BackHandler { runCatching { appsFocus.requestFocus() } }
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap)) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
            ) {
                LastUsedTile(
                    app = recents.lastUsed,
                    onLaunchApp = { actions.onLaunchApp(it) },
                    onShowOptions = actions.onShowOptions,
                    modifier = Modifier.fillMaxWidth().weight(1f).tracksFocus(HomeArea.LastUsed, onFocusArea),
                )
                RecentsRow(
                    recents = recents,
                    onLaunchApp = { actions.onLaunchApp(it) },
                    onShowOptions = actions.onShowOptions,
                    onRequestAccess = onRequestUsageAccess,
                    modifier = Modifier.fillMaxWidth().height(HomeDimens.RecentsHeight)
                        .tracksFocus(HomeArea.Recents, onFocusArea),
                )
            }
            Column(
                modifier = Modifier.width(HomeDimens.SideColumnWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(HomeDimens.Gap),
            ) {
                SystemInfoTile(theme, Modifier.fillMaxWidth())
                AppsTile(
                    apps = apps,
                    onLaunchApp = actions.onLaunchApp,
                    onShowOptions = actions.onShowOptions,
                    workspace = Workspace.Home,
                    onOpenAllApps = actions::onOpenAllApps,
                    modifier = Modifier.fillMaxWidth().weight(1f).tracksFocus(HomeArea.Apps, onFocusArea),
                    listFocusRequester = appsFocus,
                )
            }
        }
        HintBar(homeHints(focusArea), Modifier.fillMaxWidth().height(HomeDimens.HintBarHeight))
    }
}
