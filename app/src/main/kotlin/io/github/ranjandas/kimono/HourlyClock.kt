package io.github.ranjandas.kimono

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

/**
 * The local time, updated at the top of each hour, for what changes through the day: Kimono's Yūgure sky moves
 * on from dawn to day, dusk and night, and Kimono Sumi's pattern changes at midnight.
 */
@Composable
internal fun rememberHourlyClock(): LocalDateTime = hourlyClock().value

@Composable
private fun hourlyClock(): State<LocalDateTime> = produceState(LocalDateTime.now()) {
    while (true) {
        delay(untilNextHour(LocalDateTime.now()).toMillis())
        value = LocalDateTime.now()
    }
}

/** How long from [now] until the next hour starts, plus a moment so the clock has surely moved on. */
internal fun untilNextHour(now: LocalDateTime): Duration =
    Duration.between(now, now.truncatedTo(ChronoUnit.HOURS).plusHours(1)).plus(SETTLE)

private val SETTLE = Duration.ofSeconds(1)
