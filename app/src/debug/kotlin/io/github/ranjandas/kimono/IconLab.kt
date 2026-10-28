package io.github.ranjandas.kimono

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.core.design.component.Monogram
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.icon.Glyph
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val LabelWidth = 200.dp
private val SquareSize = 64.dp
private val WordmarkWidth = 144.dp
private val ColumnGap = 20.dp

/** The icon lab page: Left and Right cycle the theme, starting from the stored one; Up and Down scroll. */
@Composable
internal fun IconLab(settingsStore: SettingsStore, loadApps: () -> List<LabApp>) {
    val settings by settingsStore.settings.collectAsStateWithLifecycle(initialValue = null)
    var themeStep by remember { mutableIntStateOf(0) }
    val stored = ThemeId.fromName(settings?.themeName)
    val theme = ThemeId.entries[(stored.ordinal + themeStep).mod(ThemeId.entries.size)]
    val apps by produceState<List<LabApp>?>(null, loadApps) { value = withContext(Dispatchers.Default) { loadApps() } }
    KimonoTheme(theme) {
        Column(
            Modifier
                .fillMaxSize()
                .background(KimonoTheme.colors.background)
                .onPreviewKeyEvent {
                    val step = when (it.key) {
                        Key.DirectionLeft -> -1
                        Key.DirectionRight -> 1
                        else -> 0
                    }
                    if (step != 0 && it.type == KeyEventType.KeyDown) themeStep += step
                    step != 0
                }
                .padding(horizontal = 48.dp, vertical = 27.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Icon lab · ${theme.displayName} · ← → theme",
                style = KimonoTheme.typography.headline,
                color = KimonoTheme.colors.text,
            )
            HeaderRow()
            val loaded = apps
            if (loaded == null) Caption("Reading icons…") else LabList(loaded)
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
        Heading("App", LabelWidth)
        Heading("Colour", SquareSize)
        Heading("Official", SquareSize)
        Heading("Silhouette", SquareSize)
        Heading("Wordmark", WordmarkWidth)
        Heading("Monogram", SquareSize)
    }
}

@Composable
private fun LabList(apps: List<LabApp>) {
    val first = remember { FocusRequester() }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(apps) { index, app ->
            LabRow(app, if (index == 0) Modifier.focusRequester(first) else Modifier)
        }
    }
    LaunchedEffect(apps) { if (apps.isNotEmpty()) runCatching { first.requestFocus() } }
}

@Composable
private fun LabRow(app: LabApp, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.tile
    Row(
        modifier
            .fillMaxWidth()
            .focusable(interactionSource = interaction)
            .border(2.dp, if (focused) colors.borderFocused else Color.Transparent, shape)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(ColumnGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(LabelWidth)) {
            Text(app.label, style = KimonoTheme.typography.title, color = colors.text, maxLines = 1)
            if (!app.enabled) Caption("disabled")
        }
        val icon = app.glyphs.icon
        Box(Modifier.size(SquareSize), contentAlignment = Alignment.Center) {
            if (icon != null) {
                val bitmap = remember(icon) { icon.asImageBitmap() }
                Image(bitmap, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
        GlyphCell(app.glyphs.monochrome, SquareSize, SquareSize)
        GlyphCell(app.glyphs.silhouette, SquareSize, SquareSize)
        GlyphCell(app.glyphs.wordmark, WordmarkWidth, SquareSize)
        Monogram(app.label, Modifier.size(SquareSize))
    }
}

// The glyph tinted in the accent on a plate, the way a tile would show it, with how much of its box it fills.
@Composable
private fun GlyphCell(glyph: Glyph?, width: Dp, height: Dp) {
    val colors = KimonoTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width, height)
                .background(colors.accent.copy(alpha = 0.16f), KimonoTheme.shapes.small)
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (glyph == null) {
                Text("–", color = colors.textMuted)
            } else {
                Image(
                    remember(glyph) { glyph.bitmap.asImageBitmap() },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(colors.accent),
                )
            }
        }
        val note = glyph?.let { "${(it.coverage * 100).roundToInt()}%" + if (it.removedPlate) " plate" else "" }
        Caption(note ?: "", Modifier.height(18.dp))
    }
}

@Composable
private fun Heading(text: String, width: Dp) {
    Text(text, Modifier.width(width), style = KimonoTheme.typography.label, color = KimonoTheme.colors.textMuted)
}

@Composable
private fun Caption(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = KimonoTheme.typography.label, color = KimonoTheme.colors.textMuted, maxLines = 1)
}
