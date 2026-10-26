package io.github.ranjandas.kimono.data.apps

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** What happened when Kimono tried to open an app. */
sealed interface LaunchResult {
    data object Launched : LaunchResult

    /** The activity is gone or disabled, typically because the app was just uninstalled or updated. */
    data class Unavailable(val app: InstalledApp) : LaunchResult
}

/** Opens [InstalledApp]s. An interface so screens can be tested without starting activities. */
fun interface AppLauncher {
    fun launch(app: InstalledApp): LaunchResult

    companion object {
        fun create(context: Context): AppLauncher = ActivityAppLauncher(context)
    }
}

internal class ActivityAppLauncher(context: Context) : AppLauncher {
    private val appContext = context.applicationContext

    override fun launch(app: InstalledApp): LaunchResult = try {
        appContext.startActivity(launchIntent(app))
        LaunchResult.Launched
    } catch (_: ActivityNotFoundException) {
        LaunchResult.Unavailable(app)
    } catch (_: SecurityException) {
        // The activity stopped being exported, or now needs a permission Kimono doesn't hold.
        LaunchResult.Unavailable(app)
    }
}

/** The intent the system launcher would use: a new task, or the app's existing one brought to the front. */
internal fun launchIntent(app: InstalledApp): Intent = Intent(Intent.ACTION_MAIN)
    .addCategory(if (app.isTvApp) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
    .setComponent(ComponentName(app.packageName, app.activityName))
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
