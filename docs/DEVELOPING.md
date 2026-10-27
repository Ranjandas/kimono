# Developing Kimono

How to build Kimono, put it on a Google TV Streamer and get the TV back if something goes wrong.

## Build

You need JDK 17 or newer and the Android SDK (Android Studio brings both), plus fonttools, which the build
uses to subset the bundled font to the glyphs Kimono uses. The Gradle wrapper fetches everything else.

```sh
brew install fonttools       # macOS; elsewhere: pipx install fonttools
pyftsubset --help >/dev/null # should print nothing
```

If `pyftsubset` isn't on the PATH Gradle sees (Android Studio launched from the Dock may not see
Homebrew's), put its full path in `~/.gradle/gradle.properties` as `kimono.pyftsubset=/opt/homebrew/bin/pyftsubset`.

```sh
./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
./gradlew check              # unit tests, Android Lint, ktlint (Spotless) and Detekt (CI adds screenshots)
./gradlew spotlessApply      # fix formatting before committing
```

Icons are glyphs in that font. To add one, find its name on the Nerd Fonts cheat sheet
(https://www.nerdfonts.com/cheat-sheet, Material Design `nf-md-*` icons) and add an entry with its code point
to `KimonoIcon`; the subsetting task picks it up from there.

Lint treats warnings as errors. If a warning is intentional, fix the cause or explain it in the PR; only
regenerate a module's `lint-baseline.xml` (`./gradlew updateLintBaseline`) when the PR says why.

Every PR's CI run also uploads the debug APK. To fetch one without building:

```sh
gh run list --branch <branch> --limit 1           # find the run id
gh run download <run-id> -n kimono-debug-apk -D /tmp/kimono
```

Debug builds from CI and from every machine are signed with the same committed key (`app/debug.keystore`),
so they install over each other. It is a debug key only and never signs a release.

## Recent apps

The recents row on home reads usage stats, which needs usage access. Google TV hides that screen in Settings,
so grant it over adb (and revoke it the same way with `deny`):

```sh
adb shell appops set io.github.ranjandas.kimono GET_USAGE_STATS allow
```

Kimono reads recents again each time it comes back to the front.

## Screenshot tests

Roborazzi renders the home screen and the drawer under Robolectric in every theme × corner style, and compares
them with the PNGs committed under `feature/*/src/test/screenshots/`. CI fails when a screen changes.

```sh
./gradlew verifyRoborazziDebug   # compare with the committed screenshots, as CI does
./gradlew compareRoborazziDebug  # write before/after comparisons to feature/*/build/outputs/roborazzi/
./gradlew recordRoborazziDebug   # overwrite the committed screenshots with the current rendering
```

When a change is meant to alter a screen, update the screenshots in the same PR. Fonts render slightly
differently on macOS, so record on CI's Linux instead of locally: when screenshot verification fails, the CI
run uploads a `screenshots` artifact with freshly recorded images. Check them, then copy them in and commit:

```sh
gh run download <run-id> -n screenshots -D /tmp/kimono-screenshots
cp -R /tmp/kimono-screenshots/. feature/
git add feature/*/src/test/screenshots && git commit -m "Update screenshots"
```

## Set up the Streamer

Do this once per device.

1. **Developer options:** Settings › System › About, select **Android TV OS build** seven times until it says
   you're a developer.
2. **Wireless debugging:** Settings › System › Developer options › **Wireless debugging** on. The screen shows
   the TV's IP address and port.
3. **Pair** (once per computer): on the TV choose **Pair device with pairing code**, then on the computer:

   ```sh
   adb pair <tv-ip>:<pairing-port>     # enter the six-digit code when asked
   ```

4. **Connect** (each session):

   ```sh
   adb connect <tv-ip>:<port>          # the port from the Wireless debugging screen
   adb devices -l                      # should list the TV as "device"
   ```

   If the port changed or isn't shown, `adb mdns services` lists `_adb-tls-connect` entries with the
   current one. Give the TV a fixed IP in your router so the address doesn't move.

With more than one device connected, add `-s <tv-ip>:<port>` to the commands below.

## Install, launch and inspect

```sh
adb install -r app-debug.apk                                   # install or update, keeps settings
adb shell am start -n io.github.ranjandas.kimono/.HomeActivity # launch
adb shell input keyevent KEYCODE_HOME                          # back to the current home screen
adb shell input keyevent KEYCODE_WAKEUP                        # wake the TV if it's asleep
adb exec-out screencap -p > kimono.png                         # screenshot
```

If `install -r` fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the installed copy was signed with a
different key (a build from before the shared debug key, or a release build). Uninstall it once, which
clears Kimono's settings, then install again:

```sh
adb uninstall io.github.ranjandas.kimono
```

### Measuring speed

Debug builds are much slower than what users get, so measure frame times with the benchmark build: optimised
and not debuggable, but signed with the debug key, so it installs over a debug build and keeps settings. CI
uploads it as `kimono-benchmark-apk`.

```sh
adb install -r app-benchmark.apk
adb shell cmd package compile -f -m speed io.github.ranjandas.kimono     # compile ahead of time, as a store install would
adb shell dumpsys gfxinfo io.github.ranjandas.kimono reset
# ...move focus around...
adb shell dumpsys gfxinfo io.github.ranjandas.kimono                     # janky frames and percentiles
```

`run-as` doesn't work on it, since it isn't debuggable; install a debug build again for that.

### Wallpaper

Each theme has a default wallpaper that Kimono downloads on the TV the first time it's shown and keeps
(scaled to 1080p) in its own storage; none are bundled in the app. To use your own, pass an https URL:

```sh
adb shell am start -n io.github.ranjandas.kimono/.HomeActivity --es kimono.wallpaper https://example.com/wall.jpg
adb shell am start -n io.github.ranjandas.kimono/.HomeActivity --es kimono.wallpaper ""   # back to the theme's
```

Settings' wallpaper row turns it off and on again. Its panels row sets how much of the wallpaper shows
through the tiles (clear, frosted or solid); the glass is a blurred copy made once per wallpaper, so it costs
nothing per frame.

### Screensaver

Kimono also offers its idle pixel art as the system screensaver (`KimonoDream`), made from the same cached
wallpaper in the theme's colours, cycling so no pixel stays lit for long. Google TV's settings only list its own
ambient mode, so pick it over adb, and note what was set before so you can put it back:

```sh
adb shell settings get secure screensaver_components   # note this, to restore it later
adb shell settings put secure screensaver_components io.github.ranjandas.kimono/.KimonoDream
adb shell am start -n com.android.systemui/.Somnambulator   # show it now
```

### Logs

```sh
# Kimono's own output while it runs (start Kimono first)
adb logcat --pid=$(adb shell pidof -s io.github.ranjandas.kimono)

# Crashes from any app, including Kimono
adb logcat -b crash

# Everything Kimono logs, even across restarts
adb logcat | grep -i kimono
```

Clear the buffer before reproducing a problem with `adb logcat -c`.

### Is Kimono registered correctly?

```sh
adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER | grep -A3 kimono
adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.HOME | grep -A3 kimono
```

Kimono should appear in both. Installing it does not change which app the Home button opens; see
[PLAN §6.7](PLAN.md#67-home-screen-takeover-on-google-tv).

## Make Kimono the home screen

Google TV ignores the usual choice of home app, so Kimono takes over by disabling the stock launcher and
Google's setup helper ([ADR 0001](adr/0001-home-takeover.md), route A). Two scripts do it and undo it:

```sh
scripts/home-take --dry-run     # print what would change, change nothing
scripts/home-take               # asks you to type "yes", then Home opens Kimono
scripts/home-restore            # Home opens the stock launcher again; safe to run any time
```

Set `ADB_SERIAL=<tv-ip>:<port>` when more than one device is connected. Both scripts print the state before
and after: which app Home opens, the HOME role holder and which of the two packages are disabled.

Before leaving Kimono as home, make sure adb comes back after a reboot. `home-take` warns when it won't
(`adb tcpip` connections don't survive one). Kimono's drawer can still open Settings to re-enable wireless
debugging.

## Recovery

Being a home screen means a bug can leave the TV with nothing usable to show. Before experimenting with home
takeover (M2), make sure wireless debugging is on, you know the TV's IP and port, and `adb devices` works.

**Kimono crashes or hangs on start**

```sh
adb shell am force-stop io.github.ranjandas.kimono
adb uninstall io.github.ranjandas.kimono      # removes it entirely; Google TV takes over again
```

**The stock launcher was disabled** (route A) and you want it back, or Home misbehaves: run
`scripts/home-restore`. By hand, it does this:

```sh
adb shell pm enable com.google.android.tungsten.setupwraith
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
```

Check `adb shell pm list packages -d` for anything else you disabled, and re-enable it the same way.

**adb can't reach the TV**

- Check the TV and computer are on the same network, then `adb disconnect` and `adb connect` again.
- After a reboot the wireless debugging port changes; read the new one on the TV or use `adb mdns services`.
- If the TV has no working home screen and adb is unreachable, open Settings from the remote's settings
  shortcut if your remote has one, and re-enable wireless debugging there. As a last resort, a factory reset
  restores the stock setup; follow Google's instructions for the Streamer.
