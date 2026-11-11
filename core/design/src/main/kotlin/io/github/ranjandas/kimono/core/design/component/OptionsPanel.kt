package io.github.ranjandas.kimono.core.design.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A panel of actions over the dimmed screen, for the options of whatever had focus. The first option takes
 * focus, so the D-pad is on it straight away, and Back closes the panel without choosing anything.
 *
 * Focus stays in the panel: the D-pad stops at the first and last option rather than leaving for the page
 * underneath. It used to slip out past the first option to the top bar, and then Back and OK went to the page, so the
 * panel stayed on screen with nothing focused and no way to close it. Left and Right have nowhere to go in the one
 * column, so the panel keeps them too: in all apps they used to reach the page and switch workspace.
 *
 * Back is taken as a key here rather than only through a back handler: Compose spends the first Back on clearing
 * focus, which left the panel on screen, unfocused and deaf to the D-pad, until a second press. The back handler
 * covers a Back that doesn't arrive as a key, so it still closes the panel rather than reaching the page.
 *
 * The panel usually opens from a held OK, and that key is still down when the first option takes focus. Its
 * remaining repeats and release are ignored, so they can't pick the first option; OK works again from the
 * next fresh press.
 *
 * A [message] under the title makes the panel a wider question with a paragraph to read before choosing, such as a
 * disclosure that has to be agreed to.
 */
@Composable
fun OptionsPanel(
    title: String,
    options: List<Option>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val firstOption = remember { FocusRequester() }
    // Plain state: nothing is drawn from it, so changing it needn't recompose.
    val okGate = remember { OkGate() }
    LaunchedEffect(Unit) { runCatching { firstOption.requestFocus() } }
    BackHandler(onBack = onDismiss)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScrimColor)
            // Taps outside the panel close it. The D-pad never reaches this, which is why Back closes it too.
            .focusProperties { canFocus = false }
            .clickable(interactionSource = null, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Tile(
            modifier = Modifier
                .width(if (message == null) PanelWidth else MessagePanelWidth)
                .focusProperties { onExit = { cancelFocusChange() } }
                .focusGroup()
                .onPreviewKeyEvent { event ->
                    // Close on release, so the release doesn't reach whatever is under the panel.
                    if (event.key == Key.Back && event.type == KeyEventType.KeyUp) onDismiss()
                    event.key == Key.Back || okGate.swallows(event)
                }
                .onKeyEvent { it.key in SidewaysKeys },
        ) {
            // The title is an app's name, so it keeps its own case.
            SectionHeader(title, caps = false)
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = KimonoSpacing.S),
                    style = KimonoTheme.typography.body,
                    color = KimonoTheme.colors.textMuted,
                )
            }
            Column(
                modifier = Modifier.padding(top = KimonoSpacing.S),
                verticalArrangement = Arrangement.spacedBy(KimonoSpacing.XXS),
            ) {
                options.forEachIndexed { index, option ->
                    ListRow(
                        text = option.label,
                        onClick = option.onSelect,
                        modifier = if (index == 0) Modifier.focusRequester(firstOption) else Modifier,
                        trailing = option.detail,
                    )
                }
            }
        }
    }
}

/** Swallows OK until a fresh press: the repeats and release of the held OK that opened the panel. */
private class OkGate {
    private var armed = false

    fun swallows(event: KeyEvent): Boolean {
        if (event.key !in OkKeys) return false
        if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.repeatCount == 0) armed = true
        return !armed
    }
}

private val SidewaysKeys = setOf(Key.DirectionLeft, Key.DirectionRight)

private val PanelWidth = 360.dp
private val MessagePanelWidth = 560.dp
private val ScrimColor = Color.Black.copy(alpha = 0.72f)

/** One line in an [OptionsPanel]. */
@Immutable
data class Option(val label: String, val detail: String? = null, val onSelect: () -> Unit)
