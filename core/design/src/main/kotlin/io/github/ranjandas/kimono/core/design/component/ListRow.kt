package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A row in a text list: an optional [leading] detail such as a number, a focus marker, the [text] itself,
 * and optional [trailing] detail. Focus fills the row and turns the text to the accent colour, with no
 * movement, so a D-pad move down a list reads as one thing lighting up after another.
 *
 * [onLongClick] is held OK, which opens the row's options.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: String? = null,
    trailing: String? = null,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ListRowDefaults.Height)
            .heldOk(onLongClick)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .background(if (focused) colors.surfaceVariant else colors.surface, shape)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Text(leading, style = KimonoTheme.typography.label, color = colors.textMuted)
        }
        Text(
            if (focused) "›" else " ",
            modifier = Modifier.width(10.dp),
            style = KimonoTheme.typography.body,
            color = colors.accent,
        )
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = KimonoTheme.typography.body,
            color = if (focused) colors.accent else colors.text,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Text(trailing, style = KimonoTheme.typography.label, color = colors.textMuted)
        }
    }
}

/**
 * Calls [onLongClick] when OK is held. Compose's `clickable` turns the OK key into a click but has no
 * notion of holding it, so the key is handled here: the first repeat (about half a second) is the long
 * press, and the rest of the repeats and the key release are swallowed so no click follows.
 */
@Composable
private fun Modifier.heldOk(onLongClick: (() -> Unit)?): Modifier {
    if (onLongClick == null) return this
    var held by remember { mutableStateOf(false) }
    return onPreviewKeyEvent { event ->
        when {
            event.key !in OkKeys -> false

            event.type == KeyEventType.KeyUp -> held.also { held = false }

            event.nativeKeyEvent.repeatCount == 1 -> {
                held = true
                onLongClick()
                true
            }

            else -> held
        }
    }
}

private val OkKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

object ListRowDefaults {
    /** Row height, the same in every list so the apps tile and the drawer scroll at the same rhythm. */
    val Height = 30.dp
}
