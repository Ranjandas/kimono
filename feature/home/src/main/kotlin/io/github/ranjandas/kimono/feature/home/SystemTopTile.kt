package io.github.ranjandas.kimono.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.ranjandas.kimono.core.design.component.SectionHeader
import io.github.ranjandas.kimono.core.design.component.Tile
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The top view: how the box is doing right now, as bars for memory and processor clock, then temperature and
 * network, and under a rule the apps watched most today. Android keeps other apps' CPU and memory private, so the
 * app list is screen time, from usage access, rather than a process list. Rows are set like Settings' rows.
 */
@Composable
internal fun SystemTopTile(screenTime: ScreenTimeToday, modifier: Modifier = Modifier) {
    val stats by rememberTopStats()
    Tile(modifier = modifier) {
        SectionHeader("right now", icon = KimonoIcon.Activity)
        DeviceRows(stats)
        SystemRule()
        SectionHeader("watched today")
        TodayRows(screenTime)
    }
}

@Composable
private fun DeviceRows(stats: TopStats?) {
    if (stats == null) {
        SystemRow(KimonoIcon.Memory, "Memory", "…")
        return
    }
    val memory = formatMemory(stats.memory)
    SystemRow(KimonoIcon.Memory, "Memory", spoken = memory) { MeterValue(stats.memory.fraction, memory) }
    val clock = stats.clock
    if (clock == null) {
        SystemRow(KimonoIcon.Cpu, "Processor", "${stats.cores} cores")
    } else {
        val speed = formatClock(clock.currentKhz)
        SystemRow(KimonoIcon.Cpu, "Processor", spoken = speed) { MeterValue(clock.fraction, speed) }
    }
    val colors = KimonoTheme.colors
    val heatColor = when (stats.heat) {
        Heat.Normal -> colors.success
        Heat.Warm -> colors.warning
        Heat.Hot -> colors.error
    }
    SystemRow(KimonoIcon.Heat, "Temperature", spoken = stats.heat.label) { DotValue(heatColor, stats.heat.label) }
    val traffic = stats.traffic
    SystemRow(
        KimonoIcon.Traffic,
        "Network",
        traffic?.let { "↓ ${formatRate(it.inPerSecond)}  ↑ ${formatRate(it.outPerSecond)}" } ?: "…",
    )
}

/** Up to [TODAY_APPS] apps, each with its own icon and its time on screen, or a note when there are none. */
@Composable
private fun TodayRows(screenTime: ScreenTimeToday) {
    val apps = (screenTime as? ScreenTimeToday.Apps)?.apps.orEmpty().take(TODAY_APPS)
    if (apps.isEmpty()) {
        val note = if (screenTime == ScreenTimeToday.NeedsAccess) "Needs usage access" else "Nothing yet"
        SystemRow(KimonoIcon.Recent, note, "")
    }
    apps.forEach { (app, time) -> SystemRow(app.icon, app.label, formatScreenTime(time)) }
}

private const val TODAY_APPS = 2
