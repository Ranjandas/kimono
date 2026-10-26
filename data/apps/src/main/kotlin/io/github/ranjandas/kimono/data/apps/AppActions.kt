package io.github.ranjandas.kimono.data.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** The system screens Kimono sends the user to for an app it can't change itself. */
fun interface AppActions {
    /**
     * Opens a system screen for [app]. Returns false when the device has no such screen, which happens on
     * TV builds that hide parts of Settings.
     */
    fun open(screen: AppScreen, app: InstalledApp): Boolean

    companion object {
        fun create(context: Context): AppActions = SystemAppActions(context)
    }
}

/** A system screen about one app. */
enum class AppScreen {
    /** The app's entry in Settings: storage, permissions, notifications, force stop. */
    Info,

    /** The system's uninstall confirmation. The user confirms it, never Kimono. */
    Uninstall,
}

internal class SystemAppActions(context: Context) : AppActions {
    private val appContext = context.applicationContext

    override fun open(screen: AppScreen, app: InstalledApp): Boolean = try {
        appContext.startActivity(screenIntent(screen, app.packageName))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

internal fun screenIntent(screen: AppScreen, packageName: String): Intent {
    val uri = Uri.fromParts("package", packageName, null)
    val action = when (screen) {
        AppScreen.Info -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        AppScreen.Uninstall -> Intent.ACTION_DELETE
    }
    return Intent(action, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
