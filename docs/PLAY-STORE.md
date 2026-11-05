# Play Store readiness

Where Kimono Launcher stands for a Play Store release (#48). The Play Console account exists (2026-11-05); the
step-by-step walkthrough lives in the project's release doc.

## Policy review

| item | status | notes |
|---|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Fine | Wallpaper downloads, and the top bar's online indicator. |
| `PACKAGE_USAGE_STATS` | Fine with disclosure | A special permission the user grants in Settings. Used for recents and screen time; disclosed in the privacy policy. The app works without it. |
| `REQUEST_DELETE_PACKAGES` | Fine | Uninstall from an app's options, a core launcher feature; the system confirms each one. Check the current Play form at submission. |
| Package visibility | Fine | `<queries>` limited to `LEANBACK_LAUNCHER` and `LAUNCHER` activities; no `QUERY_ALL_PACKAGES`. |
| `READ_TV_LISTINGS` | Debug builds only | The Watch Next probe (#143); not in release builds. |
| Accessibility service | None shipped | The Home takeover's route B (ADR 0001) would need one, and Play's accessibility API policy would need a declaration and a prominent disclosure. Keep it sideload-only or out of the Play build. |
| Home takeover | Not in the app | Route A is adb-only and stays in docs. |
| Brand logos | Check at submission | Other apps' logos only next to the app they open (plan risks table). |
| Data safety form | Ready to fill | No data collected or shared; see the privacy policy. |
| Target SDK | Fine | targetSdk 37. |
| TV quality | To check | App icon and banner (640×360 px in drawable-xhdpi, 320×180 dp), D-pad only, no touch required, leanback launcher intent. |

## Privacy policy

[PRIVACY.md](PRIVACY.md), published at https://gist.github.com/Ranjandas/effdee2510f62f3bb81f66630712b4e1 until the
repo goes public. Use that URL in Play Console.

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

**Graphics:** in [store/](store/): the 512×512 icon, the 1024×500 feature graphic and the 1280×720 TV banner. The
mark is a vermilion name seal (hanko) with a carved K. `node scripts/store-art/render.js` redraws them and the
launcher banner from `scripts/store-art/art.html`; the app icon itself is the vector in
`app/src/main/res/drawable/ic_launcher_foreground.xml`.
