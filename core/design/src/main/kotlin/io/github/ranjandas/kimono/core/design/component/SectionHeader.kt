package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/** The muted line at the top of a tile or list: a [title] such as `~/apps`, and optional [trailing] detail. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val colors = KimonoTheme.colors
    val style = KimonoTheme.typography.label
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, modifier = Modifier.semantics { heading() }, color = colors.textMuted, style = style)
        if (trailing != null) {
            Text(trailing, color = colors.textMuted, style = style)
        }
    }
}
