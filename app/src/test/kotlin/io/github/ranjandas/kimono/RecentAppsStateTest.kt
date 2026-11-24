package io.github.ranjandas.kimono

import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.feature.home.RecentApps
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAppsStateTest {
    private val tv = InstalledApp("com.example.tv", "com.example.tv.Main", "TV", AppCategory.Video, true)
    private val news = InstalledApp("com.example.news", "com.example.news.Main", "News", AppCategory.Video, true)
    private val sport = InstalledApp("com.example.sport", "com.example.sport.Main", "Sport", AppCategory.Video, true)
    private val launchable = listOf(tv, news, sport).associateBy { it.packageName }

    @Test
    fun `asks for access only where the TV has the screen`() {
        assertEquals(RecentApps.NeedsAccess, recentAppsOf(emptyList(), emptyList(), launchable, askForAccess = true))
        assertEquals(
            RecentApps.Apps(emptyList()),
            recentAppsOf(emptyList(), emptyList(), launchable, askForAccess = false),
        )
    }

    @Test
    fun `shows launchable apps whenever there are some`() {
        val packages = listOf("com.example.gone", tv.packageName)
        assertEquals(RecentApps.Apps(listOf(tv)), recentAppsOf(packages, emptyList(), launchable, askForAccess = true))
    }

    @Test
    fun `tops up a short system history with Kimono's launches, system first`() {
        // The system knows only the app used last, as on a TV stuck on a future usage bucket (#239).
        val system = listOf(news.packageName, "com.android.systemui")
        val launches = listOf(sport.packageName, news.packageName, tv.packageName)
        assertEquals(
            RecentApps.Apps(listOf(news, sport, tv)),
            recentAppsOf(system, launches, launchable, askForAccess = false),
        )
    }

    @Test
    fun `uses Kimono's launches when the system's apps can't be launched`() {
        val launches = listOf(tv.packageName)
        assertEquals(
            RecentApps.Apps(listOf(tv)),
            recentAppsOf(listOf("com.android.systemui"), launches, launchable, askForAccess = true),
        )
    }
}
