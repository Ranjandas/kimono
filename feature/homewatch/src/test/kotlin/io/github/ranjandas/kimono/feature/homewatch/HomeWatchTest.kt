package io.github.ranjandas.kimono.feature.homewatch

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeWatchTest {
    @Test
    fun `opens Kimono when Google TV's home comes to the front`() {
        val watch = HomeWatch()
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", now = 0))
    }

    @Test
    fun `also covers the older Android TV home`() {
        assertTrue(HomeWatch().shouldOpenKimono("com.google.android.tvlauncher", now = 0))
    }

    @Test
    fun `ignores every other app`() {
        val watch = HomeWatch()
        assertFalse(watch.shouldOpenKimono("com.google.android.youtube.tv", now = 0))
        assertFalse(watch.shouldOpenKimono(null, now = 0))
    }

    @Test
    fun `opens once for a burst of window changes`() {
        val watch = HomeWatch(quietMillis = 1_000)
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", now = 5_000))
        assertFalse(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", now = 5_400))
        assertTrue(watch.shouldOpenKimono("com.google.android.apps.tv.launcherx", now = 6_000))
    }
}
