package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/** A remote key drawn as a keycap, followed by what it does: `[ok] launch`. */
@Composable
fun KeyHint(key: String, action: String, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val style = KimonoTheme.typography.label
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = key,
            modifier = Modifier
                .border(1.dp, colors.border, KimonoTheme.shapes.small)
                .padding(horizontal = 6.dp, vertical = 1.dp),
            color = colors.text,
            style = style,
        )
        Text(action, color = colors.textMuted, style = style)
    }
}
