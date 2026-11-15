package io.github.ranjandas.kimono.data.apps

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Which apps were opened lately, from the system's usage stats. Reading them needs usage access, a special
 * permission the user grants in Settings (or over adb on TVs that hide that screen), so [hasAccess] comes first.
 */
interface UsageHistory {
    fun hasAccess(): Boolean

    /** Packages brought to the foreground within [window], most recent first. Empty without access. */
    suspend fun recentPackages(window: Duration = DefaultWindow): List<String>

    /**
     * How long each package was on screen since [since], longest first, leaving out Kimono itself. Empty without
     * access. The system keeps these totals in day-sized buckets, so a start mid-day is rounded to its bucket.
     */
    suspend fun screenTime(since: Instant): List<ScreenTime>

    /**
     * Whether this TV has a usage access screen to send the user to. Google TV hides it, so there the only way in
     * is adb, and Kimono doesn't ask (#188).
     */
    fun canOpenAccessSettings(): Boolean

    /** Opens the usage access screen in Settings. False when this device has none. */
    fun openAccessSettings(): Boolean

    companion object {
        val DefaultWindow: Duration = Duration.ofDays(7)

        fun create(context: Context): UsageHistory = SystemUsageHistory(context)
    }
}

/** How long one package was in the foreground. */
data class ScreenTime(val packageName: String, val time: Duration)

/** Packages that were on screen at all, longest first. */
internal fun longestFirst(times: List<ScreenTime>): List<ScreenTime> =
    times.filter { !it.time.isZero && !it.time.isNegative }.sortedByDescending { it.time }

/** One time an app came to the foreground. */
internal data class AppUse(val packageName: String, val timeMillis: Long)

/** Each package once, by its latest use, most recent first. */
internal fun mostRecentFirst(uses: List<AppUse>): List<String> = uses
    .groupBy { it.packageName }
    .mapValues { (_, byPackage) -> byPackage.maxOf { it.timeMillis } }
    .entries
    .sortedByDescending { it.value }
    .map { it.key }

internal class SystemUsageHistory(context: Context, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    UsageHistory {
    private val appContext = context.applicationContext
    private val usageStats = appContext.getSystemService(UsageStatsManager::class.java)
    private val appOps = appContext.getSystemService(AppOpsManager::class.java)

    override fun hasAccess(): Boolean = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        appContext.packageName,
    ) == AppOpsManager.MODE_ALLOWED

    override suspend fun recentPackages(window: Duration): List<String> {
        if (!hasAccess()) return emptyList()
        return withContext(ioDispatcher) {
            val end = System.currentTimeMillis()
            mostRecentFirst(readUses(end - window.toMillis(), end)).filter { it != appContext.packageName }
        }
    }

    override suspend fun screenTime(since: Instant): List<ScreenTime> {
        if (!hasAccess()) return emptyList()
        return withContext(ioDispatcher) {
            val stats = usageStats.queryAndAggregateUsageStats(since.toEpochMilli(), System.currentTimeMillis())
            longestFirst(
                stats.values
                    .filter { it.packageName != appContext.packageName }
                    .map { ScreenTime(it.packageName, Duration.ofMillis(it.totalTimeInForeground)) },
            )
        }
    }

    // Asked once: the screen doesn't come and go while Kimono runs. Visible through this module's <queries>.
    private val hasAccessSettings by lazy {
        appContext.packageManager.resolveActivity(accessSettingsIntent(), 0) != null
    }

    override fun canOpenAccessSettings(): Boolean = hasAccessSettings

    override fun openAccessSettings(): Boolean = try {
        appContext.startActivity(accessSettingsIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    private fun accessSettingsIntent() =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.fromParts("package", appContext.packageName, null))

    private fun readUses(begin: Long, end: Long): List<AppUse> {
        val events = usageStats.queryEvents(begin, end) ?: return emptyList()
        val event = UsageEvents.Event()
        val uses = mutableListOf<AppUse>()
        while (events.getNextEvent(event)) {
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                uses += AppUse(event.packageName, event.timeStamp)
            }
        }
        return uses
    }
}
