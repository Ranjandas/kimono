package io.github.ranjandas.kimono.feature.homewatch

import android.accessibilityservice.AccessibilityService
import java.lang.ref.WeakReference

/**
 * The TV's own power menu, with Restart in it (#230). An ordinary app can't restart the TV (only system apps may
 * hold `REBOOT`), but an accessibility service may ask the system for its power menu, so the top bar's power button
 * goes through the home watcher while it's on. The watcher runs in Kimono's process, so it hands itself over here
 * when the system binds it.
 */
object PowerMenu {
    @Volatile
    private var service: WeakReference<AccessibilityService>? = null

    /** Whether the watcher is running, so [open] can work. */
    val isReady: Boolean get() = service?.get() != null

    /** Opens the power menu. False when the watcher isn't running or the system wouldn't show it. */
    fun open(): Boolean = service?.get()?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) ?: false

    internal fun attach(watcher: AccessibilityService) {
        service = WeakReference(watcher)
    }

    internal fun detach(watcher: AccessibilityService) {
        if (service?.get() === watcher) service = null
    }
}
