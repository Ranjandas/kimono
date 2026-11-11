package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * A row of the system tile, set like a Settings row: a quiet [icon], the [name] in the text face, and the
 * [trailing] value pushed to the right edge. TalkBack reads it as one line, "[name], [spoken]".
 */
@Composable
internal fun SystemRow(
    icon: KimonoIcon,
    name: String,
    spoken: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    val colors = KimonoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = KimonoSpacing.XS)
            .clearAndSetSemantics { contentDescription = if (spoken.isEmpty()) name else "$name, $spoken" },
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, color = colors.textMuted)
        Text(
            name,
            modifier = Modifier.weight(1f),
            style = KimonoTheme.typography.body,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

/** A row whose value is plain text: muted for a figure, or in the text colour when [strong]. */
@Composable
internal fun SystemRow(
    icon: KimonoIcon,
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
) {
    SystemRow(icon, name, spoken = value, modifier = modifier) { SystemValue(value, strong = strong) }
}

@Composable
internal fun SystemValue(value: String, modifier: Modifier = Modifier, strong: Boolean = false) {
    val colors = KimonoTheme.colors
    Text(
        value,
        modifier = modifier,
        style = KimonoTheme.typography.value,
        color = if (strong) colors.text else colors.textMuted,
        textAlign = TextAlign.End,
        maxLines = 1,
    )
}

/**
 * A thin bar filled to [fraction] in the second accent, like the Settings slider, then [value] in a fixed column
 * so the bars of a tile line up. Nearly full turns it amber.
 */
@Composable
internal fun RowScope.MeterValue(fraction: Float, value: String) {
    val colors = KimonoTheme.colors
    val fill = if (fraction >= METER_HIGH) colors.warning else colors.accentSecondary
    Box(
        modifier = Modifier
            .width(MeterWidth)
            .height(MeterHeight)
            .background(colors.surfaceVariant),
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(fill))
    }
    val style = KimonoTheme.typography.value
    SystemValue(value, modifier = Modifier.width(style.ems(METER_VALUE_EMS)))
}

/** A small dot in [color] before [value], for a state such as the temperature. */
@Composable
internal fun RowScope.DotValue(color: Color, value: String) {
    Box(Modifier.size(DotSize).background(color, CircleShape))
    SystemValue(value, strong = true)
}

/** The hairline between a tile's groups, as Settings draws between its sections. */
@Composable
internal fun SystemRule(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(vertical = KimonoSpacing.XS)
            .fillMaxWidth()
            .height(1.dp)
            .background(KimonoTheme.colors.border),
    )
}

private const val METER_HIGH = 0.85f

// Mono figures are 0.6 em each, so "1.2 / 2.0 GB" needs about 7.5 ems.
private const val METER_VALUE_EMS = 7.5f
private val MeterWidth = 88.dp
private val MeterHeight = 4.dp
private val DotSize = 7.dp
