package io.github.ranjandas.kimono

import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.feature.home.RecentApps
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAppsStateTest {
    private val tv = InstalledApp("com.example.tv", "com.example.tv.Main", "TV", AppCategory.Video, true)
    private val launchable = mapOf(tv.packageName to tv)

    @Test
    fun `asks for access only where the TV has the screen`() {
        assertEquals(RecentApps.NeedsAccess, recentAppsOf(emptyList(), launchable, askForAccess = true))
        assertEquals(RecentApps.Apps(emptyList()), recentAppsOf(emptyList(), launchable, askForAccess = false))
    }

    @Test
    fun `shows launchable apps whenever there are some`() {
        val packages = listOf("com.example.gone", tv.packageName)
        assertEquals(RecentApps.Apps(listOf(tv)), recentAppsOf(packages, launchable, askForAccess = true))
    }
}
