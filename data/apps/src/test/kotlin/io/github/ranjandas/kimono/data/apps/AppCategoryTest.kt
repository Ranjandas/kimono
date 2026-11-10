package io.github.ranjandas.kimono.data.apps

import android.content.pm.ApplicationInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppCategoryTest {
    @Test
    fun `a declared category wins over the known list`() {
        assertEquals(AppCategory.Game, AppCategory.of(ApplicationInfo.CATEGORY_GAME, "com.netflix.ninja"))
    }

    @Test
    fun `streaming apps that declare nothing are filed as video`() {
        val undeclared = ApplicationInfo.CATEGORY_UNDEFINED
        listOf(
            "org.smarttube.stable",
            "au.net.abc.iview",
            "au.com.kayosports.tv",
            "com.amazon.amazonvideo.livingroom",
            "au.com.stan.and",
            "au.com.ninenow.ctv",
            "com.netflix.ninja",
            "com.google.android.youtube.tv",
        ).forEach { assertEquals(AppCategory.Video, AppCategory.of(undeclared, it), it) }
    }

    @Test
    fun `the longest matching key wins, so YouTube Music is audio`() {
        val undeclared = ApplicationInfo.CATEGORY_UNDEFINED
        assertEquals(AppCategory.Audio, AppCategory.of(undeclared, "com.google.android.youtube.tvmusic"))
        assertEquals(AppCategory.Video, AppCategory.of(undeclared, "com.google.android.youtube.tvkids"))
    }

    @Test
    fun `a key only matches whole package segments`() {
        assertEquals(null, KnownCategories.of("com.netflixish.app"))
        assertEquals(null, KnownCategories.of("com.google.android.youtube"))
        assertEquals(AppCategory.Other, AppCategory.of(ApplicationInfo.CATEGORY_PRODUCTIVITY, "com.example.notes"))
    }
}
