package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
 */
@Composable
fun ListRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: String? = null,
    trailing: String? = null,
    shape: Shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ListRowDefaults.Height)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
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

object ListRowDefaults {
    /** Row height, the same in every list so the apps tile and the drawer scroll at the same rhythm. */
    val Height = 30.dp
}
