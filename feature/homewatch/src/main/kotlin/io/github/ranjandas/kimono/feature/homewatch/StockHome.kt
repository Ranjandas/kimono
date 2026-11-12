package io.github.ranjandas.kimono.feature.homewatch

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Google TV's own home, visited from Kimono (#198). Profiles, the For you and Live tabs and Google's search live
 * there, and with the watcher on Home never reaches them. [open] starts a [StockHomeVisit] so the watcher stands
 * aside, then opens it.
 */
object StockHome {
    /** Whether Google TV's home is here and enabled. It isn't when it was turned off over adb (route A). */
    fun isAvailable(context: Context): Boolean = context.packageManager.resolveActivity(intent(), 0) != null

    /** Opens Google TV's home for one visit. False when it can't be opened, and then no visit starts. */
    fun open(context: Context): Boolean {
        StockHomeVisit.current.start()
        return try {
            context.startActivity(intent())
            true
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Google TV's home is missing", e)
            StockHomeVisit.current.end()
            false
        }
    }

    private fun intent() = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_HOME)
        .setPackage(HomeWatch.LAUNCHERX)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private const val TAG = "KimonoHomeWatcher"
}
