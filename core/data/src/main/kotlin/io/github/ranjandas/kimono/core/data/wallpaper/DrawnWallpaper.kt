package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * The wallpapers Kimono draws itself instead of downloading, for its own OLED theme. Each is thin lines in the
 * Kimono theme's dyes on true black, so every other pixel stays switched off. They're named by [url], a
 * `kimono:` link, so they go wherever a wallpaper link does.
 */
enum class DrawnWallpaper(private val draw: (width: Int, height: Int) -> IntArray) {
    /** A thin gold moon over vermilion waves. */
    Moon(::moonWallpaper),

    /** Seigaiha, the wave-scale pattern, rising from the bottom right. */
    Seigaiha(::seigaihaWallpaper),

    /** Asanoha, the hemp-leaf star pattern, glowing from the bottom left. */
    Asanoha(::asanohaWallpaper),
    ;

    val url: String get() = SCHEME + name.lowercase()

    /** The wallpaper as packed ARGB in rows of [width]. */
    fun pixels(width: Int, height: Int): IntArray = draw(width, height)

    companion object {
        private const val SCHEME = "kimono:"

        /** The drawn wallpaper [url] names, or null for any other link. */
        fun fromUrl(url: String): DrawnWallpaper? = entries.find { it.url == url }

        /** One a day, in turn, so no pattern sits on the panel day after day. */
        fun forDay(epochDay: Long): DrawnWallpaper = entries[Math.floorMod(epochDay, entries.size)]
    }
}

// The Kimono theme's dyes: shu vermilion and kin gold.
internal const val SHU = 0xF2543D
internal const val KIN = 0xE6B84A
