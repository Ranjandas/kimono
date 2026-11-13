package io.github.ranjandas.kimono.core.design.icon

/**
 * Icons are glyphs in the bundled Nerd Font (PLAN D8), named by what they mean so screens never use
 * raw code points. All come from the Material Design Icons set, so they share one style.
 *
 * The font subsetting task keeps only the code points written here as `0x…` literals, so an icon is
 * added by adding an entry. Each entry's comment is its Nerd Fonts name, for finding it on the cheat sheet.
 */
enum class KimonoIcon(val codePoint: Int) {
    // Navigation and controls
    Home(0xF02DC), // nf-md-home
    Apps(0xF003B), // nf-md-apps
    Search(0xF0349), // nf-md-magnify
    Settings(0xF0493), // nf-md-cog
    Remote(0xF0454), // nf-md-remote
    Play(0xF040A), // nf-md-play
    Pause(0xF03E4), // nf-md-pause
    Pin(0xF0403), // nf-md-pin
    Info(0xF02FD), // nf-md-information_outline
    ChevronLeft(0xF0141), // nf-md-chevron_left
    ChevronRight(0xF0142), // nf-md-chevron_right
    Power(0xF0425), // nf-md-power

    // Status
    Wifi(0xF05A9), // nf-md-wifi
    WifiOff(0xF05AA), // nf-md-wifi_off
    Ethernet(0xF0200), // nf-md-ethernet
    Volume(0xF057E), // nf-md-volume_high
    VolumeOff(0xF0581), // nf-md-volume_off
    Clock(0xF0150), // nf-md-clock_outline

    // Workspaces and app groups
    Media(0xF0502), // nf-md-television
    Music(0xF075A), // nf-md-music
    Games(0xF0297), // nf-md-gamepad_variant
    System(0xF062E), // nf-md-tune

    // Tiles and system info
    Continue(0xF040D), // nf-md-play_circle_outline
    Recent(0xF02DA), // nf-md-history
    Device(0xF0839), // nf-md-television_box
    Os(0xF0032), // nf-md-android
    Launcher(0xF14DE), // nf-md-rocket_launch
    Uptime(0xF051B), // nf-md-timer_outline
    Idle(0xF04B2), // nf-md-sleep
    Storage(0xF02CA), // nf-md-harddisk
    Font(0xF06D6), // nf-md-format_font
    Activity(0xF0430), // nf-md-pulse
    Memory(0xF035B), // nf-md-memory
    Cpu(0xF061A), // nf-md-chip
    Heat(0xF050F), // nf-md-thermometer
    Traffic(0xF04E2), // nf-md-swap_vertical

    // Settings
    Palette(0xF03D8), // nf-md-palette
    Corners(0xF0607), // nf-md-rounded_corner
    TextSize(0xF027F), // nf-md-format_size
    Wallpaper(0xF02E9), // nf-md-image
    Glass(0xF00B5), // nf-md-blur

    // Apps: brands first, then the fallbacks
    YouTube(0xF05C3), // nf-md-youtube
    Netflix(0xF0746), // nf-md-netflix
    Spotify(0xF04C7), // nf-md-spotify
    Plex(0xF06BA), // nf-md-plex
    Kodi(0xF0314), // nf-md-kodi
    Twitch(0xF0543), // nf-md-twitch
    Hulu(0xF0829), // nf-md-hulu
    Vlc(0xF057C), // nf-md-vlc
    Steam(0xF04D3), // nf-md-steam
    GooglePlay(0xF02BC), // nf-md-google_play
    Chrome(0xF02AF), // nf-md-google_chrome
    Firefox(0xF0239), // nf-md-firefox
    PhoneApp(0xF011C), // nf-md-cellphone
    AppFallback(0xF08C6), // nf-md-application
    ;

    /** The glyph as a string, for use in `Text`. */
    val text: String get() = String(Character.toChars(codePoint))
}
