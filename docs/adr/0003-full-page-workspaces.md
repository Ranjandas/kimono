# ADR 0003: Full-page workspaces

- **Status:** Accepted, 2026-10-27
- **Date:** 2026-10-27
- **Issue:** #98. Changes PLAN decision O6.

## Context

After using Kimono for a while with the Google TV phone remote app, Ranjan found three problems:

- **Some things need keys most remotes lack.** The drawer opened only on Menu or Search. Workspaces switched
  quickly only with the number keys or channel up/down. The phone remote app has none of these keys, and neither
  does the Google TV Streamer remote as far as we know. Google TV also takes the Search key for Assistant. With
  the D-pad alone, switching workspace meant going Up to the top bar, pressing Left several times, then OK, and
  focus then jumped back into the list.
- **A switch was hard to see.** A workspace changed only the apps tile in the bottom right. Continue watching,
  recents and system info stayed the same, so switching barely showed.
- **The workspaces overlapped.** Home and apps listed the same apps, and system was empty until something was
  pinned to it.

Other launchers do one of two things. Some swap the whole screen for each category: the Google TV tabs and
Projectivy's categories. Others stack categories as rows on one page: FLauncher and the classic Android TV home
screen. None of them has a category that changes one panel.

## Options

1. **Full-page workspaces.** Each workspace is its own page, and switching slides the whole page.
2. **One scrolling page.** There are no workspaces; rows of recents, favourites, watch, play and all apps
   are navigated with Up and Down.
3. **Keep the grid and fix only the navigation.**

## Decision

Option 1, chosen by Ranjan on 2026-10-27. It's the closest to Omarchy, where a workspace is a whole screen, and
it makes every switch obvious.

- There are four workspaces: **1 home, 2 watch, 3 play, 4 all apps**. All apps is the drawer, so it's one Right
  press past play and needs no Menu key.
- **Left and Right at the edge of a page switch workspace**, wrapping from the last back to the first. A held
  key stops at the edge, and only a fresh press switches. The new page slides in from the side the switch went
  to. Left and Right never leave a page for the top bar.
- Number keys 1 to 4, channel up/down and Menu/Search still work as shortcuts, but the hint bar doesn't name
  them, because most remotes don't have them. On all apps, digits go to the search instead.
- All apps has a real text field for search, so the on-screen keyboard and the phone app's keyboard work. OK on
  the field brings up the keyboard.
- Stored state carries over. Media pins move to watch, games pins to play, and apps and system pins to home.
  Pins made from all apps go to home.
- Back on watch and play goes to home. On all apps, Back clears the search first.

## Consequences

- Home keeps its grid for now. The look of the pages (monogram tiles, a "last used" tile in place of the
  continue-watching placeholder) and pinning to any workspace follow in later PRs, tracked under #98.
- Until pinning to any workspace lands (#106), apps can only be pinned to watch or play from that page itself,
  and an empty watch or play page can only point to all apps.
