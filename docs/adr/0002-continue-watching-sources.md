# ADR 0002: Where "continue watching" comes from

- **Status:** Accepted, 2026-10-27: no media source for now. Recently used apps stay the answer.
  Revisited 2026-11-02 after the Watch Next spike (M3-9, #143): still no source.
- **Date:** 2026-10-27
- **Issue:** #30 (M3-1), decides PLAN open question O1. See [PLAN §6.8](../PLAN.md#68-continue-watching-media-sources).

## Context

Google TV's own "continue watching" row comes mostly from the user's Google account (apps report to it through
the Engage SDK), so a third-party launcher can't show it. The on-device Watch Next table in TvProvider is a
different thing: it is readable, which this ADR first got wrong (see the spike below). Kimono needs a source it
can reach. The plan's default was recently used apps first, then Trakt for progress
and TMDB for artwork, with Plex or Jellyfin later. Recently used apps shipped in M2 (#72).

## What the terms say (checked 2026-10-27)

### Trakt

- **Use:** standard API access is free for commercial and non-commercial use, with no approval step. A media
  app reading the signed-in user's progress is the kind of use the policy describes. Selling or redistributing
  Trakt data, scraping, and dodging rate limits are not allowed.
- **Attribution:** an About or Settings page must say "Kimono uses Trakt but is not endorsed or certified by
  Trakt." The logo is optional, and if used must follow the branding guide.
- **Rate limits:** authenticated GETs are 500 per 5 minutes per user. POST, PUT and DELETE are 1 per second.
  Clients should read the `X-Ratelimit` and `Retry-After` headers rather than hard-code these.
- **Caching:** allowed, with no fixed maximum. Images must be cached. Catalogue data may not be kept as a
  reusable copy after the integration ends.
- **Auth:** the device-code flow suits a TV: `/oauth/device/code`, then poll `/oauth/device/token` at the
  given interval until it expires. Access tokens last 7 days. Refresh tokens are single-use, so each refresh
  must store the new pair. The docs call the client secret deprecated for user sign-in. Whether the device
  token call still needs it has to be checked when M3-3 is built.
- **Account:** creating an app needs a Trakt account linked to a verified GitHub account. Apps unused for 30
  days are deleted.
- **Privacy:** Kimono must say what it stores and how to disconnect.

### TMDB

- **Use:** free for non-commercial use with attribution. Commercial use, where the main purpose is revenue,
  needs a licence. A free, open-source launcher is non-commercial.
- **Attribution:** an About or Credits section must say "This product uses the TMDB API but is not endorsed
  or certified by TMDB", with the TMDB logo as their logo page describes.
- **Key:** each build needs an API key from a TMDB account.

### The catch

Trakt only knows what something reports to it. It's useful when a player on the TV scrobbles to Trakt, such
as Jellyfin with its Trakt plugin, Kodi, Plex or some third-party apps. Netflix, Prime Video, Disney+ and
YouTube don't report to Trakt by themselves. Without a scrobbling source, a Trakt-backed row stays empty.

## Options

| Option | Gives | Needs | Exact resume |
|---|---|---|---|
| A. Trakt + TMDB (the plan's default) | In-progress films and shows, up-next episodes, across any app that scrobbles | Trakt account and app, TMDB key, a scrobbling player | No; opens the app or a details page |
| B. Jellyfin first | Resume points and up-next from Ranjan's own server, with artwork | Jellyfin server URL and a sign-in (Quick Connect suits a TV) | Yes, via Jellyfin's resume data and deep links |
| C. Both, behind `MediaSource` | Whichever the user connects | Either set of the above | Per source |

Jellyfin is installed on the Streamer, so B may give the best result for Ranjan's own viewing.

## Proposed decision

Build `MediaSource` (M3-2) so that both can plug in, then do the first source based on Ranjan's answer below.
If he's undecided, Trakt + TMDB stays the default, as in the plan.

## Decision

Asked which source to build first (Jellyfin, Trakt + TMDB, or neither), Ranjan chose **neither yet**.

- `MediaSource`, its cache and the background refresh (M3-2, #84) stay in place, so either option can be added
  later without reworking anything.
- The Trakt login and provider (M3-3, M3-4) and TMDB artwork (M3-5) are parked until a source is wanted.
- The recently used apps row (#72) is how Kimono gets back to what was being watched.

## Consequences if a source is added later

- Secrets: the Trakt client ID (and secret, if still required) and the TMDB key can't be committed. They go in
  `local.properties` locally and in repository secrets for CI, and are read through `BuildConfig`.
- An About section in settings carries the Trakt and TMDB notices.
- Settings needs a "connect / disconnect" entry per source, and the privacy section must say what is cached.

## Watch Next spike (M3-9, 2026-11-02)

The first version of this ADR said reading other apps' Watch Next rows needs a system permission. That's wrong.
`android.permission.READ_TV_LISTINGS` is a `dangerous` permission, so it's a normal runtime prompt (or
`adb shell pm grant`). With it, TvProvider returns the caller's own rows plus every row marked `searchable = 1`,
the column's default. `ACCESS_ALL_EPG_DATA` and `ACCESS_WATCHED_PROGRAMS` are `signature|privileged` and aren't
needed. Projectivy Launcher uses the same route.

### What the Streamer has

A debug-only probe (`app/src/debug/.../WatchNextProbe.kt`) declares the permission and dumps
`content://android.media.tv/watch_next_program` and `preview_program` to logcat (tag `KimonoWatchNext`). Its
receiver runs while the TV sleeps:

```
adb shell pm grant io.github.ranjandas.kimono android.permission.READ_TV_LISTINGS
adb shell am broadcast -n io.github.ranjandas.kimono/.WatchNextProbeReceiver
adb logcat -d -s KimonoWatchNext
```

Run on the Google TV Streamer (Android 14) on 2026-11-02, with 15 third-party apps installed:

| Table | Rows | From |
|---|---|---|
| `watch_next_program` | **1** | Prime Video: one film at 6 of 41 minutes, last watched 2026-10-10 (3.5 weeks old) |
| `preview_program` | 113 | SmartTube 62, YouTube 24, Prime Video 22, Jellyfin 5 |

- Netflix, Disney+, Stan, Kayo, ABC iview, SBS, 9Now, 10, 7plus and Jellyfin wrote no Watch Next rows, or
  wrote them as not searchable, which hides them from other apps. Either way Kimono can't see them.
- The one Watch Next row is good data: title, position, duration, poster and an `intent_uri` that resumes the
  exact title (`https://app.primevideo.com/watch?gti=…&time=auto`).
- Preview rows are each app's home-screen channels: recommendations (Prime Video, YouTube), subscriptions
  (SmartTube) and library sections (Jellyfin). They have no playback position, so they aren't
  "continue watching". They all carry deep links and artwork.
- Plain `adb shell content query` returns "No result found." for both tables, because shell doesn't hold the
  permission. Use the probe.

### Decision

**Don't build a `WatchNextMediaSource` now.** One stale row from one app is not a "continue watching" row,
and Google is moving apps off the Watch Next API to the Engage SDK (support reported to end in the second half
of 2027), so the table will likely get emptier. Recently used apps stay the answer.

- The probe stays in debug builds, so the check can be repeated if the apps on the Streamer change.
- If Watch Next is revisited, it fits the `MediaSource` plumbing (#84) with no changes: one query, rows already
  carry the deep link, and only a runtime permission prompt is needed.
- Preview channels are a possible later idea ("what's new in Jellyfin / YouTube" rows), not continue watching.
  Not planned.
