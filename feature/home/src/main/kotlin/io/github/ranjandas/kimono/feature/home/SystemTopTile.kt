package io.github.ranjandas.kimono.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The top-style tile: how the box is doing right now, as meters for memory and CPU clock, then heat and network,
 * then the apps on screen most today. Android keeps other apps' CPU and memory private, so the app list is screen
 * time, from usage access, rather than a process list.
 */
@Composable
internal fun SystemTopTile(screenTime: ScreenTimeToday, modifier: Modifier = Modifier) {
    val stats by rememberTopStats()
    Tile(modifier = modifier) {
        SectionHeader("~/top", icon = KimonoIcon.Activity)
        DeviceLines(stats)
        TodayLines(screenTime)
    }
}

@Composable
private fun DeviceLines(stats: TopStats?, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (stats == null) {
            InfoLine(KimonoIcon.Memory, "memory", "…")
        } else {
            InfoLine(KimonoIcon.Memory, "memory") {
                Meter(stats.memory.fraction, formatMemory(stats.memory), Modifier.weight(1f))
            }
            val clock = stats.clock
            if (clock == null) {
                InfoLine(KimonoIcon.Cpu, "cpu", "${stats.cores} cores")
            } else {
                InfoLine(KimonoIcon.Cpu, "cpu") {
                    Meter(clock.fraction, formatClock(clock.currentKhz), Modifier.weight(1f))
                }
            }
            InfoLine(KimonoIcon.Heat, "heat") { HeatValue(stats.heat) }
            val traffic = stats.traffic
            InfoLine(
                KimonoIcon.Traffic,
                "network",
                traffic?.let { "${formatRate(it.inPerSecond)} in · ${formatRate(it.outPerSecond)} out" } ?: "…",
            )
        }
    }
}

@Composable
private fun HeatValue(heat: Heat, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val color = when (heat) {
        Heat.Normal -> colors.text
        Heat.Warm -> colors.warning
        Heat.Hot -> colors.error
    }
    Text(heat.label, modifier = modifier, style = KimonoTheme.typography.body, color = color, maxLines = 1)
}

/** Up to [TODAY_APPS] apps, each on its own line under one `today` key. */
@Composable
private fun TodayLines(screenTime: ScreenTimeToday, modifier: Modifier = Modifier) {
    val muted = KimonoTheme.colors.textMuted
    val apps = (screenTime as? ScreenTimeToday.Apps)?.apps.orEmpty().take(TODAY_APPS)
    Column(modifier) {
        if (apps.isEmpty()) {
            val note = if (screenTime == ScreenTimeToday.NeedsAccess) "needs usage access" else "nothing yet"
            InfoLine(KimonoIcon.Recent, "today") {
                Text(note, style = KimonoTheme.typography.body, color = muted, maxLines = 1)
            }
        }
        apps.forEachIndexed { index, (app, time) ->
            val first = index == 0
            InfoLine(if (first) KimonoIcon.Recent else null, if (first) "today" else "") {
                Text(
                    app.label,
                    modifier = Modifier.weight(1f),
                    style = KimonoTheme.typography.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(formatScreenTime(time), style = KimonoTheme.typography.body, color = muted, maxLines = 1)
            }
        }
    }
}

/** A bar filled to [fraction], then [value] in a fixed column so the bars line up. Nearly full turns it amber. */
@Composable
private fun Meter(fraction: Float, value: String, modifier: Modifier = Modifier) {
    val colors = KimonoTheme.colors
    val fill = if (fraction >= METER_HIGH) colors.warning else colors.accent
    val style = KimonoTheme.typography.body
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(KimonoTheme.shapes.small)
                .background(colors.surfaceVariant),
        ) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(fill))
        }
        Text(
            value,
            modifier = Modifier.width(style.ems(METER_VALUE_EMS)),
            style = style,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

private const val TODAY_APPS = 3
private const val METER_HIGH = 0.85f
private const val METER_VALUE_EMS = 6.5f
