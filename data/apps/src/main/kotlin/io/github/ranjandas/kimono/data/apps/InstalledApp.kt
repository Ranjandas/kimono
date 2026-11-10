package io.github.ranjandas.kimono.data.apps

import android.content.pm.ApplicationInfo

/** A launchable activity, one per app. */
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val category: AppCategory,
    /** False for phone apps that only have a `LAUNCHER` activity, which may not work well with a remote. */
    val isTvApp: Boolean,
)

/**
 * Drawer groups, from the category an app declares in its manifest (`ApplicationInfo.category`), or from
 * [KnownCategories] when it declares none that Kimono groups by.
 */
enum class AppCategory {
    Video,
    Audio,
    Game,
    Image,
    News,
    Other,
    ;

    companion object {
        /** The group for an app that declares [category], falling back to what Kimono knows of [packageName]. */
        fun of(category: Int, packageName: String): AppCategory =
            fromApplicationInfo(category).takeUnless { it == Other } ?: KnownCategories.of(packageName) ?: Other

        fun fromApplicationInfo(category: Int): AppCategory = when (category) {
            ApplicationInfo.CATEGORY_VIDEO -> Video
            ApplicationInfo.CATEGORY_AUDIO -> Audio
            ApplicationInfo.CATEGORY_GAME -> Game
            ApplicationInfo.CATEGORY_IMAGE -> Image
            ApplicationInfo.CATEGORY_NEWS -> News
            else -> Other
        }
    }
}
