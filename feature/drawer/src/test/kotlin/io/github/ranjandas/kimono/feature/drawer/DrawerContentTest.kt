package io.github.ranjandas.kimono.feature.drawer

import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DrawerContentTest {
    private fun app(label: String, category: AppCategory) =
        InstalledApp("pkg.${label.lowercase()}", "pkg.main", label, category, isTvApp = true)

    private val plex = app("Plex", AppCategory.Video)
    private val appleTv = app("Apple TV", AppCategory.Video)
    private val spotify = app("Spotify", AppCategory.Audio)
    private val retro = app("Retro", AppCategory.Game)
    private val files = app("Files", AppCategory.Other)
    private val apps = listOf(appleTv, files, plex, retro, spotify)

    @Test
    fun `groups apps as watch, listen, play and system, in that order`() {
        assertEquals(
            listOf(
                DrawerSection(DrawerGroup.Watch, listOf(appleTv, plex)),
                DrawerSection(DrawerGroup.Listen, listOf(spotify)),
                DrawerSection(DrawerGroup.Play, listOf(retro)),
                DrawerSection(DrawerGroup.System, listOf(files)),
            ),
            drawerSections(apps),
        )
    }

    @Test
    fun `leaves out a group with nothing in it`() {
        assertEquals(listOf(DrawerGroup.Watch), drawerSections(listOf(plex, appleTv)).map { it.group })
    }

    @Test
    fun `filters as you type, ignoring case`() {
        assertEquals(listOf(appleTv, plex), drawerSections(apps, "ple").flatMap { it.apps })
        assertEquals(listOf(plex), drawerSections(apps, "PLEX").flatMap { it.apps })
        assertEquals(emptyList<DrawerSection>(), drawerSections(apps, "zzz"))
    }

    @Test
    fun `ignores spaces, so appletv finds Apple TV`() {
        assertTrue(appleTv.matches("appletv"))
        assertTrue(appleTv.matches("apple tv"))
        assertFalse(appleTv.matches("appletvplus"))
    }

    @Test
    fun `an empty query matches everything`() {
        assertEquals(apps.size, drawerSections(apps, "").sumOf { it.apps.size })
        assertEquals(apps.size, drawerSections(apps, " ").sumOf { it.apps.size })
    }

    @Test
    fun `apps without a brand icon take their group's, and phone apps say so`() {
        assertEquals(KimonoIcon.Media, app("Jellyfin", AppCategory.Video).icon(DrawerGroup.Watch))
        assertEquals(KimonoIcon.AppFallback, app("Files", AppCategory.Other).icon(DrawerGroup.System))
        val phoneApp = app("Notes", AppCategory.Other).copy(isTvApp = false)
        assertEquals(KimonoIcon.PhoneApp, phoneApp.icon(DrawerGroup.System))
    }

    @Test
    fun `a brand icon wins over the group's`() {
        val youTube = app("YouTube", AppCategory.Video).copy(packageName = "com.google.android.youtube.tv")
        assertEquals(KimonoIcon.YouTube, youTube.icon(DrawerGroup.Watch))
    }
}
