# Kimono Launcher privacy policy

*Draft for the Play listing (#48). Last updated 2026-11-02.*

Kimono Launcher is a home screen for Android TV and Google TV. It has no account, no analytics, no ads and no
crash reporting, and it sends nothing about you or your TV anywhere.

## What stays on your TV

- **Your settings, pins and workspaces.** Stored in Kimono's own app storage. Uninstalling Kimono deletes them.
  Kimono doesn't take part in Android backup.
- **Which apps you open from Kimono.** Kept on the TV so home can show your recent apps.
- **App usage, if you allow it.** With usage access turned on, Kimono reads the system's own record of which apps
  were used, and for how long today, to show recent apps and screen time on home. It's read on the TV and never
  leaves it. You can turn usage access off at any time.
- **Your list of installed apps.** Read so Kimono can show and open them.

## What Kimono downloads

The theme wallpapers are downloaded the first time a theme needs one, from fixed GitHub addresses
(raw.githubusercontent.com), and kept on the TV. If you set a wallpaper of your own, Kimono downloads it from the
address you gave. Those requests carry only what any web request carries (your TV's IP address); Kimono adds
nothing about you. Kimono's own theme draws its wallpapers on the TV and downloads nothing.

## Permissions

| permission | why |
|---|---|
| Internet, network state | Downloading wallpapers, and showing in the top bar whether the TV is online |
| Usage access (optional, you turn it on) | Recent apps and screen time on home |
| Request to delete apps | "Uninstall" in an app's options; Android asks you to confirm every time |
| See installed apps (launcher apps only) | Showing and opening your apps |

## Children

Kimono collects no personal information from anyone, children included.

## Changes and contact

Changes to this policy are published with the app's releases. Questions: open an issue at
https://github.com/Ranjandas/kimono/issues.
