package io.github.ranjandas.kimono

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.ranjandas.kimono.core.data.settings.SettingsStore
import io.github.ranjandas.kimono.data.apps.icon.AppGlyphLoader
import io.github.ranjandas.kimono.data.apps.icon.AppGlyphs

/**
 * Debug builds only: every app on the device in each themed icon style, side by side, to choose one (#114).
 * Open it with `adb shell am start -n io.github.ranjandas.kimono/.IconLabActivity`.
 */
class IconLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsStore = SettingsStore.create(this)
        setContent { IconLab(settingsStore, loadApps = ::labApps) }
    }

    // Disabled apps too, such as a switched-off Netflix, since they're the ones worth comparing.
    private fun labApps(): List<LabApp> {
        val loader = AppGlyphLoader.create(this)
        return (query(Intent.CATEGORY_LEANBACK_LAUNCHER) + query(Intent.CATEGORY_LAUNCHER))
            .map { it.activityInfo }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .map { info ->
                LabApp(
                    label = info.loadLabel(packageManager).toString(),
                    enabled = info.applicationInfo.enabled,
                    loadGlyphs = { loader.load(info) },
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun query(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        val flags = PackageManager.MATCH_DISABLED_COMPONENTS.toLong()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, flags.toInt())
        }
    }
}

/** An app in the lab. Its glyphs take a while, so each row loads its own as it scrolls into view. */
internal class LabApp(val label: String, val enabled: Boolean, val loadGlyphs: () -> AppGlyphs)
