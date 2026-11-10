// Colours are the scene's own, picked per time of day to sit under the Kimono dusk theme.
@file:Suppress("MagicNumber")

package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * One time of day for the Yūgure landscape: its sky from top to horizon, the sun (or moon) and its glow, the hill
 * layers from far to near, mist, sea and wave lines, cloud bands and cranes. All RGB.
 */
@Suppress("LongParameterList") // One colour per part of the scene.
internal class YugureSky(
    val sky: IntArray,
    val sun: Int,
    val glow: Int,
    val hills: IntArray,
    val mist: Int,
    val water: Int,
    val wave: Int,
    val cloud: Int,
    val cranes: Int,
    val night: Boolean = false,
    val sunPath: Double = 0.22,
)

/** The four skies, and the local hour each one starts at. */
internal enum class YugureTime(val startHour: Int, val sky: YugureSky) {
    Dawn(
        5,
        YugureSky(
            sky = intArrayOf(0x283462, 0x5B6598, 0xB893A8, 0xEFC0A6, 0xF8DCC0),
            sun = 0xFFF2DF,
            glow = 0xFFE1C8,
            hills = intArrayOf(0xA796B8, 0x7A74A3, 0x545A8A, 0x343D6C, 0x232C55),
            mist = 0xFFE8DE,
            water = 0x26305C,
            wave = 0x4C5893,
            cloud = 0xFFF0E4,
            cranes = 0x2A2F52,
        ),
    ),
    Day(
        10,
        YugureSky(
            sky = intArrayOf(0x2F4F85, 0x5C82B5, 0x93B3D4, 0xC9D9E4, 0xE7E5DA),
            sun = 0xFFFAF0,
            glow = 0xFFFAEB,
            hills = intArrayOf(0xA8B6CB, 0x7D93B4, 0x5A7099, 0x3B5079, 0x273A60),
            mist = 0xF5F8FA,
            water = 0x284069,
            wave = 0x4F6C9B,
            cloud = 0xFFFFFF,
            cranes = 0x24334F,
        ),
    ),
    Dusk(
        16,
        YugureSky(
            sky = intArrayOf(0x0F1530, 0x27315F, 0x6F5F8C, 0xC98C86, 0xEAB48C),
            sun = 0xF8DFC2,
            glow = 0xFAC8A0,
            hills = intArrayOf(0x8A7598, 0x5A557F, 0x363C66, 0x1E2648, 0x131A36),
            mist = 0xF0CEC4,
            water = 0x121A35,
            wave = 0x2F3D72,
            cloud = 0xF6E2D2,
            cranes = 0xF1E8D6,
        ),
    ),
    Night(
        21,
        YugureSky(
            sky = intArrayOf(0x05070F, 0x0B1024, 0x151C3A, 0x1F2A4D, 0x2A365C),
            sun = 0xE8E3D3,
            glow = 0xDCE1F0,
            hills = intArrayOf(0x2C3458, 0x222A4A, 0x181F3B, 0x10162D, 0x0A0F22),
            mist = 0xAAB6E1,
            water = 0x090E20,
            wave = 0x1A2448,
            cloud = 0xC8D2F0,
            cranes = 0xC9C6BD,
            night = true,
            sunPath = 0.18,
        ),
    ),
    ;

    companion object {
        /** The time of day that local [hour] (0 to 23) falls in. */
        fun at(hour: Int): YugureTime = entries.lastOrNull { hour >= it.startHour } ?: Night
    }
}
