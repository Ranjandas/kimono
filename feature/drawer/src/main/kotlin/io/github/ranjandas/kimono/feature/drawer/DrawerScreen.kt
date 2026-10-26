package io.github.ranjandas.kimono.feature.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
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
import io.github.ranjandas.kimono.core.design.component.KeyHint
import io.github.ranjandas.kimono.core.design.component.ListRow
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import kotlinx.coroutines.delay

private object DrawerDimens {
    val ScreenPaddingH = 20.dp
    val ScreenPaddingV = 12.dp
    val Gap = 14.dp
}

/**
 * The full-screen app drawer: every installed app as text, grouped into watch, listen, play and system,
 * with type-to-filter search. Letters and digits typed on the remote's keyboard narrow the list, Del
 * deletes a character, and Back clears the search before it closes the drawer.
 */
@Composable
fun DrawerScreen(
    apps: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> LaunchResult,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var unavailable by remember { mutableStateOf<InstalledApp?>(null) }
    val sections = remember(apps, query) { drawerSections(apps, query) }
    val listFocus = remember { FocusRequester() }
    val hasResults = sections.isNotEmpty()
    LaunchedEffect(hasResults) {
        if (hasResults) listFocus.requestFocusWhenLaidOut()
    }
    LaunchedEffect(unavailable) {
        if (unavailable != null) {
            delay(STATUS_MILLIS)
            unavailable = null
        }
    }
    // Back clears the search first, so a mistyped query doesn't cost the whole screen.
    BackHandler {
        if (query.isEmpty()) onClose() else query = ""
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KimonoTheme.colors.background)
            .onPreviewKeyEvent { event ->
                val edited = event.editQuery(query)
                if (edited != null) query = edited
                edited != null
            }
            .padding(horizontal = DrawerDimens.ScreenPaddingH, vertical = DrawerDimens.ScreenPaddingV),
        verticalArrangement = Arrangement.spacedBy(DrawerDimens.Gap),
    ) {
        SearchLine(query, matches = sections.sumOf { it.apps.size })
        DrawerList(
            sections = sections,
            query = query,
            onClickApp = { app ->
                if (onLaunchApp(app) is LaunchResult.Unavailable) unavailable = app
            },
            modifier = Modifier.fillMaxWidth().weight(1f).focusRequester(listFocus).focusRestorer(),
        )
        unavailable?.let {
            Text(
                "${it.label} can't be opened. It may have been uninstalled or disabled.",
                style = KimonoTheme.typography.label,
                color = KimonoTheme.colors.error,
            )
        }
        HintBar(query)
    }
}

/** The grouped list: a heading per group, then its apps; nothing but a note when the search matches none. */
@Composable
private fun DrawerList(
    sections: List<DrawerSection>,
    query: String,
    onClickApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        sections.forEach { section ->
            item(key = "group:${section.group.name}") {
                SectionHeader(
                    "~/${section.group.label}",
                    modifier = Modifier.padding(top = DrawerDimens.Gap, bottom = 4.dp),
                    trailing = "${section.apps.size}",
                )
            }
            items(section.apps, key = { it.packageName }) { app ->
                ListRow(
                    text = app.label,
                    onClick = { onClickApp(app) },
                    trailing = if (app.isTvApp) null else "phone app",
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
private fun HintBar(query: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyHint("a…z", "search")
        KeyHint("ok", "launch")
        KeyHint("back", if (query.isEmpty()) "home" else "clear search")
    }
}

@Composable
private fun SearchLine(query: String, matches: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Text("~/apps ❯ ", style = KimonoTheme.typography.headline, color = KimonoTheme.colors.accent)
            Text(
                query.ifEmpty { "type to search" },
                style = KimonoTheme.typography.headline,
                color = if (query.isEmpty()) KimonoTheme.colors.textMuted else KimonoTheme.colors.text,
            )
            Text("▌", style = KimonoTheme.typography.headline, color = KimonoTheme.colors.accent)
        }
        Text(
            "$matches apps",
            style = KimonoTheme.typography.label,
            color = KimonoTheme.colors.textMuted,
        )
    }
}

@Composable
private fun EmptyResults(query: String, modifier: Modifier = Modifier) {
    Text(
        "nothing matches \"$query\"",
        modifier = modifier.padding(top = DrawerDimens.Gap),
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

/** Requests focus, retrying for a few frames while the lazy list composes its first rows. */
private suspend fun FocusRequester.requestFocusWhenLaidOut() {
    repeat(FOCUS_ATTEMPTS) {
        if (runCatching { requestFocus() }.getOrDefault(false)) return
        withFrameNanos { }
    }
}

private const val FOCUS_ATTEMPTS = 10
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
        DrawerScreen(apps = apps, onLaunchApp = { LaunchResult.Launched }, onClose = {})
    }
}
