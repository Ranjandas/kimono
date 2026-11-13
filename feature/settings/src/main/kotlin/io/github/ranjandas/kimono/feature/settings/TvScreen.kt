package io.github.ranjandas.kimono.feature.settings

import io.github.ranjandas.kimono.core.design.icon.KimonoIcon

/**
 * A screen of the TV's own settings that Kimono's settings open (#200). Covering Google TV's home also hides its
 * quick settings, so these are the way back to them. Google TV keeps display and sound on one screen.
 */
enum class TvScreen(val label: String, val icon: KimonoIcon) {
    Network("Network & internet", KimonoIcon.Wifi),
    DisplaySound("Display & sound", KimonoIcon.Volume),
    All("All TV settings", KimonoIcon.Settings),
}
