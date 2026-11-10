package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * The wallpapers Kimono draws itself instead of downloading, for its own two themes. Kimono Sumi's ([oled]) are
 * thin lines in its dyes on true black, so every other pixel stays switched off. Kimono's is the Yūgure landscape,
 * one for each time of day. They're named by [url], a `kimono:` link, so they go wherever a wallpaper link does.
 */
enum class DrawnWallpaper(private val draw: (width: Int, height: Int) -> IntArray, val oled: Boolean = true) {
    /** A thin gold moon over vermilion waves. */
    Moon(::moonWallpaper),

    /** Seigaiha, the wave-scale pattern, rising from the bottom right. */
    Seigaiha(::seigaihaWallpaper),

    /** Asanoha, the hemp-leaf star pattern, glowing from the bottom left. */
    Asanoha(::asanohaWallpaper),

    /** Yūgure at dawn: a pale rose sky over lavender hills. */
    YugureDawn({ w, h -> yugureWallpaper(w, h, YugureTime.Dawn) }, oled = false),

    /** Yūgure by day: a clear blue sky and a white sun. */
    YugureDay({ w, h -> yugureWallpaper(w, h, YugureTime.Day) }, oled = false),

    /** Yūgure at dusk: indigo into apricot, the sun low behind the hills. */
    YugureDusk({ w, h -> yugureWallpaper(w, h, YugureTime.Dusk) }, oled = false),

    /** Yūgure at night: a moon and stars over the darkest hills. */
    YugureNight({ w, h -> yugureWallpaper(w, h, YugureTime.Night) }, oled = false),
    ;

    val url: String get() = SCHEME + name.lowercase()

    /** The wallpaper as packed ARGB in rows of [width]. */
    fun pixels(width: Int, height: Int): IntArray = draw(width, height)

    companion object {
        private const val SCHEME = "kimono:"

        /** The drawn wallpaper [url] names, or null for any other link. */
        fun fromUrl(url: String): DrawnWallpaper? = entries.find { it.url == url }

        private val Oled = entries.filter { it.oled }

        /** One of Kimono Sumi's a day, in turn, so no pattern sits on the panel day after day. */
        fun forDay(epochDay: Long): DrawnWallpaper = Oled[Math.floorMod(epochDay, Oled.size)]

        /** The Yūgure sky for local [hour] (0 to 23): dawn from 5, day from 10, dusk from 16 and night from 21. */
        fun yugureAt(hour: Int): DrawnWallpaper = when (YugureTime.at(hour)) {
            YugureTime.Dawn -> YugureDawn
            YugureTime.Day -> YugureDay
            YugureTime.Dusk -> YugureDusk
            YugureTime.Night -> YugureNight
        }
    }
}

// Kimono Sumi's dyes: shu vermilion and kin gold.
internal const val SHU = 0xF2543D
internal const val KIN = 0xE6B84A
