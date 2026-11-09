package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The muted line at the top of a tile or list: an optional [icon], a [title] such as `apps` and optional [trailing]
 * detail. Like Omarchy's panels (#152), both are small spaced-out UPPERCASE mono, while TalkBack still reads the
 * words as written. A title that is content, such as an app's name, passes [caps] false to keep its own case and
 * the text face.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: KimonoIcon? = null,
    trailing: String? = null,
    caps: Boolean = true,
) {
    val colors = KimonoTheme.colors
    val style = if (caps) KimonoTheme.typography.section else KimonoTheme.typography.label
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(icon, color = colors.textMuted, style = KimonoTheme.typography.label)
            Text(
                if (caps) title.uppercase() else title,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = title
                },
                color = colors.textMuted,
                style = style,
            )
        }
        if (trailing != null) {
            Text(
                if (caps) trailing.uppercase() else trailing,
                modifier = Modifier.semantics { contentDescription = trailing },
                color = colors.textMuted,
                style = style,
            )
        }
    }
}
