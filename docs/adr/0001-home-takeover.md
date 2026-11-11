# ADR 0001: Home-screen takeover on Google TV

- **Status:** Accepted for route A (development and Ranjan's own TV), tried on the Streamer on 2026-10-27.
  Route B is built as the home watcher (#26), in every build except the Play bundle.
- **Date:** 2026-10-26, results added 2026-10-27
- **Issue:** #24 (M2-1). See [PLAN §6.7](../PLAN.md#67-home-screen-takeover-on-google-tv).

## Context

Kimono declares itself a home app (`MAIN` + `HOME` + `DEFAULT` on a `singleTask` activity), but Google TV never
offers the usual "choose your home app" step. The Home button always goes to the stock launcher. To be the home
screen, Kimono has to take over one of two ways.

### What the Streamer reports

These are read-only queries over adb. Nothing on the device was changed to collect them.

| Fact | Value |
|---|---|
| Device | Google TV Streamer (`kirkwood`), Android 14 (SDK 34), build `UTTK.260317.003`, patch 2026-04-01 |
| Default home (`resolve-activity` HOME) | `com.google.android.apps.tv.launcherx/.home.HomeActivity` |
| `android.app.role.HOME` holder | `com.google.android.apps.tv.launcherx` |
| Stock launcher | `launcherx` 1.0.1002449598: a system app updated from the Play Store (`UPDATED_SYSTEM_APP`, installer `com.android.vending`) |
| Other HOME candidates, by priority | `setupwraith/.RecoveryActivity` (1), Projectivy `MainActivity` (0), Kimono `HomeActivity` (0), Settings `FallbackHome` (-1000) |
| Also installed | `com.google.android.tvlauncher` (the older launcher), `tvrecommendations`, `dreamx` |
| Accessibility | Off: `accessibility_enabled=0`, no enabled or bound services. Projectivy declares `ProjectivyAccessibilityService` but it isn't on. |

From this ordering it looked like disabling `launcherx` alone wouldn't be enough, since `setupwraith`'s
`RecoveryActivity` (priority 1) outranks Kimono (0). The spike below confirmed that.

### What the spike found (2026-10-27)

Tried over adb on the Streamer with `launcherx` 1.0.1002449598. Every change was undone at the end, and the final
state matched the starting one: `launcherx` as HOME role holder and resolved home, nothing disabled.

| Step | Result |
|---|---|
| A′: `cmd role add-role-holder android.app.role.HOME io.github.ranjandas.kimono` | The role moved to Kimono, but `resolve-activity` HOME still gave `launcherx`, and four Home presses all opened the stock launcher. **The role alone doesn't change Home.** `launcherx`'s priority-2 HOME filter wins. Removing the role handed it back to `launcherx` by itself. |
| A: `pm disable-user --user 0 …launcherx` | Within about 2 s, `setupwraith`'s `RecoveryActivity` took the role and became home. It kept relaunching (its task id climbed by ~300 in 20 s) and showed the Settings screen. **Not usable on its own.** |
| A: then `pm disable-user --user 0 …setupwraith` | The role passed to Kimono automatically and Home opened Kimono **directly, with no chooser**, even though Projectivy is also a HOME candidate. Kimono ran in a `type=home` task. |
| Home from inside an app | Went to Kimono on workspace 1. Focus landed on the app's own row rather than row 01, fixed in #76. |
| Settings with both disabled | Android TV Settings opens from Kimono's drawer. Accounts and profiles, and System, both open. Nothing was changed. |
| Undo | `pm enable` both packages, then `cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity` ("Success"). The role went back to `launcherx`, and Home opened the stock launcher. |
| Reboot | Not tried. adb on the Streamer runs over `adb tcpip 5555`, which doesn't survive a reboot, and the wireless-debugging port changes on boot, so a reboot could cut off adb while the stock launcher is disabled. |

## Options

### A. Disable the stock launcher over adb

```sh
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith   # if Home still lands on RecoveryActivity
```

Both are needed on the Streamer (see the spike). Home then opens Kimono straight away, with no chooser.

- **For:** Home goes straight to Kimono with no flash. Kimono comes back on boot as the real home activity.
  It needs no special permissions, so the Play listing is unaffected.
- **Against:**
  - It needs adb, so it's fine for Ranjan's own TV but not for other people.
  - Disabling `setupwraith` may break the Google TV setup flow. Account, profile and system settings still
    opened in the spike, but the first-run setup and adding a profile weren't tried.
  - A Play Store update to `launcherx` or a system OTA may re-enable it. That needs checking after each one.
  - Google TV features that live in `launcherx` go away: the For You row, Ambient and the profile switcher.
    Kimono's own ambient mode (M4) covers part of this.
- **Undo:**

  ```sh
  adb shell pm enable com.google.android.tungsten.setupwraith
  adb shell pm enable com.google.android.apps.tv.launcherx
  adb shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
  ```

  If adb isn't reachable, use Settings > Apps > See all apps > Show system apps > Google TV Home > Enable, if
  Settings can still be opened from a remote shortcut. A factory reset is the last resort.

### A′. Give Kimono the HOME role

```sh
adb shell cmd role add-role-holder android.app.role.HOME io.github.ranjandas.kimono
```

This is the "supported" form of choosing a home app, and it doesn't disable anything. **It doesn't work on the
Streamer**: the role moves, but Home keeps resolving to `launcherx`. Ruled out.

### B. Accessibility watcher

An `AccessibilityService` watches window changes. When `launcherx` comes to the front, it starts Kimono. This is
how Projectivy and other third-party TV launchers work without adb.

- **For:** It works for anyone, and turning off the service in Settings undoes it.
- **Against:**
  - The stock launcher flashes briefly on every Home press.
  - The service has to be switched on by hand in Settings > Accessibility, and Android may turn it off after
    an app update or a crash.
  - Google Play restricts the Accessibility API to apps whose core purpose is accessibility. A launcher listing
    would need a declaration and could be rejected, so this may have to ship sideload-only.
  - The service can see window contents across apps, so its config must limit it to window-state events and
    the README has to say what it reads.

## Decision

1. **For Ranjan's Streamer and for development:** route A, disabling both `launcherx` and `setupwraith`. Package
   it as the M2-2 scripts (`scripts/home-take` and `scripts/home-restore`), with the undo steps above in
   DEVELOPING.md. Before leaving it on long term, make adb survive a reboot (wireless debugging re-paired, or a
   USB route), then check reboot, a `launcherx` update and an OTA.
2. **For everyone else:** build B as an optional onboarding step (M2-3), sideload-only until the Play
   accessibility policy is checked against a draft listing.

## Spike checklist

Finish every session with the TV asleep.

- [x] A′: does `add-role-holder` change what Home opens? No.
- [x] A: with `launcherx` disabled, where does Home go? To `RecoveryActivity`, in a relaunch loop. `setupwraith`
      is needed too, and then there's no chooser.
- [x] A: after a reboot, does Kimono come up? Yes (2026-11-11): about 2 s of Android's FallbackHome, then Kimono
      as home, about 35 s from `adb reboot`. Both packages stay disabled. Wireless debugging is off after the reboot.
- [x] A: does Settings still open? Yes, with accounts, profiles and system. Input settings weren't opened.
- [x] A: undo restores stock Home exactly.
- [ ] B: with a minimal watcher, how long is the stock-launcher flash on Home? Does it survive a reboot and an
      app update?
- [x] Record the `launcherx` version tested: 1.0.1002449598.

## Consequences

- Kimono must cope with being the real home activity: Home arrives as `onNewIntent` (#27), and there's no stock
  launcher to fall back to if Kimono crashes. A crash loop would need adb to recover, which is one more reason
  route A stays a developer route.
- Google TV's own home features go away under route A. M3 (continue watching) and M4 (ambient) are the
  replacements.
