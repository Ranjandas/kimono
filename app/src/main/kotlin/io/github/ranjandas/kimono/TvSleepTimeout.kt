package io.github.ranjandas.kimono

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.ranjandas.kimono.core.data.settings.LauncherSettings
import io.github.ranjandas.kimono.core.data.settings.Screensaver
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

/**
 * How long home may hold the screen on once its pixel art starts (#236): none when Google TV's screensaver should
 * take over, otherwise until the TV's sleep timer would have run out, counted from the last key press, which was
 * the idle timeout before the art started.
 */
@Composable
internal fun keepAwakeFor(settings: LauncherSettings): Duration {
    val context = LocalContext.current
    val sleepAfter = remember(context) { tvSleepTimeout(context) }
    // With dimming off, home never goes idle, so there's nothing to hold.
    if (settings.screensaver != Screensaver.Kimono || settings.idleTimeout.duration.isInfinite()) return Duration.ZERO
    return sleepAfter - settings.idleTimeout.duration
}

/**
 * How long the TV waits without a key press before it goes to sleep: Google TV's energy setting, stored as
 * `sleep_timeout`. A window that keeps the screen on also holds off that timer, so Kimono has to keep to it
 * itself. Infinite when it's set to never; an hour if the TV won't say.
 */
internal fun tvSleepTimeout(context: Context): Duration {
    val millis = try {
        Settings.Secure.getLong(context.contentResolver, SLEEP_TIMEOUT, NEVER)
    } catch (e: SecurityException) {
        Log.w(TAG, "Can't read $SLEEP_TIMEOUT", e)
        return FALLBACK
    }
    return if (millis <= 0) Duration.INFINITE else millis.milliseconds
}

private const val TAG = "Kimono"
private const val SLEEP_TIMEOUT = "sleep_timeout"
private const val NEVER = -1L
private val FALLBACK = 1.hours
