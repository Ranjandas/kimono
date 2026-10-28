package io.github.ranjandas.kimono.core.design.icon

/**
 * The icon for the app with [packageName]: its brand icon when the font has one, otherwise [fallback], usually
 * the icon for its category.
 */
fun appIcon(packageName: String, fallback: KimonoIcon): KimonoIcon = BrandIcons[packageName] ?: fallback

/**
 * The app's brand icon when the font has a real one, for a tile to show as its logo, the same as in the lists
 * (#116). Apps named here only to give them a kind of icon have none.
 */
fun brandIcon(packageName: String): KimonoIcon? = BrandIcons[packageName]?.takeUnless { it in KindIcons }

private val KindIcons = setOf(KimonoIcon.Media)

// TV packages first, then the phone builds that some people sideload.
private val BrandIcons = mapOf(
    "com.google.android.youtube.tv" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvunplugged" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvkids" to KimonoIcon.YouTube,
    "com.google.android.youtube.tvmusic" to KimonoIcon.YouTube,
    "com.google.android.youtube" to KimonoIcon.YouTube,
    // SmartTube, a YouTube client popular on TV boxes.
    "org.smarttube.stable" to KimonoIcon.YouTube,
    "org.smarttube.beta" to KimonoIcon.YouTube,
    "com.teamsmart.videomanager.tv" to KimonoIcon.YouTube,
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
    // No brand glyph in the font, and the app doesn't declare a video category, so name it here.
    "com.amazon.amazonvideo.livingroom" to KimonoIcon.Media,
)
