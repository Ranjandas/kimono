package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/** A remote key in bold, followed by what it does: `ok launch`. No keycap box, so the hints stay quiet. */
@Composable
fun KeyHint(key: String, action: String, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val style = KimonoTheme.typography.label
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = key, color = colors.text, style = style, fontWeight = FontWeight.Bold)
        Text(action, color = colors.textMuted, style = style)
    }
}
