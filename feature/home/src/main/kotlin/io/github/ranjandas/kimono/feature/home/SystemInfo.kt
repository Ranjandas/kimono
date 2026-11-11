package io.github.ranjandas.kimono.feature.home

import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import java.time.Duration
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Time since boot, refreshed every minute: the only unit the tile shows below an hour. */
@Composable
internal fun rememberUptime(): State<Duration> = produceState(uptime()) {
    while (true) {
        delay(UPTIME_REFRESH_MILLIS)
        value = uptime()
    }
}

/** Free and total space on the data partition, read off the main thread every few minutes. */
@Composable
internal fun rememberStorage(): State<Storage?> = produceState<Storage?>(null) {
    while (true) {
        value = withContext(Dispatchers.IO) { runCatching { readStorage() }.getOrNull() }
        delay(STORAGE_REFRESH_MILLIS)
    }
}

internal data class Storage(val freeBytes: Long, val totalBytes: Long) {
    /** How full the storage is, from 0 to 1, for its bar. */
    val usedFraction: Float
        get() = if (totalBytes <= 0) 0f else ((totalBytes - freeBytes).toFloat() / totalBytes).coerceIn(0f, 1f)
}

/** Uptime in words a viewer reads at a glance: `3 days` (or `1 day`), `5h 12m`, `12m`. */
internal fun formatUptime(uptime: Duration): String {
    val days = uptime.toDays()
    val hours = uptime.toHours() % HOURS_PER_DAY
    val minutes = uptime.toMinutes() % MINUTES_PER_HOUR
    return when {
        days == 1L -> "1 day"
        days > 0 -> "$days days"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

/**
 * Free storage in decimal gigabytes, as Android's own settings count them, next to its bar: `22 GB free`, with a
 * decimal below ten (`3.4 GB free`) where a whole number would hide too much.
 */
internal fun formatFree(storage: Storage): String {
    val free = storage.freeBytes.toDouble() / BYTES_PER_GB
    val pattern = if (free < WHOLE_GB_FROM) "%.1f GB free" else "%.0f GB free"
    return String.format(Locale.ROOT, pattern, free)
}

private fun uptime(): Duration = Duration.ofMillis(SystemClock.elapsedRealtime())

private fun readStorage(): Storage {
    val stats = StatFs(Environment.getDataDirectory().path)
    return Storage(freeBytes = stats.availableBytes, totalBytes = stats.totalBytes)
}

private const val UPTIME_REFRESH_MILLIS = 60_000L
private const val STORAGE_REFRESH_MILLIS = 5 * 60_000L
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_HOUR = 60
private const val BYTES_PER_GB = 1_000_000_000.0
private const val WHOLE_GB_FROM = 10.0
