package io.github.ranjandas.kimono.data.apps

/**
 * Categories for well-known TV apps that don't declare one in their manifest. Most streaming apps leave
 * `android:appCategory` unset (on the Streamer in 2026-11 only Jellyfin set it), so without this list they all
 * land under System. A key matches its package and any package under it (`org.smarttube` covers
 * `org.smarttube.stable`).
 */
internal object KnownCategories {
    private val byPackage: Map<String, AppCategory> = buildMap {
        listOf(
            // Global streaming
            "com.netflix",
            "com.disney.disneyplus",
            "com.amazon.amazonvideo.livingroom",
            "com.apple.atve",
            "com.google.android.youtube.tv",
            "com.google.android.youtube.tvunplugged",
            "com.google.android.youtube.tvkids",
            "com.google.android.videos",
            "org.smarttube",
            "com.teamsmart.videomanager.tv",
            "com.hulu",
            "tv.twitch.android.app",
            // Australian broadcasters and streamers
            "au.net.abc.iview",
            "au.com.kayosports",
            "au.com.stan",
            "au.com.ninenow",
            "au.com.tenplay",
            "com.sbs.ondemand",
            "com.swm.live",
            // Media servers and players
            "org.jellyfin",
            "com.plexapp",
            "org.xbmc.kodi",
            "org.videolan.vlc",
        ).forEach { put(it, AppCategory.Video) }
        listOf(
            "com.google.android.youtube.tvmusic",
            "com.spotify",
        ).forEach { put(it, AppCategory.Audio) }
        listOf(
            "com.valvesoftware.steamlink",
            "com.nvidia.geforcenow",
        ).forEach { put(it, AppCategory.Game) }
    }

    /** The category for [packageName], from the longest matching key, or null when it isn't listed. */
    fun of(packageName: String): AppCategory? {
        var key = packageName
        while (true) {
            byPackage[key]?.let { return it }
            val dot = key.lastIndexOf('.')
            if (dot < 0) return null
            key = key.substring(0, dot)
        }
    }
}
