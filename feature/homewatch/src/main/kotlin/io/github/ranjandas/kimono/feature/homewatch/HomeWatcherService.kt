package io.github.ranjandas.kimono.feature.homewatch

import android.accessibilityservice.AccessibilityService
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Google TV never offers a choice of home app, and its Home button always opens its own launcher. This service
 * notices that launcher coming to the front and opens Kimono over it, the way other TV launchers do without adb
 * (route B in ADR 0001). Google TV's home shows for a moment first; that can't be avoided this way.
 *
 * Its config (res/xml/home_watcher.xml) limits it to window changes from Google TV's two home apps and gives it
 * no window contents, so it sees nothing else. Turning it off in Settings > Accessibility undoes it.
 */
class HomeWatcherService : AccessibilityService() {
    private val watch = HomeWatch()

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        // Google TV's home reports its home pane gone when another app comes over it.
        val leaving = event.contentChangeTypes and AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED != 0
        if (!watch.shouldOpenKimono(packageName, className, SystemClock.elapsedRealtime(), leaving)) {
            if (packageName in HomeWatch.STOCK_HOMES) Log.d(TAG, "Left $className alone (leaving=$leaving)")
            return
        }
        Log.d(TAG, "Opened Kimono for $className")
        // The same intent the Home button sends, so Kimono treats it as a Home press and goes to its first
        // workspace. An accessibility service may start an activity from the background.
        val home = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setPackage(this.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(home)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Kimono's home activity is missing", e)
        }
    }

    override fun onInterrupt() = Unit

    private companion object {
        const val TAG = "KimonoHomeWatcher"
    }
}

/**
 * When to open Kimono: when one of Google TV's home apps comes to the front, but not again within [quietMillis] of
 * the last time, so a launcher that fires several window changes in a row opens Kimono once. Not when the change is
 * that home `leaving` (going behind another app), and not during a [visit] the user asked for (#198); leaving ends
 * the visit.
 *
 * Google TV's dashboard (the side panel that a held Home opens, with profiles, settings and inputs) belongs to the
 * same home app, but it's a panel over whatever is on screen rather than the home screen, so it's left alone.
 */
class HomeWatch(
    private val quietMillis: Long = QUIET_MILLIS,
    private val visit: StockHomeVisit = StockHomeVisit.current,
) {
    private var lastOpenedAt: Long? = null

    fun shouldOpenKimono(packageName: String?, className: String?, now: Long, leaving: Boolean = false): Boolean {
        if (packageName !in STOCK_HOMES) return false
        if (leaving) visit.end()
        val last = lastOpenedAt
        val quiet = last != null && now - last < quietMillis
        val open = !leaving && !visit.isOpen && !isDashboard(className) && !quiet
        if (open) lastOpenedAt = now
        return open
    }

    companion object {
        /** Google TV's home app. */
        const val LAUNCHERX = "com.google.android.apps.tv.launcherx"

        /** Google TV's home and the older Android TV home. Keep in step with res/xml/home_watcher.xml. */
        val STOCK_HOMES = setOf(LAUNCHERX, "com.google.android.tvlauncher")

        const val QUIET_MILLIS = 1_000L

        /** launcherx's dashboard classes live in its `dashboard` package, e.g. `.dashboard.DashboardHandler`. */
        private fun isDashboard(className: String?): Boolean = className?.contains(".dashboard.") == true
    }
}
