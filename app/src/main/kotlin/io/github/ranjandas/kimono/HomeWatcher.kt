package io.github.ranjandas.kimono

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * The home watcher from :feature:homewatch, seen from the app, which can't name its class: the Play bundle doesn't
 * have it. [isInBuild] is false there, and Settings leaves its row out.
 */
internal class HomeWatcher(private val context: Context) {
    private val component = ComponentName(context.packageName, SERVICE_CLASS)

    val isInBuild: Boolean = try {
        context.packageManager.getServiceInfo(component, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    /** Whether it's switched on in Settings > Accessibility. */
    fun isOn(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return isListed(enabled, context.packageName, SERVICE_CLASS)
    }

    /**
     * Opens the accessibility screen, where it's switched on and off. False when this device hides it. It opens in a
     * fresh task: otherwise TV Settings resumes whatever it last showed under it, and Back walks through that
     * instead of returning to Kimono.
     */
    fun openSettings(): Boolean = try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    companion object {
        const val SERVICE_CLASS = "io.github.ranjandas.kimono.feature.homewatch.HomeWatcherService"

        /**
         * Whether the service is in Settings' colon-separated list of enabled services, written either in full
         * (`package/package.Class`) or short (`package/.Class`).
         */
        fun isListed(enabled: String, packageName: String, className: String): Boolean {
            val full = "$packageName/$className"
            val short = "$packageName/" + className.removePrefix(packageName)
            return enabled.split(':').any { it == full || it == short }
        }
    }
}
