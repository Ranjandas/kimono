package io.github.ranjandas.kimono.data.apps

import android.content.pm.ApplicationInfo
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppsRepositoryTest {
    private class FakeAppSource(var apps: List<InstalledApp>) : AppSource {
        val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        override fun launchableApps() = apps

        override fun changes(): Flow<Unit> = changes
    }

    private fun app(label: String, packageName: String = "pkg.${label.lowercase()}") =
        InstalledApp(packageName, "$packageName.Main", label, AppCategory.Other, isTvApp = true)

    @Test
    fun `emits the installed apps sorted by label, ignoring case and accents`() = runTest {
        val source = FakeAppSource(listOf(app("youtube"), app("Plex"), app("Émulateur"), app("apple tv")))
        AppsRepository(source, StandardTestDispatcher(testScheduler)).apps.test {
            assertEquals(listOf("apple tv", "Émulateur", "Plex", "youtube"), awaitItem().map { it.label })
        }
    }

    @Test
    fun `emits again when a package changes`() = runTest {
        val source = FakeAppSource(listOf(app("Plex")))
        AppsRepository(source, StandardTestDispatcher(testScheduler)).apps.test {
            assertEquals(listOf("Plex"), awaitItem().map { it.label })
            source.apps = listOf(app("Plex"), app("Kodi"))
            source.changes.emit(Unit)
            assertEquals(listOf("Kodi", "Plex"), awaitItem().map { it.label })
        }
    }

    @Test
    fun `skips changes that leave the list as it was`() = runTest {
        val source = FakeAppSource(listOf(app("Plex")))
        AppsRepository(source, StandardTestDispatcher(testScheduler)).apps.test {
            awaitItem()
            source.changes.emit(Unit)
            source.apps = listOf(app("Plex"), app("Kodi"))
            source.changes.emit(Unit)
            assertEquals(listOf("Kodi", "Plex"), awaitItem().map { it.label })
        }
    }

    @Test
    fun `apps with the same label keep a stable order`() = runTest {
        val source = FakeAppSource(listOf(app("Files", "pkg.b"), app("Files", "pkg.a")))
        AppsRepository(source, StandardTestDispatcher(testScheduler)).apps.test {
            assertEquals(listOf("pkg.a", "pkg.b"), awaitItem().map { it.packageName })
        }
    }

    @Test
    fun `maps manifest categories to drawer groups`() {
        assertEquals(AppCategory.Video, AppCategory.fromApplicationInfo(ApplicationInfo.CATEGORY_VIDEO))
        assertEquals(AppCategory.Audio, AppCategory.fromApplicationInfo(ApplicationInfo.CATEGORY_AUDIO))
        assertEquals(AppCategory.Game, AppCategory.fromApplicationInfo(ApplicationInfo.CATEGORY_GAME))
        assertEquals(AppCategory.Other, AppCategory.fromApplicationInfo(ApplicationInfo.CATEGORY_UNDEFINED))
        assertEquals(AppCategory.Other, AppCategory.fromApplicationInfo(ApplicationInfo.CATEGORY_PRODUCTIVITY))
    }
}
