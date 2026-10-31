package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The muted line at the top of a tile or list: an optional [icon], a [title] such as `apps` in the section style,
 * and optional [trailing] detail.
 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, icon: KimonoIcon? = null, trailing: String? = null) {
    val colors = KimonoTheme.colors
    val style = KimonoTheme.typography.label
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, color = colors.textMuted, style = style)
            Text(
                title,
                modifier = Modifier.semantics { heading() },
                color = colors.textMuted,
                style = KimonoTheme.typography.section,
            )
        }
        if (trailing != null) {
            Text(trailing, color = colors.textMuted, style = style)
        }
    }
}
