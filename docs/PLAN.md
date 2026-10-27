# Kimono — project plan

> A text-first, tiling Android TV launcher inspired by [Omarchy](https://omarchy.org).
> *Kimono* (着物) means "thing to wear": the launcher is what your TV wears, and themes are the garments.

| | |
|---|---|
| Status | Draft for review |
| Last updated | 2026-10-26 |
| Owner | @Ranjandas |
| Design mockups | Kimono design canvas (private Claude artifact — ask the owner for access) |

---

## 1. Summary

Kimono replaces the stock Google TV home screen with a calm, keyboard-ish, terminal-flavoured launcher:
monospace type, square tiles separated by gaps, an accent-coloured border on whatever has focus, and a
top bar with numbered workspaces — the Omarchy/Hyprland desktop translated to a 10-foot, D-pad-driven screen.

The first target is a single device (Google TV Streamer) installed by sideloading. A Play Store release
is the long-term goal, so decisions that would block it (package name, licensing, permission use) are made
with that in mind from day one.

## 2. Goals and non-goals

### Goals

- **G1 — Fast, legible home screen.** Every app reachable in a few D-pad presses; text readable from the sofa.
- **G2 — Omarchy look and feel.** Tiled layout, monospace type, themes ported from Omarchy's palettes,
  switchable at runtime.
- **G3 — Be the real home screen** on Google TV Streamer, including surviving the Home button and reboots.
- **G4 — Useful "continue watching"** from sources a third-party launcher can actually read.
- **G5 — Ambient polish.** Wallpaper with frosted panels; an idle mode where artwork dissolves into
  theme-coloured pixel art and wakes back to sharp on input.
- **G6 — Play Store-ready architecture.** No dependence on anything that only works via adb.

### Non-goals (for now)

- Phones, tablets or Android Automotive.
- Reading other apps' "Watch Next" rows (needs a system permission third-party apps can't get).
- Live TV / tuner inputs, EPG.
- Being a media player. Kimono launches apps; it does not play content.
- Cloud accounts or sync of Kimono's own settings.

## 3. Decisions so far

| # | Decision | Choice | Notes |
|---|---|---|---|
| D1 | Name | **Kimono** (device label), **Kimono TV** / **Kimono Launcher** in store listings | Bare "kimono" is a generic word; a qualifier helps search and avoids claiming the word itself. Trademark/store search before any Play release (M5). |
| D2 | Package ID | `io.github.ranjandas.kimono` | Permanent once published on Play. |
| D3 | First device | Google TV Streamer (Google TV, Android TV 14) | Other Google TV / Android TV devices later. |
| D4 | Distribution | Sideload first; Play Store later | Sideload builds come from GitHub Releases. |
| D5 | License | MIT | Third-party palettes and fonts credited in `NOTICE`. |
| D6 | Stack | Kotlin, Jetpack Compose for TV (`androidx.tv:tv-material`), single activity | See §6. |
| D7 | Repo / codename | `kimono` | The "omatv" name in early mockups is retired. |
| D8 | Icons | Glyphs from **Symbols Nerd Font** (includes Font Awesome Free, Material Design Icons and more), subset at build time | Icons are text, so they line up with labels and take theme colours automatically. Nerd Fonts is MIT/OFL, Font Awesome Free icons are CC BY 4.0, all credited in `NOTICE`. |
| D10 | Text font | **Noto Sans** everywhere (decided 2026-10-27, replacing JetBrains Mono) | Monospace read as too "dev" for everyday viewers and is harder to read from the sofa. Noto covers most scripts app names use. OFL. |
| D9 | Corner style | User setting, independent of colour theme: **square** (default) · **soft** (~6 dp) · **round** (~12 dp) | One shape token drives every component. |

### Decisions still open

| # | Question | Default if undecided | Needed by |
|---|---|---|---|
| O1 | Media source(s) for "continue watching" | Recently used apps first, then Trakt + TMDB (see §6.8) | M3 |
| O2 | minSdk | 31 (Android 12): covers current Google TV devices and gives `RenderEffect` blur | M0 |
| O3 | Dependency injection | Manual DI (one `AppContainer`); revisit if it hurts | M0 |
| O4 | How wallpapers get onto the device | Bundled set + import from a URL | M4 |
| O5 | Crash reporting | None; maybe opt-in later | M5 |
| O6 | What each workspace contains | 1 home · 2 watch · 3 play · 4 all apps, each a full page, user-pinnable (was five; changed by [ADR 0003](adr/0003-full-page-workspaces.md)) | M1 |

## 4. Design principles

Taken from the mockups; these settle arguments during implementation.

1. **Text first.** App names over logos. Artwork only where it carries information (media).
2. **Tiles with gaps.** One 2 px border per tile: accent colour when focused, line colour otherwise.
   Corners are square by default and configurable (square / soft / round) through one shape setting.
3. **One plain typeface.** Noto Sans, bundled (D10); the Omarchy feel comes from the tiles, colours and `~/` labels.
4. **Icons accompany text, never replace it.** Every icon sits next to a label; icons are muted by default
   and take the accent colour with focus. One icon family per context.
5. **Themes are data.** A theme is a palette plus a few semantic tokens; no screen hard-codes a colour or a radius.
6. **D-pad first.** Every screen works with ◀ ▶ ▲ ▼, OK, Back, Home. Focus is always visible and never lost.
7. **10-foot legibility.** Body text ≥ 14 sp (28 px at 1080p), contrast ≥ 4.5:1 in every theme.
8. **Calm, not bland.** No auto-playing trailers, ads or unearned recommendations — but icons, the second
   accent colour, subtle focus motion, wallpaper and idle pixel art keep it alive.

## 5. Feature overview

| Area | What the user sees | Milestone |
|---|---|---|
| Top bar | Workspaces 1–4, current workspace name, clock/date, network, volume, theme switcher | M1 |
| Home tiles | Continue watching, recent apps, system info ("fetch" tile), numbered app list | M1–M3 |
| App drawer | Full-screen text list grouped by category (watch / listen / play / system) with type-to-filter | M1 |
| App options | Long-press OK: pin to workspace, app info, uninstall | M1 |
| Themes | Tokyo Night (default), Catppuccin, Gruvbox, Rosé Pine, Nord, Kanagawa, Everforest | M1 |
| Corner style | Square (default), soft or round corners on every element, independent of theme | M1 |
| Icons | Nerd Font icons in the top bar, workspaces, app list and drawer (brand icon or category fallback), tile headers, system info, hints and settings | M1 |
| Home takeover | Kimono comes back on Home and after reboot | M2 |
| Continue watching | Title, episode, progress, resume / details | M3 |
| Artwork treatment | Posters with colours mapped to the theme palette | M3 |
| Wallpaper | Image background with frosted panels | M4 |
| Idle mode | Artwork dissolves to pixel art and chrome dims after inactivity; any key wakes without acting | M4 |
| Screensaver | Optional pixel-poster wall as an Android screensaver (`DreamService`) | M4 |
| Settings | Theme, corner style, wallpaper, idle timeout, clock format, text size, workspace pins | M1–M4 |

## 6. Architecture

### 6.1 Shape of the app

- **Single activity** (`HomeActivity`) hosting Compose for TV, with `launchMode="singleTask"`,
  excluded from recents, and these intent filters:
  - `MAIN` + `HOME` + `DEFAULT` — eligible to be the home app.
  - `MAIN` + `LEANBACK_LAUNCHER` — appears in the Google TV apps row for manual launch.
- **Unidirectional data flow:** repositories expose `Flow`s → screen `ViewModel`s combine them into an
  immutable UI state (`StateFlow`) → composables render and send intents back.
- **Gradle modules** (kept small on purpose; split further only when build times or boundaries demand it):

```
:app                 HomeActivity, navigation, AppContainer (DI), manifest
:core:design         Theme model, palettes, typography, Tile/Kbd/TopBar components
:core:data           Settings (DataStore), shared models, image pipeline (palette mapping, pixelation)
:feature:home        Home screen, tiles, workspaces, idle mode
:feature:drawer      App drawer + search
:feature:settings    Settings screens, onboarding (permissions, home takeover)
:data:apps           Installed apps, launch intents, package-change updates, usage stats
:data:media          MediaSource interface + providers (Trakt, TMDB, later Plex/Jellyfin), Room cache
```

### 6.2 Key libraries

| Need | Choice |
|---|---|
| UI | Compose for TV (`androidx.tv:tv-material`), Compose foundation |
| Settings | Jetpack DataStore (Preferences) |
| Cache | Room |
| Networking | OkHttp + Retrofit + kotlinx.serialization |
| Images | Coil 3 with custom transformations |
| Font and icons | Noto Sans for text; Symbols Nerd Font for icons, subset at build time to the glyphs Kimono uses (fonttools in a Gradle task) |
| Background refresh | WorkManager |
| Tests | JUnit 5, Turbine, Robolectric, Roborazzi (screenshot tests per theme) |
| Quality | Spotless (ktlint), Android Lint, Detekt |

### 6.3 Installed apps

- Query activities for `LEANBACK_LAUNCHER` (falling back to `LAUNCHER`) via `PackageManager`.
  Android 11+ package visibility is handled with `<queries>` intent filters, **not** `QUERY_ALL_PACKAGES`
  (which Play restricts).
- Keep the list fresh with `LauncherApps.Callback` (installs, removals, updates).
- Group in the drawer by `ApplicationInfo.category` (video / audio / game / other) with user overrides.

### 6.4 Focus and input

- Compose for TV focus APIs: `focusRestorer`, `FocusRequester`, `focusProperties` for explicit
  tile-to-tile moves so focus never jumps unexpectedly between grid regions.
- One `InputMonitor` at the activity level (`dispatchKeyEvent`) feeds the idle controller and
  workspace shortcuts. It never consumes keys except the first key after idle (wake-only).

### 6.5 Theming, shape and icons

**Colour**
- `KimonoTheme` = palette (`bg`, `surface`, `surface2`, `fg`, `muted`, `line`, `accent`, `accent2`, `ink`,
  plus 6 terminal colours) → semantic tokens consumed by components.
- Palettes ported from the themes Omarchy ships, each credited in `NOTICE` with its upstream licence
  (to be verified per theme in M1).
- Every theme passes an automated contrast check (text on surface ≥ 4.5:1) in unit tests.

**Shape**
- A `CornerStyle` setting (square / soft / round) maps to one base radius exposed through the theme;
  no component hard-codes a radius.
- Nested elements use `inner radius = outer radius − padding` (never below 0) so curves stay parallel.
- The focus border, artwork clipping (posters and pixel art) and workspace indicators (pills when rounded)
  all follow the same shape.
- Corner style is stored separately from the colour theme, so any palette works with any shape.

**Icons**
- Icons are glyphs from the bundled Nerd Font, rendered as text, so they inherit colour, size and
  alignment from the surrounding typography and recolour with the theme for free.
- A `KimonoIcons` object names every UI icon semantically (`Wifi`, `Volume`, `WorkspaceMedia`, …) so
  screens never reference raw code points.
- Apps get an icon from a package → icon map (brand icon where the font has one), falling back to a
  category icon. Brand icons stay monochrome and theme-coloured; they are trademarks of their owners and
  only shown next to the app they open.
- Icons are decorative for accessibility (the adjacent label carries the meaning), so TalkBack never
  reads private-use characters.
- The font is subset at build time to the glyphs in `KimonoIcons` and the app map, keeping the APK small.

### 6.6 Artwork pipeline

One image pipeline in `:core:data`, used by M3 and M4:

1. Load poster/backdrop with Coil.
2. **Palette mapping:** recolour towards the active theme's palette, keeping the image recognisable.
3. **Pixelation (idle mode):** downscale to 16×9 (or 32×18), quantise to the palette with
   4×4 Bayer dithering, upscale with nearest-neighbour. Computed once per image × theme and cached.

### 6.7 Home-screen takeover on Google TV

Google TV ignores the normal "choose default home app" flow, so the Home button keeps opening the stock
launcher. Two routes, to be confirmed in the M2 spike:

| Route | How | Use | Risks |
|---|---|---|---|
| A. Disable stock launcher (adb) | `adb shell pm disable-user --user 0 <stock launcher package>`; restore with `pm enable` | Development and personal use | Can break setup or updates; an OTA may re-enable it; needs a documented recovery path. |
| B. Accessibility watcher | An `AccessibilityService` notices the stock launcher coming to the front and brings Kimono forward (the approach used by existing third-party TV launchers) | Product route, works without adb | Brief flash of the stock launcher; Play Store restricts accessibility-API use, so the listing must justify it or the feature ships sideload-only. |

The spike records findings in an ADR (`docs/adr/0001-home-takeover.md`) before either route is built.

### 6.8 Continue watching (media sources)

Third-party launchers can't read Google TV's Watch Next data, so Kimono gets this from sources it can reach
behind a `MediaSource` interface:

| Order | Source | Gives | Needs |
|---|---|---|---|
| 1 | **Recently used apps** (`UsageStatsManager`) | Real "recents" row, no account | Usage access, granted once in Settings or via adb |
| 2 | **Trakt** | Shows and films in progress, up-next episodes, across services | Trakt account; something reporting viewing to Trakt. Device-code login suits a TV. API terms to be checked in M3. |
| 2b | **TMDB** | Posters, backdrops, metadata | API key; TMDB attribution in the UI |
| 3 | **Plex / Jellyfin** (later) | Exact resume points from a home server | User runs a server |

**Known limitation:** "resume" can usually only open the right app or a details page, not resume playback
at the exact point, unless that app supports deep links. M3 includes per-app deep-link research and a
fallback (open app, then a Google TV search intent).

### 6.9 Ambient mode

- **Wallpaper:** drawn by Kimono (not the system wallpaper). A pre-blurred copy is made once, and each
  panel draws its own crop of it, so frosted glass costs nothing per frame.
- **Idle controller:** after *N* seconds without input (default 45 s), artwork dissolves to its pixelated
  version tile by tile and chrome dims. The first key press only wakes.
- **OLED care:** while idle, dim borders and the top bar and shift the layout by a few pixels periodically.
- **Screensaver (optional):** a `DreamService` showing a slowly changing pixel-poster wall, so idle mode
  hands over smoothly to the system screensaver.

### 6.10 Privacy and permissions

| Permission | Why | When requested |
|---|---|---|
| `INTERNET` | Media sources, artwork | M3 |
| Usage access (`PACKAGE_USAGE_STATS`) | Recent apps | M2, from onboarding with an explanation |
| Accessibility service | Home takeover (route B) | M2, optional, from onboarding |
| `RECEIVE_BOOT_COMPLETED` | Return to Kimono after reboot if needed | M2, if the spike shows it's necessary |

No analytics. Tokens for Trakt/TMDB are stored with Android Keystore-backed encryption.

## 7. Milestones

Each milestone ends with a build sideloaded onto the Streamer and a short demo note in the milestone's
closing issue.

### M0 — Foundation
*Exit:* an empty Kimono launcher builds in CI, installs on the Streamer, and appears in the apps row.

### M1 — Launcher core
*Exit:* Kimono is usable as a manually opened launcher: themed home screen, app list, drawer with search,
workspaces and settings, all D-pad driven.

### M2 — Becoming the home screen
*Exit:* on the Streamer, Home and reboots bring back Kimono (route A for dev, route B if viable);
system info and recents tiles show real data.

### M3 — Continue watching
*Exit:* continue-watching tile and up-next row show real items with theme-mapped artwork, and resume opens
the right app.

### M4 — Ambient
*Exit:* wallpaper with frosted panels, idle pixel mode with wake-only input, OLED care; optional screensaver.

### M5 — Release readiness
*Exit:* signed builds on GitHub Releases; performance, accessibility and policy reviews done; store name
decided.

## 8. Work breakdown

Sizes: **S** ≤ half a day · **M** 1–2 days · **L** 3–5 days. Each item becomes one GitHub issue.

### M0 — Foundation
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M0-1 | Repo hygiene: LICENSE (MIT), README, NOTICE, .gitignore, .editorconfig, issue and PR templates | S | — |
| M0-2 | Gradle scaffold: version catalog, modules from §6.1, Compose for TV, minSdk/targetSdk, package ID | M | M0-1 |
| M0-3 | Launcher manifest: HOME + LEANBACK_LAUNCHER filters, banner, singleTask; shows in apps row | S | M0-2 |
| M0-4 | CI (GitHub Actions): build, unit tests, lint on every PR; debug APK as artifact | M | M0-2 |
| M0-5 | Code quality: Spotless/ktlint, Detekt, Android Lint baselines | S | M0-4 |
| M0-6 | Developer guide: wireless adb to the Streamer, install, logs, recovery steps | S | M0-3 |

### M1 — Launcher core
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M1-1 | Theme system: palette model, 7 Omarchy palettes, semantic tokens, `CornerStyle` shape token, contrast test, NOTICE credits | M | M0-2 |
| M1-2 | Typography and font: bundle JetBrains Mono Nerd Font with a build-time subsetting task, type scale for 10-foot UI | M | M1-1 |
| M1-3 | Base components: Tile (focus border), TextButton, Kbd hint, section header — all reading colour and shape from the theme, nested-radius rule | M | M1-1 |
| M1-4 | Installed-apps repository: query, sort, `<queries>`, `LauncherApps.Callback` updates | M | M0-2 |
| M1-5 | App launching: launch intents, missing-activity handling, launch feedback | S | M1-4 |
| M1-6 | Home layout: tiled grid per mockup with placeholder tiles | M | M1-3 |
| M1-7 | Apps tile: numbered text list, focus marker, OK launches | S | M1-5, M1-6 |
| M1-8 | Focus model: tile-to-tile navigation, focus restore, initial focus, Back behaviour | M | M1-6 |
| M1-9 | Top bar: workspaces, clock/date, network and volume status, theme switcher | M | M1-3 |
| M1-10 | Workspaces: model, switching, per-workspace pins, persistence (DataStore) | M | M1-9 |
| M1-11 | App drawer: grouped text list by category, type-to-filter search | L | M1-4, M1-3 |
| M1-12 | App options (long-press OK): pin to workspace, app info, uninstall | M | M1-10 |
| M1-13 | Settings screen: theme, corner style, clock format, text size; persistence | M | M1-1 |
| M1-14 | Contextual hint bar (bottom) | S | M1-8 |
| M1-15 | Screenshot tests (Roborazzi) for home and drawer in every theme × corner style | M | M1-6, M1-11 |
| M1-16 | Icon system: `KimonoIcons` semantic set, app package → icon map with category fallback, icons in top bar / workspaces / lists / tile headers / hints, decorative semantics for TalkBack | M | M1-2, M1-3 |

### M2 — Becoming the home screen
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M2-1 | Spike + ADR: home takeover on Google TV Streamer (routes A and B) | M | M0-3 |
| M2-2 | Dev scripts: set Kimono as home via adb, and restore stock launcher | S | M2-1 |
| M2-3 | Accessibility-based home takeover + onboarding (if M2-1 says viable) | L | M2-1 |
| M2-4 | Home/resume behaviour: Home returns to workspace 1, state survives process death | M | M1-10 |
| M2-5 | System info ("fetch") tile: device, Android version, uptime, storage, network, theme | S | M1-6 |
| M2-6 | Recent apps from usage stats + permission onboarding | M | M1-4 |

### M3 — Continue watching
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M3-1 | Spike: Trakt and TMDB API terms, limits, auth; decide O1 | S | — |
| M3-2 | `MediaSource` interface, Room cache, WorkManager refresh | M | M3-1 |
| M3-3 | Trakt device-code login + secure token storage | M | M3-2 |
| M3-4 | Trakt provider: in-progress items and up-next episodes | M | M3-3 |
| M3-5 | TMDB artwork + attribution | M | M3-2 |
| M3-6 | Continue-watching tile and up-next row UI | M | M3-4, M3-5 |
| M3-7 | Resume/open: per-app deep-link research, fallback chain | M | M3-6 |
| M3-8 | Artwork pipeline: palette mapping transformation (shared with M4) | M | M1-1 |

### M4 — Ambient
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M4-1 | Wallpaper: bundled set + import from URL; settings UI | M | M1-13 |
| M4-2 | Frosted panels from a pre-blurred wallpaper | M | M4-1 |
| M4-3 | Idle controller: timeout, wake-only first key, settings | M | M1-8 |
| M4-4 | Pixelated artwork (Bayer dither, cached) with staggered dissolve | M | M3-8, M4-3 |
| M4-5 | OLED care: dim chrome and periodic pixel shift while idle | S | M4-3 |
| M4-6 | Optional screensaver (`DreamService`) with pixel-poster wall | M | M4-4 |

### M5 — Release readiness
| ID | Issue | Size | Depends on |
|---|---|---|---|
| M5-1 | Performance: startup time, frame time on Streamer, memory, baseline profile | M | M4 |
| M5-2 | Accessibility: TalkBack labels, contrast audit, text scaling | M | M1–M4 |
| M5-3 | Signed release builds, GitHub Releases with APKs, changelog | M | M0-4 |
| M5-4 | Name/trademark check, store name, icon and 320×180 banner | S | — |
| M5-5 | Play Store readiness: permission and accessibility policy review, privacy policy, listing | M | M5-4 |

## 9. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Home takeover doesn't work reliably on the Streamer | Medium | High | Spike first (M2-1); route A keeps personal use working; ship route B as optional. |
| Play rejects the accessibility-based takeover | Medium | Medium | Keep it a separate optional feature; a sideload build keeps it regardless. |
| Trakt API terms or limits don't fit | Medium | Medium | `MediaSource` abstraction; recents work without any account; Plex/Jellyfin as alternatives. |
| Resume can't deep-link into streaming apps | High | Low | Honest "open in app" labelling; deep links where apps support them. |
| Compose for TV focus bugs or gaps | Low | Medium | Explicit focus properties per region; screenshot and UI tests for navigation. |
| Frosted panels too slow | Low | Low | Pre-blurred wallpaper; opaque-panel fallback setting. |
| Theme palettes' licences | Low | Low | Verify each upstream licence in M1-1; credit in NOTICE. |
| Bundled Nerd Font bloats the APK | Medium | Low | Subset to used glyphs at build time (M1-2). |
| Brand icons raise trademark concerns in a store listing | Low | Low | Monochrome, theme-coloured, only next to the app they open; category icons as fallback; review in M5-5. |
| Rounded corner style looks off in some layouts | Low | Low | One shape token, nested-radius rule, screenshot tests per corner style. |

## 10. Working conventions

- **Issues:** one per row in §8, titled `[M1-4] Installed-apps repository`, with acceptance criteria,
  milestone, and labels.
- **Labels:** `area:design`, `area:home`, `area:drawer`, `area:settings`, `area:apps`, `area:media`,
  `area:ambient`, `area:platform`, `area:infra`, `area:docs` · `type:feature`, `type:spike`, `type:chore`,
  `type:bug` · `size:S`, `size:M`, `size:L`.
- **Branches:** `<type>/<short-name>` (e.g. `feat/app-drawer`), one PR per issue, squash-merged into `main`.
- **ADRs:** decisions with lasting consequences go in `docs/adr/NNNN-title.md`.
- **Definition of done:** CI green, screenshot tests updated, tried on the Streamer, docs touched if
  behaviour changed.
