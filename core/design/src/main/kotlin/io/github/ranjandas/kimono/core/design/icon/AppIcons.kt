package io.github.ranjandas.kimono.core.design.icon

/**
 * The icon for the app with [packageName]: its brand icon when the font has one, otherwise [fallback], usually
 * the icon for its category.
 */
fun appIcon(packageName: String, fallback: KimonoIcon): KimonoIcon = BrandIcons[packageName] ?: fallback

// TV packages first, then the phone builds that some people sideload.
private val BrandIcons = mapOf(
    "com.google.android.youtube.tv" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvunplugged" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvkids" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvmusic" to KimonoIcon.YouTube,
    "com.google.android.youtube" to KimonoIcon.YouTube,
    "com.netflix.ninja" to KimonoIcon.Netflix,
    "com.netflix.mediaclient" to KimonoIcon.Netflix,
    "com.spotify.tv.android" to KimonoIcon.Spotify,
    "com.spotify.music" to KimonoIcon.Spotify,
    "com.plexapp.android" to KimonoIcon.Plex,
    "org.xbmc.kodi" to KimonoIcon.Kodi,
    "tv.twitch.android.app" to KimonoIcon.Twitch,
    "com.hulu.livingroomplus" to KimonoIcon.Hulu,
    "com.hulu.plus" to KimonoIcon.Hulu,
    "org.videolan.vlc" to KimonoIcon.Vlc,
    "com.valvesoftware.steamlink" to KimonoIcon.Steam,
    "com.android.vending" to KimonoIcon.GooglePlay,
    "com.android.chrome" to KimonoIcon.Chrome,
    "org.mozilla.firefox" to KimonoIcon.Firefox,
    "org.mozilla.tv.firefox" to KimonoIcon.Firefox,
    "com.android.tv.settings" to KimonoIcon.Settings,
)
