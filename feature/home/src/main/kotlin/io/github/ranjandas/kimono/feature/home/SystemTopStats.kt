package io.github.ranjandas.kimono.feature.home

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.PowerManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.time.Duration
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * What the top view shows, read every couple of seconds like top itself. Android doesn't let an app see other
 * processes (their /proc entries are hidden and /proc/stat is blocked), so these are whole-device numbers, and
 * each is null where this device doesn't share it. Null until the first read.
 */
@Composable
internal fun rememberTopStats(): State<TopStats?> {
    val context = LocalContext.current
    return produceState<TopStats?>(null, context) {
        val reader = TopReader(context.applicationContext)
        while (true) {
            value = withContext(Dispatchers.IO) { reader.read() }
            delay(TOP_REFRESH_MILLIS)
        }
    }
}

internal data class TopStats(
    val memory: Memory,
    val cores: Int,
    val clock: CpuClock?,
    val heat: Heat,
    /** Null on the first read, since a rate needs two. */
    val traffic: Traffic?,
)

internal data class Memory(val usedBytes: Long, val totalBytes: Long) {
    val fraction: Float get() = fractionOf(usedBytes, totalBytes)
}

/** The cores' current clock, averaged, against the fastest any of them can go. Not load, which apps can't read. */
internal data class CpuClock(val currentKhz: Long, val maxKhz: Long) {
    val fraction: Float get() = fractionOf(currentKhz, maxKhz)
}

/** The system's thermal status, in three steps a viewer cares about. */
internal enum class Heat(val label: String) {
    Normal("normal"),
    Warm("warm"),

    /** Hot enough that the system slows things down to cool off. */
    Hot("hot, slowing down"),
}

/** Bytes a second through every network interface. */
internal data class Traffic(val inPerSecond: Long, val outPerSecond: Long)

/** The device's received and sent byte counters at [atMillis] since boot. */
internal data class TrafficSample(val rxBytes: Long, val txBytes: Long, val atMillis: Long) {
    /** The rate since [earlier], or null when no time has passed or a counter went back (an interface reset). */
    fun rateSince(earlier: TrafficSample): Traffic? {
        val millis = atMillis - earlier.atMillis
        val rx = rxBytes - earlier.rxBytes
        val tx = txBytes - earlier.txBytes
        return if (millis <= 0 || rx < 0 || tx < 0) {
            null
        } else {
            Traffic(rx * MILLIS_PER_SECOND / millis, tx * MILLIS_PER_SECOND / millis)
        }
    }
}

internal fun heatOf(thermalStatus: Int): Heat = when {
    thermalStatus <= PowerManager.THERMAL_STATUS_NONE -> Heat.Normal
    thermalStatus <= PowerManager.THERMAL_STATUS_MODERATE -> Heat.Warm
    else -> Heat.Hot
}

/** The average current clock over the cores that report one, against the highest maximum; null if none do. */
internal fun cpuClockOf(readings: List<CpuClock>): CpuClock? = if (readings.isEmpty()) {
    null
} else {
    CpuClock(readings.map { it.currentKhz }.average().toLong(), readings.maxOf { it.maxKhz })
}

/** Memory in decimal gigabytes, like storage: `1.4 / 3.8 GB`. */
internal fun formatMemory(memory: Memory): String = String.format(
    Locale.ROOT,
    "%.1f / %.1f GB",
    memory.usedBytes.toDouble() / BYTES_PER_GB,
    memory.totalBytes.toDouble() / BYTES_PER_GB,
)

/** A clock speed: `1.4 GHz`, or `850 MHz` below one gigahertz. */
internal fun formatClock(khz: Long): String = if (khz >= KHZ_PER_GHZ) {
    String.format(Locale.ROOT, "%.1f GHz", khz.toDouble() / KHZ_PER_GHZ)
} else {
    "${khz / KHZ_PER_MHZ} MHz"
}

/** A rate in decimal units, one decimal from megabytes up: `0 B/s`, `40 KB/s`, `1.2 MB/s`. */
internal fun formatRate(bytesPerSecond: Long): String = when {
    bytesPerSecond >= BYTES_PER_MB -> String.format(Locale.ROOT, "%.1f MB/s", bytesPerSecond.toDouble() / BYTES_PER_MB)
    bytesPerSecond >= BYTES_PER_KB -> "${bytesPerSecond / BYTES_PER_KB} KB/s"
    else -> "$bytesPerSecond B/s"
}

/** Time on screen: `1h 12m`, `20m`, or `<1m`. */
internal fun formatScreenTime(time: Duration): String {
    val hours = time.toHours()
    val minutes = time.toMinutes() % MINUTES_PER_HOUR
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

private fun fractionOf(part: Long, whole: Long): Float =
    if (whole <= 0) 0f else (part.toFloat() / whole).coerceIn(0f, 1f)

/** Reads [TopStats], keeping the last traffic sample so each read can give a rate. */
private class TopReader(context: Context) {
    private val activity = context.getSystemService(ActivityManager::class.java)
    private val power = context.getSystemService(PowerManager::class.java)
    private val cores = Runtime.getRuntime().availableProcessors()
    private var lastTraffic: TrafficSample? = null

    fun read(): TopStats {
        val traffic = readTraffic()
        val rate = lastTraffic?.let { earlier -> traffic?.rateSince(earlier) }
        lastTraffic = traffic
        return TopStats(readMemory(), cores, readClock(), heatOf(power.currentThermalStatus), rate)
    }

    private fun readMemory(): Memory {
        val info = ActivityManager.MemoryInfo().also(activity::getMemoryInfo)
        return Memory(usedBytes = info.totalMem - info.availMem, totalBytes = info.totalMem)
    }

    /** Clock speeds from sysfs, where Android still lets apps read them; offline cores have no files. */
    private fun readClock(): CpuClock? = cpuClockOf(
        (0 until cores).mapNotNull { core ->
            val dir = File("/sys/devices/system/cpu/cpu$core/cpufreq")
            val current = readNumber(File(dir, "scaling_cur_freq"))
            val max = readNumber(File(dir, "cpuinfo_max_freq"))
            if (current != null && max != null) CpuClock(current, max) else null
        },
    )

    private fun readTraffic(): TrafficSample? {
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        return if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) {
            null
        } else {
            TrafficSample(rx, tx, SystemClock.elapsedRealtime())
        }
    }

    private fun readNumber(file: File): Long? = runCatching { file.readText().trim().toLong() }.getOrNull()
}

private const val TOP_REFRESH_MILLIS = 2_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val MINUTES_PER_HOUR = 60
private const val BYTES_PER_KB = 1_000L
private const val BYTES_PER_MB = 1_000_000L
private const val BYTES_PER_GB = 1_000_000_000.0
private const val KHZ_PER_MHZ = 1_000L
private const val KHZ_PER_GHZ = 1_000_000L
