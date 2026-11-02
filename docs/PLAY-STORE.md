# Play Store readiness

Where Kimono Launcher stands for a Play Store release (#48). There's no Play Console account yet (2026-11-02), so
this stops at the review, the privacy policy draft and a listing draft.

## Policy review

| item | status | notes |
|---|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Fine | Wallpaper downloads only. |
| `PACKAGE_USAGE_STATS` | Fine with disclosure | A special permission the user grants in Settings. Used for recents and screen time; disclosed in the privacy policy. The app works without it. |
| `REQUEST_DELETE_PACKAGES` | Fine | Uninstall from an app's options, a core launcher feature; the system confirms each one. Check the current Play form at submission. |
| Package visibility | Fine | `<queries>` limited to `LEANBACK_LAUNCHER` and `LAUNCHER` activities; no `QUERY_ALL_PACKAGES`. |
| `READ_TV_LISTINGS` | Debug builds only | The Watch Next probe (#143); not in release builds. |
| Accessibility service | None shipped | The Home takeover's route B (ADR 0001) would need one, and Play's accessibility API policy would need a declaration and a prominent disclosure. Keep it sideload-only or out of the Play build. |
| Home takeover | Not in the app | Route A is adb-only and stays in docs. |
| Brand logos | Check at submission | Other apps' logos only next to the app they open (plan risks table). |
| Data safety form | Ready to fill | No data collected or shared; see the privacy policy. |
| Target SDK | Fine | targetSdk 37. |
| TV quality | To check | Banner 320×180 (exists), D-pad only, no touch required, leanback launcher intent. |

## Privacy policy

Drafted in [PRIVACY.md](PRIVACY.md). Play needs it at a public URL. The repo is private, so it has to be published
somewhere public first (GitHub Pages from a public repo, or a gist).

## Listing draft

**App name:** Kimono Launcher

**Short description (80):** A calm, text-first home screen for Google TV, with themes from Omarchy.

**Full description:**

> Kimono Launcher is a quiet home screen for Android TV and Google TV. No rows of ads or suggested shows, just
> your apps, arranged your way.
>
> - Workspaces for home, watch, play and all apps. Move between them with Left and Right at the screen's edge, or
>   with the number keys.
> - Pin any app to any workspace and put pins in your own order.
> - Your recent apps, a clock and a small system panel.
> - Eight colour themes, including Kimono's own true-black theme made for OLED TVs, with matching wallpapers.
> - Frosted, clear or solid panels, three corner styles and a larger text size.
> - Gentle on OLED panels: the screen dims when idle, pixels shift slowly, and the screensaver keeps moving.
> - Works with the Google TV phone remote: everything is D-pad, OK and Back.
>
> No account, no ads, no tracking. Your settings stay on your TV.

**Category:** Personalization (or Tools if Personalization isn't offered for TV).

**Screenshots:** 1920×1080 captures from the Streamer: home (Kimono theme), watch workspace, all apps, settings,
one Omarchy theme with a wallpaper. Take them with `adb exec-out screencap -p` once the look is final.

**Still needed:** the final icon and banner (#47), a Play Console account, and the privacy policy's public URL.
