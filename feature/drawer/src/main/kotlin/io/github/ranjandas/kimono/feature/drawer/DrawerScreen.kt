package io.github.ranjandas.kimono.feature.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.AppOptionsPanel
import io.github.ranjandas.kimono.core.design.component.Hint
import io.github.ranjandas.kimono.core.design.component.HintBar
import io.github.ranjandas.kimono.core.design.component.ListRow
import io.github.ranjandas.kimono.core.design.component.LocalBackdrop
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gap = 14.dp

/**
 * The all apps page: every installed app as text, grouped into watch, listen, play and system, with search. Up
 * from the list reaches the search field, where OK brings up the on-screen keyboard (or the phone remote's).
 * Letters and digits typed on a hardware keyboard narrow the list from anywhere, Del deletes a character, and
 * Back clears the search before it calls [onClose]. It fills the page it's given; the launcher draws the top bar
 * and padding around it.
 */
@Composable
fun DrawerScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    workspaces: WorkspaceState,
    onTogglePin: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onUninstallApp: (InstalledApp) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var unavailable by remember { mutableStateOf<InstalledApp?>(null) }
    var options by remember { mutableStateOf<InstalledApp?>(null) }
    val sections = remember(apps, query) { drawerSections(apps, query) }
    val focus = remember { DrawerFocus() }
    val scope = rememberCoroutineScope()
    val hasResults = sections.isNotEmpty()
    val panelOpen = options != null
    // Pins made here go where the options panel says: all apps lists everything, so home.
    val pinTarget = workspaces.current.pinTarget
    // The screen itself takes focus on its first frame, so letters typed straight after opening reach the
    // search before the list has laid out its rows; the list takes over as soon as it can.
    LaunchedEffect(Unit) { runCatching { focus.root.requestFocus() } }
    // With nothing listed the screen keeps focus itself, so typing and Back still reach it.
    LaunchedEffect(hasResults, panelOpen) {
        when {
            panelOpen || focus.searching -> Unit
            hasResults -> focus.list.requestFocusWhenLaidOut()
            else -> runCatching { focus.root.requestFocus() }
        }
    }
    ClearAfterDelay(unavailable) { unavailable = null }
    // Back clears the search first, so a mistyped query doesn't cost the whole screen, and leaves the search line
    // for the list so focus is somewhere visible. The screen's key handler takes Back while it has focus; the back
    // handler is for when nothing does.
    val goBack: () -> Unit = {
        when {
            focus.searching -> {
                query = ""
                // The list may have been empty for the query, so focus it once the cleared list has its rows.
                scope.launch { focus.list.requestFocusWhenLaidOut() }
            }

            query.isEmpty() -> onClose()

            else -> query = ""
        }
    }
    BackHandler(onBack = goBack)
    Box(modifier = modifier.fillMaxSize()) {
        DrawerContent(
            sections = sections,
            pinned = workspaces.pinsOf(pinTarget).toSet(),
            query = query,
            onQueryChange = { query = it },
            // Reads the query state at the time of the key, not the value from the last frame: keys typed
            // faster than the screen recomposes would otherwise each edit the same old query and drop letters.
            // While the search field has focus it does its own typing.
            onTypeKey = { event -> !focus.searching && event.editQuery(query)?.also { query = it } != null },
            onBack = goBack,
            onClickApp = { app ->
                if (onLaunchApp(app) is LaunchResult.Unavailable) unavailable = app
            },
            onShowOptions = { options = it },
            unavailable = unavailable,
            focus = focus,
        )
        options?.let { app ->
            AppOptionsPanel(
                appLabel = app.label,
                workspaceLabel = pinTarget.label,
                pinned = app.packageName in workspaces.pinsOf(pinTarget),
                onTogglePin = { onTogglePin(app) },
                onOpenAppInfo = { onOpenAppInfo(app) },
                onUninstall = { onUninstallApp(app) },
                onDismiss = { options = null },
            )
        }
    }
}

/** Clears [unavailable] a few seconds after it's set, so the note under the list doesn't stay. */
@Composable
private fun ClearAfterDelay(unavailable: InstalledApp?, onClear: () -> Unit) {
    val clear by rememberUpdatedState(onClear)
    LaunchedEffect(unavailable) {
        if (unavailable != null) {
            delay(STATUS_MILLIS)
            clear()
        }
    }
}

/** Where focus can go on the page: the page itself, the search field and the list. */
private class DrawerFocus {
    val root = FocusRequester()
    val search = FocusRequester()
    val list = FocusRequester()

    // Plain state: only key handling reads it, so it needn't recompose anything.
    var searching = false
}

/** The search field, the grouped list and the hints: everything the options panel sits over. */
@Composable
private fun DrawerContent(
    sections: List<DrawerSection>,
    pinned: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onTypeKey: (KeyEvent) -> Boolean,
    onBack: () -> Unit,
    onClickApp: (InstalledApp) -> Unit,
    onShowOptions: (InstalledApp) -> Unit,
    unavailable: InstalledApp?,
    focus: DrawerFocus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                val edited = onTypeKey(event)
                // Back acts on release, as the system's back does.
                if (event.key == Key.Back && event.type == KeyEventType.KeyUp) onBack()
                edited || event.key == Key.Back
            }
            // After the key handler, so the handler sees keys while the screen itself holds focus.
            .focusRequester(focus.root)
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(Gap),
    ) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            matches = sections.sumOf { it.apps.size },
            onFocusChange = { focus.searching = it },
            // Done typing: back to the list to pick a result.
            onDone = { runCatching { focus.list.requestFocus() } },
            focusRequester = focus.search,
        )
        // The list isn't on a frosted tile, so its rows keep their own fill to stay readable over a wallpaper.
        CompositionLocalProvider(LocalBackdrop provides null) {
            DrawerList(
                sections = sections,
                pinned = pinned,
                query = query,
                onClickApp = onClickApp,
                onShowOptions = onShowOptions,
                modifier = Modifier.fillMaxWidth().weight(1f).focusRequester(focus.list).focusRestorer(),
            )
        }
        unavailable?.let {
            Text(
                "${it.label} can't be opened. It may have been uninstalled or disabled.",
                style = KimonoTheme.typography.label,
                color = KimonoTheme.colors.error,
            )
        }
        DrawerHints(query)
    }
}

/** The grouped list: a heading per group, then its apps; nothing but a note when the search matches none. */
@Composable
private fun DrawerList(
    sections: List<DrawerSection>,
    pinned: Set<String>,
    query: String,
    onClickApp: (InstalledApp) -> Unit,
    onShowOptions: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        sections.forEach { section ->
            item(key = "group:${section.group.name}") {
                SectionHeader(
                    "~/${section.group.label}",
                    modifier = Modifier.padding(top = Gap, bottom = 4.dp),
                    icon = section.group.icon,
                    trailing = "${section.apps.size}",
                )
            }
            items(section.apps, key = { it.packageName }) { app ->
                ListRow(
                    text = app.label,
                    onClick = { onClickApp(app) },
                    icon = app.icon(section.group),
                    trailing = when {
                        app.packageName in pinned -> "pinned"
                        !app.isTvApp -> "phone app"
                        else -> null
                    },
                    onLongClick = { onShowOptions(app) },
                    shape = KimonoTheme.shapes.small,
                )
            }
        }
        if (sections.isEmpty()) {
            item(key = "empty") { EmptyResults(query) }
        }
    }
}

@Composable
private fun DrawerHints(query: String, modifier: Modifier = Modifier) {
    HintBar(
        listOf(
            Hint("▲ ok", "search"),
            Hint("ok", "launch"),
            Hint("hold ok", "options"),
            Hint("back", if (query.isEmpty()) "home" else "clear search"),
            Hint("◀ ▶", "workspace"),
        ),
        modifier,
    )
}

@Composable
private fun EmptyResults(query: String, modifier: Modifier = Modifier) {
    Text(
        "nothing matches \"$query\"",
        modifier = modifier.padding(top = Gap),
        style = KimonoTheme.typography.body,
        color = KimonoTheme.colors.textMuted,
    )
}

/**
 * The query after [event], or null when the key isn't part of typing. Keys the list needs (the D-pad, OK,
 * Back) are left alone; a printable character is appended and Del removes the last one.
 */
private fun KeyEvent.editQuery(query: String): String? {
    if (type != KeyEventType.KeyDown) return null
    val character = utf16CodePoint.toChar()
    return when {
        key == Key.Backspace || key == Key.Delete -> query.dropLast(1).takeIf { query.isNotEmpty() }
        character.isLetterOrDigit() || (character == ' ' && query.isNotEmpty()) -> query + character
        else -> null
    }
}

private const val STATUS_MILLIS = 4_000L

@Preview(device = Devices.TV_1080p)
@Composable
private fun DrawerScreenPreview() {
    val apps = listOf(
        "Jellyfin" to AppCategory.Video,
        "Plex" to AppCategory.Video,
        "Spotify" to AppCategory.Audio,
        "Retro" to AppCategory.Game,
        "Files" to AppCategory.Other,
    ).map { (label, category) -> InstalledApp("pkg.$label", "pkg.$label.Main", label, category, isTvApp = true) }
    KimonoTheme {
        DrawerScreen(
            apps = apps,
            onLaunchApp = { LaunchResult.Launched },
            workspaces = WorkspaceState(),
            onTogglePin = {},
            onOpenAppInfo = {},
            onUninstallApp = {},
            onClose = {},
        )
    }
}
