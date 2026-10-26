package io.github.ranjandas.kimono.core.design.icon

/**
 * Icons are glyphs in the bundled Nerd Font (PLAN D8), named by what they mean so screens never use
 * raw code points. All come from the Material Design Icons set, so they share one style.
 *
 * The font subsetting task keeps only the code points written here as `0x…` literals, so an icon is
 * added by adding an entry. The full set and its use in screens arrive with M1-16.
 */
enum class KimonoIcon(val codePoint: Int) {
    Home(0xF02DC),
    Apps(0xF003B),
    Search(0xF0349),
    Settings(0xF0493),
    Wifi(0xF05A9),
    WifiOff(0xF05AA),
    Ethernet(0xF0200),
    Volume(0xF057E),
    VolumeOff(0xF0581),
    Clock(0xF0150),
    Play(0xF040A),
    Pause(0xF03E4),
    Pin(0xF0403),
    Palette(0xF03D8),
    Info(0xF02FD),
    ChevronLeft(0xF0141),
    ChevronRight(0xF0142),
    WorkspaceMedia(0xF0502),
    WorkspaceGames(0xF0297),
    WorkspaceMusic(0xF075A),
    AppFallback(0xF08C6),
    ;

    /** The glyph as a string, for use in `Text`. */
    val text: String get() = String(Character.toChars(codePoint))
}
