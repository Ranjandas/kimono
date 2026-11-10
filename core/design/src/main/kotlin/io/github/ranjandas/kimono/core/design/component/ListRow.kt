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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A row in a text list: an optional [leading] detail such as a number, an app's [mark] or else an [icon], the [text]
 * itself, and optional [trailing] detail, or [trailingContent] drawn knowing whether the row has focus. Only the
 * focused row has a fill (#133): focus eases in a lightly tinted fill, turns the icon to the accent and sets the text
 * in bold, as Omarchy marks its selected row (#158). Nothing moves, so a D-pad move down a list reads as one thing
 * lighting up after another (#126).
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
    icon: KimonoIcon? = null,
    mark: Mark? = null,
    trailing: String? = null,
    trailingContent: (@Composable (focused: Boolean) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val heldOk = remember { HeldOk() }
    val colors = KimonoTheme.colors
    val fill by animateFocusColor(if (focused) colors.rowFocused else Color.Transparent, label = "row fill")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ListRowDefaults.Height)
            .then(
                if (onLongClick == null) {
                    Modifier
                } else {
                    Modifier.onPreviewKeyEvent { heldOk.onKey(it, onLongClick) }
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .background(fill, shape)
            .padding(horizontal = KimonoSpacing.M),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            // Numbers are a quiet guide, not something to read.
            Text(
                leading,
                style = KimonoTheme.typography.value,
                color = colors.textMuted.copy(alpha = LEADING_ALPHA),
            )
        }
        RowGlyph(mark, icon, if (focused) colors.accent else colors.textMuted)
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = KimonoTheme.typography.body,
            color = colors.text,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        when {
            trailingContent != null -> trailingContent(focused)

            trailing != null -> Text(
                trailing,
                style = KimonoTheme.typography.value,
                color = if (focused) colors.textMutedFocused else colors.textMuted,
            )
        }
    }
}

/** The app's [mark] at icon size, or else the [icon], or nothing. */
@Composable
private fun RowGlyph(mark: Mark?, icon: KimonoIcon?, color: Color) {
    when {
        mark != null -> MarkGlyph(mark, color, Modifier.size(ListRowDefaults.MarkSize))
        icon != null -> Icon(icon, color = color)
    }
}

private const val LEADING_ALPHA = 0.6f

object ListRowDefaults {
    /** Row height, the same in every list so the apps tile and the drawer scroll at the same rhythm. */
    val Height = 30.dp

    /** An app logo's square in a row: a little taller than the body text beside it, as a logo needs. */
    val MarkSize = 18.dp
}
