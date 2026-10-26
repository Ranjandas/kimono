# ADR 0001: Home-screen takeover on Google TV

- **Status:** Proposed. The routes below haven't been tried on the Streamer yet. Ranjan asked for that to wait
  until he says so, because it changes what the Home button does on his TV.
- **Date:** 2026-10-26
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

Two things follow from this:

- **Disabling `launcherx` alone won't be enough.** The next HOME candidate is `setupwraith`'s
  `RecoveryActivity` at priority 1, ahead of Kimono at 0. Some community guides for Google TV disable `setupwraith`
  as well, which matches this ordering. It still needs confirming here.
- **Projectivy is a HOME candidate too.** With both stock activities out of the way, Android would show a
  chooser between Projectivy and Kimono. Picking Kimono with "always" settles it.

## Options

### A. Disable the stock launcher over adb

```sh
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith   # if Home still lands on RecoveryActivity
```

Then press Home and choose Kimono, "always".

- **For:** Home goes straight to Kimono with no flash. Kimono comes back on boot as the real home activity.
  It needs no special permissions, so the Play listing is unaffected.
- **Against:**
  - It needs adb, so it's fine for Ranjan's own TV but not for other people.
  - Disabling `setupwraith` may break the Google TV setup flow and the account and profile screens that hang
    off it.
  - A Play Store update to `launcherx` or a system OTA may re-enable it. That needs checking after each one.
  - Google TV features that live in `launcherx` go away: the For You row, Ambient and the profile switcher.
    Kimono's own ambient mode (M4) covers part of this.
- **Undo:**

  ```sh
  adb shell pm enable com.google.android.tungsten.setupwraith
  adb shell pm enable com.google.android.apps.tv.launcherx
  ```

  If adb isn't reachable, use Settings > Apps > See all apps > Show system apps > Google TV Home > Enable, if
  Settings can still be opened from a remote shortcut. A factory reset is the last resort.

### A′. Give Kimono the HOME role

```sh
adb shell cmd role add-role-holder android.app.role.HOME io.github.ranjandas.kimono
```

This is the "supported" form of choosing a home app, and it doesn't disable anything. On Google TV,
`launcherx` is expected to win it back, or Home is expected to bypass the role. That should be tried first,
because undoing it is just `remove-role-holder` and it is the least invasive.

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

## Proposed decision

1. **For Ranjan's Streamer and for development:** try A′ first. If Home still goes to `launcherx`, use A with
   only `launcherx` disabled, then add `setupwraith` only if Home lands on `RecoveryActivity`. Check after
   reboot, after a `launcherx` update and after an OTA. Package this as the M2-2 scripts (`scripts/home-take`
   and `scripts/home-restore`), with the undo steps above in DEVELOPING.md.
2. **For everyone else:** build B as an optional onboarding step (M2-3), sideload-only until the Play
   accessibility policy is checked against a draft listing.

## Spike checklist

To fill in on the Streamer once Ranjan gives the go-ahead. Finish every session with the TV asleep.

- [ ] A′: does `add-role-holder` change what Home opens? Does it survive a reboot?
- [ ] A: with `launcherx` disabled, where does Home go? Is there a chooser? Is `setupwraith` needed?
- [ ] A: after a reboot, does Kimono come up? How long until first draw?
- [ ] A: does Settings still open? Do account, profile and input settings still work?
- [ ] A: undo restores stock Home exactly.
- [ ] B: with a minimal watcher, how long is the stock-launcher flash on Home? Does it survive a reboot and an
      app update?
- [ ] Record the `launcherx` version tested, so a later update that re-enables it can be spotted.

## Consequences

- Kimono must cope with being the real home activity: Home arrives as `onNewIntent` (#27), and there's no stock
  launcher to fall back to if Kimono crashes. A crash loop would need adb to recover, which is one more reason
  route A stays a developer route.
- Google TV's own home features go away under route A. M3 (continue watching) and M4 (ambient) are the
  replacements.
