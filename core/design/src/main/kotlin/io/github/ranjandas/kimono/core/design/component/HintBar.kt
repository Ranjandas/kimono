package io.github.ranjandas.kimono.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/** The row of remote keys along the bottom of a screen, after a remote icon, saying what each one does now. */
@Composable
fun HintBar(hints: List<Hint>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.Remote, color = KimonoTheme.colors.textMuted, style = KimonoTheme.typography.label)
        hints.forEach { KeyHint(it.key, it.action) }
    }
}

/** One entry in a [HintBar]: a remote [key] and the [action] it does. */
@Immutable
data class Hint(val key: String, val action: String)
