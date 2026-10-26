package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A home-screen tile: a surface with a border that turns to the accent colour while the tile, or anything
 * inside it, has focus. The border follows the corner style.
 *
 * Pass [onClick] for a tile that is a single focus target (a recent app). Leave it null for a tile whose
 * children take focus (the apps list).
 */
@Composable
fun Tile(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    var hasFocus by remember { mutableStateOf(false) }
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.tile
    val clickable = onClick?.let { Modifier.clickable(interactionSource = null, indication = null, onClick = it) }
    Column(
        modifier = modifier
            .onFocusChanged { hasFocus = it.hasFocus }
            .then(clickable ?: Modifier)
            .background(colors.surface, shape)
            .border(TileDefaults.BorderWidth, if (hasFocus) colors.borderFocused else colors.border, shape)
            .padding(TileDefaults.PADDING_DP.dp),
        content = content,
    )
}

object TileDefaults {
    /** Border width, focused or not, so focus changes colour without moving anything. */
    val BorderWidth = 2.dp

    /**
     * Inset between a tile's edge and its content. Anything inside with its own background or border uses
     * `KimonoTheme.shapes.nested(PADDING_DP)` so its corners stay parallel to the tile's.
     */
    const val PADDING_DP = 20
}
