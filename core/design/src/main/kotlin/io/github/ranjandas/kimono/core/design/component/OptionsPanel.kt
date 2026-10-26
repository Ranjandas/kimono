package io.github.ranjandas.kimono.core.design.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A panel of actions over the dimmed screen, for the options of whatever had focus. The first option takes
 * focus, so the D-pad is on it straight away, and Back closes the panel without choosing anything.
 */
@Composable
fun OptionsPanel(title: String, options: List<Option>, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val firstOption = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstOption.requestFocus() } }
    BackHandler(onBack = onDismiss)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScrimColor)
            // Taps outside the panel close it; the D-pad can't reach this, which is why Back closes it too.
            .clickable(interactionSource = null, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Tile(modifier = Modifier.width(PanelWidth)) {
            SectionHeader(title)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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

private val PanelWidth = 360.dp
private val ScrimColor = Color.Black.copy(alpha = 0.72f)

/** One line in an [OptionsPanel]. */
@Immutable
data class Option(val label: String, val detail: String? = null, val onSelect: () -> Unit)
