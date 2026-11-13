package io.github.ranjandas.kimono

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.github.ranjandas.kimono.feature.settings.TvScreen

/**
 * Opens the TV's own settings screens (#200). Each screen lists its actions in the order to try them: Google TV has
 * no display screen of its own, so display and sound goes to its sound screen, which shows both, and other TVs that
 * hide that fall back to a display screen.
 */
internal object TvSettings {
    fun actions(screen: TvScreen): List<String> = when (screen) {
        TvScreen.Network -> listOf(Settings.ACTION_WIFI_SETTINGS)
        TvScreen.DisplaySound -> listOf(Settings.ACTION_SOUND_SETTINGS, Settings.ACTION_DISPLAY_SETTINGS)
        TvScreen.All -> listOf(Settings.ACTION_SETTINGS)
    }

    /**
     * Opens [screen], or returns false when this TV hides it. Like the accessibility screen, it opens in a fresh task,
     * so TV Settings doesn't resume whatever it last showed and Back comes straight home.
     */
    fun open(context: Context, screen: TvScreen): Boolean = actions(screen).any { action ->
        try {
            context.startActivity(
                Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            )
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
