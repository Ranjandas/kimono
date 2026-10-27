# ADR 0002: Where "continue watching" comes from

- **Status:** Proposed. Needs Ranjan's answer on which source he actually uses (see Open question).
- **Date:** 2026-10-27
- **Issue:** #30 (M3-1), decides PLAN open question O1. See [PLAN §6.8](../PLAN.md#68-continue-watching-media-sources).

## Context

Google TV keeps its own "continue watching" (Watch Next) to itself, so a third-party launcher can't show it.
Kimono needs a source it can reach. The plan's default was recently used apps first, then Trakt for progress
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

## Open question

Which does Ranjan watch through, and does anything report it to Trakt? Specifically:

- A Jellyfin server, which would point to B.
- Trakt with a scrobbling player, which would point to A.
- Mostly streaming apps with nothing reporting, where neither helps much and the recent-apps row is the honest
  answer.

## Consequences

- Secrets: the Trakt client ID (and secret, if still required) and the TMDB key can't be committed. They go in
  `local.properties` locally and in repository secrets for CI, and are read through `BuildConfig`.
- An About section in settings carries the Trakt and TMDB notices.
- Settings needs a "connect / disconnect" entry per source, and the privacy section must say what is cached.
