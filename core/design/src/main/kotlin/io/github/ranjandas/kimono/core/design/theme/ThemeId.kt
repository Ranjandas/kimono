package io.github.ranjandas.kimono.core.design.theme

private fun rgb(hex: String) = Rgb.of(hex)

/**
 * The palettes Kimono ships: Kimono's own two, then the themes ported from Omarchy (MIT); see NOTICE for the
 * upstream themes.
 */
enum class ThemeId(val displayName: String, val palette: Palette) {
    /**
     * Kimono's own theme: an indigo dusk. The background is ai indigo and text is washi paper, so frosted panels read
     * as glass over the wallpaper. The colours are traditional Japanese dyes. Kaki persimmon is kept for focus
     * alone, and kin gold marks section labels.
     */
    Kimono(
        "Kimono",
        Palette(
            isLight = false,
            background = rgb("#151b2e"),
            surface = rgb("#1d2440"),
            surfaceVariant = rgb("#2a3254"),
            foreground = rgb("#f1e8d6"),
            mutedForeground = rgb("#8f93ad"),
            line = rgb("#3a4366"),
            accent = rgb("#f28c5b"),
            accent2 = rgb("#e8c27a"),
            red = rgb("#f07274"),
            yellow = rgb("#e8c27a"),
            green = rgb("#a3c48c"),
            cyan = rgb("#7fc4cf"),
            blue = rgb("#8ea7ea"),
            magenta = rgb("#e9a7bd"),
        ),
    ),

    /**
     * Kimono Sumi, named for sumi ink: Kimono's true-black theme, made for OLED panels. The screen is black, so its
     * pixels are off, and tiles are only just lit. Text is a warm off-white rather than white, so labels that stay
     * put wear the panel less. Shu vermilion marks focus and kin gold headers. Its wallpapers are drawn by Kimono in
     * the same dyes, thin lines on black, a different one each day.
     */
    KimonoSumi(
        "Kimono Sumi",
        Palette(
            isLight = false,
            background = rgb("#000000"),
            surface = rgb("#0b0a0d"),
            surfaceVariant = rgb("#1d1a21"),
            foreground = rgb("#dcd6ca"),
            mutedForeground = rgb("#6b6573"),
            line = rgb("#2e2a34"),
            accent = rgb("#f2543d"),
            accent2 = rgb("#e6b84a"),
            red = rgb("#ef4657"),
            yellow = rgb("#e6b84a"),
            green = rgb("#93b866"),
            cyan = rgb("#4fb3bf"),
            blue = rgb("#6f95e0"),
            magenta = rgb("#c77dba"),
        ),
    ),
    TokyoNight(
        "Tokyo Night",
        Palette(
            isLight = false,
            background = rgb("#1a1b26"),
            surface = rgb("#24283b"),
            surfaceVariant = rgb("#292e42"),
            foreground = rgb("#a9b1d6"),
            mutedForeground = rgb("#565f89"),
            line = rgb("#414868"),
            accent = rgb("#7aa2f7"),
            accent2 = rgb("#ad8ee6"),
            red = rgb("#f7768e"),
            yellow = rgb("#e0af68"),
            green = rgb("#9ece6a"),
            cyan = rgb("#449dab"),
            blue = rgb("#7aa2f7"),
            magenta = rgb("#ad8ee6"),
        ),
    ),
    Catppuccin(
        "Catppuccin",
        Palette(
            isLight = false,
            background = rgb("#1e1e2e"),
            surface = rgb("#313244"),
            surfaceVariant = rgb("#45475a"),
            foreground = rgb("#cdd6f4"),
            mutedForeground = rgb("#6c7086"),
            line = rgb("#585b70"),
            accent = rgb("#89b4fa"),
            accent2 = rgb("#f5c2e7"),
            red = rgb("#f38ba8"),
            yellow = rgb("#f9e2af"),
            green = rgb("#a6e3a1"),
            cyan = rgb("#94e2d5"),
            blue = rgb("#89b4fa"),
            magenta = rgb("#f5c2e7"),
        ),
    ),
    Gruvbox(
        "Gruvbox",
        Palette(
            isLight = false,
            background = rgb("#282828"),
            surface = rgb("#3c3836"),
            surfaceVariant = rgb("#504945"),
            foreground = rgb("#d4be98"),
            mutedForeground = rgb("#7c6f64"),
            line = rgb("#665c54"),
            accent = rgb("#7daea3"),
            accent2 = rgb("#d3869b"),
            red = rgb("#ea6962"),
            yellow = rgb("#d8a657"),
            green = rgb("#a9b665"),
            cyan = rgb("#89b482"),
            blue = rgb("#7daea3"),
            magenta = rgb("#d3869b"),
        ),
    ),
    RosePine(
        "Rosé Pine",
        Palette(
            isLight = true,
            background = rgb("#faf4ed"),
            surface = rgb("#f2e9e1"),
            surfaceVariant = rgb("#dfdad9"),
            foreground = rgb("#575279"),
            mutedForeground = rgb("#9893a5"),
            line = rgb("#cecacd"),
            accent = rgb("#56949f"),
            accent2 = rgb("#907aa9"),
            red = rgb("#b4637a"),
            yellow = rgb("#ea9d34"),
            green = rgb("#286983"),
            cyan = rgb("#d7827e"),
            blue = rgb("#56949f"),
            magenta = rgb("#907aa9"),
        ),
    ),
    Nord(
        "Nord",
        Palette(
            isLight = false,
            background = rgb("#2e3440"),
            surface = rgb("#3b4252"),
            surfaceVariant = rgb("#434c5e"),
            foreground = rgb("#d8dee9"),
            mutedForeground = rgb("#667080"),
            line = rgb("#4c566a"),
            accent = rgb("#81a1c1"),
            accent2 = rgb("#b48ead"),
            red = rgb("#bf616a"),
            yellow = rgb("#ebcb8b"),
            green = rgb("#a3be8c"),
            cyan = rgb("#88c0d0"),
            blue = rgb("#81a1c1"),
            magenta = rgb("#b48ead"),
        ),
    ),
    Kanagawa(
        "Kanagawa",
        Palette(
            isLight = false,
            background = rgb("#1f1f28"),
            surface = rgb("#223249"),
            surfaceVariant = rgb("#363646"),
            foreground = rgb("#dcd7ba"),
            mutedForeground = rgb("#727169"),
            line = rgb("#54546d"),
            accent = rgb("#dcd7ba"),
            accent2 = rgb("#957fb8"),
            red = rgb("#c34043"),
            yellow = rgb("#c0a36e"),
            green = rgb("#76946a"),
            cyan = rgb("#6a9589"),
            blue = rgb("#7e9cd8"),
            magenta = rgb("#957fb8"),
        ),
    ),
    Everforest(
        "Everforest",
        Palette(
            isLight = false,
            background = rgb("#2d353b"),
            surface = rgb("#343f44"),
            surfaceVariant = rgb("#3d484d"),
            foreground = rgb("#d3c6aa"),
            mutedForeground = rgb("#4f585e"),
            line = rgb("#475258"),
            accent = rgb("#7fbbb3"),
            accent2 = rgb("#d699b6"),
            red = rgb("#e67e80"),
            yellow = rgb("#dbbc7f"),
            green = rgb("#a7c080"),
            cyan = rgb("#83c092"),
            blue = rgb("#7fbbb3"),
            magenta = rgb("#d699b6"),
        ),
    ),
    ;

    companion object {
        /** Kimono Sumi until the dusk theme has its own wallpaper (#171). */
        val Default = KimonoSumi

        /** The entry stored as [name], or [Default] when nothing is stored or the name no longer exists. */
        fun fromName(name: String?): ThemeId = entries.find { it.name == name } ?: Default
    }
}
