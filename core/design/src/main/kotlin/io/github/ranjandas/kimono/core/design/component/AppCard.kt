package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * An app in a grid: its [AppMark] ([mark], or its monogram without one) with the name under it. Focus lifts the card
 * onto the variant surface with an accent border; OK is [onClick] and held OK [onLongClick], as on a [ListRow].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    mark: ImageBitmap? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val heldOk = remember { HeldOk() }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)
    Column(
        modifier = modifier
            .onPreviewKeyEvent { heldOk.onKey(it, onLongClick) }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .background(if (focused) colors.surfaceVariant else Color.Transparent, shape)
            .border(TileDefaults.BorderWidth, if (focused) colors.borderFocused else Color.Transparent, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(label, mark, Modifier.fillMaxWidth())
        Text(
            label,
            style = KimonoTheme.typography.body,
            color = if (focused) colors.text else colors.textMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
