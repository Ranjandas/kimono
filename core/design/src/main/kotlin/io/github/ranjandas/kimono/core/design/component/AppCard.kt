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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * An app in a grid: its [AppMark] ([mark], or its monogram without one) with the name under it, on up to two lines.
 * Focus eases the card onto the variant surface with an accent border and grows it a little (#126); OK is
 * [onClick] and held OK [onLongClick], as on a [ListRow].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    mark: Mark? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val heldOk = remember { HeldOk() }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)
    val fill by animateFocusColor(if (focused) colors.surfaceVariant else Color.Transparent, label = "card fill")
    val border by animateFocusColor(if (focused) colors.borderFocused else Color.Transparent, label = "card border")
    val scale by animateFocusScale(focused, FocusDefaults.CARD_SCALE, label = "card scale")
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onPreviewKeyEvent { heldOk.onKey(it, onLongClick) }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .background(fill, shape)
            .border(TileDefaults.BorderWidth, border, shape)
            .padding(KimonoSpacing.M),
        verticalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalMarkFocused provides focused) { AppMark(label, mark, Modifier.fillMaxWidth()) }
        Text(
            label,
            style = KimonoTheme.typography.body,
            color = if (focused) colors.text else colors.textMuted,
            textAlign = TextAlign.Center,
            // Two lines, so "Play Movies" isn't cut to "Play Mov…" (#131); always two, so the cards in a row line up.
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
