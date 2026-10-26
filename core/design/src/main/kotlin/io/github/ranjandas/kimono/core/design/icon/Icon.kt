package io.github.ranjandas.kimono.core.design.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * Draws [icon] as text in the bundled font, so it lines up with the label next to it and takes its colour.
 * Icons are decorative: the label carries the meaning, so TalkBack skips them (PLAN §6.5).
 */
@Composable
fun Icon(
    icon: KimonoIcon,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = KimonoTheme.typography.body,
) {
    Text(
        text = icon.text,
        modifier = modifier.clearAndSetSemantics {},
        color = color,
        style = style,
    )
}
