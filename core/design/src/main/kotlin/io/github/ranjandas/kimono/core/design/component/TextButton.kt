package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A text button, optionally led by an icon: a quiet fill at rest, with no outline, since only tiles carry borders
 * (#125). It eases to the accent colour while focused. A [primary] button (the one action a tile is mostly for, such
 * as resume) has accent text unfocused too.
 *
 * The default [shape] assumes the button sits inside a [Tile], so its radius is the tile's minus the padding.
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: KimonoIcon? = null,
    primary: Boolean = false,
    shape: Shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = KimonoTheme.colors
    val content = when {
        focused -> colors.onAccent
        primary -> colors.accent
        else -> colors.text
    }
    val fill by animateFocusColor(if (focused) colors.accent else colors.surfaceVariant, label = "button fill")
    Row(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .background(fill, shape)
            .heightIn(min = 44.dp)
            .padding(horizontal = KimonoSpacing.XXL),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, color = content, style = KimonoTheme.typography.title)
        }
        Text(text, color = content, style = KimonoTheme.typography.title)
    }
}
