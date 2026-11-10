package io.github.ranjandas.kimono.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/*
 * The controls a setting row shows for its value, after Omarchy's panels (#156): chips for a short choice, a square
 * switch for on and off, and a stepped bar for an ordered value. They only draw the value; the row itself takes
 * Left, Right and OK, and TalkBack reads the value as the row's state, so none of these is read out.
 */

/** Every option side by side, the picked one filled. On the focused row its outline is the accent. */
@Composable
internal fun Chips(options: List<String>, selected: Int, focused: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.small
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.XS),
    ) {
        options.forEachIndexed { index, option ->
            val picked = index == selected
            val outline = when {
                picked && focused -> colors.accent
                picked -> colors.border
                else -> colors.border.copy(alpha = QUIET_ALPHA)
            }
            Text(
                option,
                modifier = Modifier
                    .background(if (picked) colors.surfaceVariant else Color.Transparent, shape)
                    .border(ControlBorder, outline, shape)
                    .padding(horizontal = KimonoSpacing.S, vertical = ChipInset),
                style = KimonoTheme.typography.value,
                color = if (picked) colors.text else colors.textMuted,
                maxLines = 1,
            )
        }
    }
}

/** A square switch: the knob sits right in the second accent when [on], left and muted when off. */
@Composable
internal fun Toggle(on: Boolean, focused: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.small
    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .size(ToggleWidth, ToggleHeight)
            .background(colors.surfaceVariant, shape)
            .border(ControlBorder, if (focused) colors.accent else Color.Transparent, shape)
            .padding(KimonoSpacing.XS),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(ToggleHeight - KimonoSpacing.S)
                .background(if (on) colors.accentSecondary else colors.textMuted.copy(alpha = QUIET_ALPHA), shape),
        )
    }
}

/** [steps] bars with the first [filled] in the second accent, then [label]: an ordered value such as a timeout. */
@Composable
internal fun Steps(filled: Int, steps: Int, label: String, focused: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(StepGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(steps) { index ->
            Box(
                Modifier
                    .size(StepWidth, StepHeight)
                    .background(if (index < filled) colors.accentSecondary else colors.border),
            )
        }
        ValueText(label, focused, Modifier.padding(start = KimonoSpacing.S))
    }
}

/** [value] between chevrons that show only while the row is [focused], for a setting with too many values for chips. */
@Composable
internal fun Cycle(value: String, focused: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val arrows = if (focused) colors.accent else Color.Transparent
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.ChevronLeft, color = arrows, style = KimonoTheme.typography.value)
        ValueText(value, focused)
        Icon(KimonoIcon.ChevronRight, color = arrows, style = KimonoTheme.typography.value)
    }
}

/** A value in mono, brighter on the focused row. */
@Composable
internal fun ValueText(value: String, focused: Boolean, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    Text(
        value,
        modifier = modifier,
        style = KimonoTheme.typography.value,
        color = if (focused) colors.text else colors.textMuted,
        maxLines = 1,
    )
}

/** The hairline between sections of one panel, as Omarchy draws between Brightness, Text size and Scale. */
@Composable
internal fun Rule(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(vertical = KimonoSpacing.S)
            .fillMaxWidth()
            .height(1.dp)
            .background(KimonoTheme.colors.border),
    )
}

private const val QUIET_ALPHA = 0.55f
private val ControlBorder = 1.dp
private val ChipInset = 1.dp
private val ToggleWidth = 36.dp
private val ToggleHeight = 20.dp
private val StepWidth = 14.dp
private val StepHeight = 3.dp
private val StepGap = 3.dp
