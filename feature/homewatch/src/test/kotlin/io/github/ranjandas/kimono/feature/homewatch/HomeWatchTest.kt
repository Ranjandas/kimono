package io.github.ranjandas.kimono.feature.homewatch

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeWatchTest {
    @Test
    fun `opens Kimono when Google TV's home comes to the front`() {
        val watch = HomeWatch()
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", HOME_CLASS, now = 0))
    }

    @Test
    fun `also covers the older Android TV home`() {
        assertTrue(HomeWatch().shouldOpenKimono("com.google.android.tvlauncher", HOME_CLASS, now = 0))
    }

    @Test
    fun `ignores every other app`() {
        val watch = HomeWatch()
        assertFalse(watch.shouldOpenKimono("com.google.android.youtube.tv", HOME_CLASS, now = 0))
        assertFalse(watch.shouldOpenKimono(null, HOME_CLASS, now = 0))
    }

    @Test
    fun `leaves Google TV's dashboard alone, the panel a held Home opens`() {
        val watch = HomeWatch()
        assertFalse(watch.shouldOpenKimono(LAUNCHERX, DASHBOARD_CLASS, now = 0))
        // Skipping the dashboard doesn't start the quiet time, so the next real Home still opens Kimono.
        assertTrue(watch.shouldOpenKimono(LAUNCHERX, HOME_CLASS, now = 100))
    }

    @Test
    fun `opens when the window change has no class name`() {
        assertTrue(HomeWatch().shouldOpenKimono(LAUNCHERX, null, now = 0))
    }

    @Test
    fun `opens once for a burst of window changes`() {
        val watch = HomeWatch(quietMillis = 1_000)
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", HOME_CLASS, now = 5_000))
        assertFalse(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", HOME_CLASS, now = 5_400))
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", HOME_CLASS, now = 6_000))
    }

    private companion object {
        const val LAUNCHERX = "com.google.android.apps.tv.launcherx"
        const val DASHBOARD_CLASS = "com.google.android.apps.tv.launcherx.dashboard.DashboardHandler"
        const val HOME_CLASS = "com.google.android.apps.tv.launcherx.home.HomeActivity"
    }
}
