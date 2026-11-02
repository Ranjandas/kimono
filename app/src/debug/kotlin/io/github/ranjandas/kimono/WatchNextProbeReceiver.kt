package io.github.ranjandas.kimono

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Debug builds only: the same dump as [WatchNextProbeActivity] without waking the screen (#143). Grant the
 * permission with adb, then `adb shell am broadcast -n io.github.ranjandas.kimono/.WatchNextProbeReceiver`.
 */
class WatchNextProbeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WatchNextProbe.dump(context.contentResolver)
    }
}
