package io.github.ranjandas.kimono.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Lists apps with a `LEANBACK_LAUNCHER` activity, plus phone apps that only have a `LAUNCHER` one, and listens
 * for package changes through [LauncherApps]. Visibility comes from the `<queries>` in this module's manifest.
 */
internal class PackageManagerAppSource(context: Context) : AppSource {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)

    override fun launchableApps(): List<InstalledApp> {
        val tvApps = query(Intent.CATEGORY_LEANBACK_LAUNCHER).map { it.toInstalledApp(isTvApp = true) }
        val tvPackages = tvApps.mapTo(HashSet()) { it.packageName }
        val phoneApps = query(Intent.CATEGORY_LAUNCHER)
            .filter { it.activityInfo.packageName !in tvPackages }
            .map { it.toInstalledApp(isTvApp = false) }
        return (tvApps + phoneApps)
            .filter { it.packageName != appContext.packageName }
            .distinctBy { it.packageName }
    }

    override fun changes(): Flow<Unit> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(Unit)
            }
        }
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    private fun query(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
    }

    private fun ResolveInfo.toInstalledApp(isTvApp: Boolean) = InstalledApp(
        packageName = activityInfo.packageName,
        activityName = activityInfo.name,
        label = loadLabel(packageManager).toString(),
        category = AppCategory.fromApplicationInfo(activityInfo.applicationInfo.category),
        isTvApp = isTvApp,
    )
}
