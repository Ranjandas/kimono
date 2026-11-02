# Accessibility

What Kimono does for TalkBack, contrast and text size, and how it's checked (#45). The rules come from the plan's
design principles (PLAN §6.5 and principle 7).

## TalkBack

- Everything that takes focus has a name: an app's label, a setting's name, a workspace's number and name, or a
  content description for icon-only buttons (the settings gear, the status items in the top bar).
- Icons are decorative. `Icon` clears its semantics, so TalkBack never reads a Nerd Font glyph; the label next to it
  carries the meaning.
- A setting reads as its name with the value as its state ("theme, kimono"). Left and Right are also offered as
  TalkBack actions, "previous value" and "next value".
- `app/src/test/.../AccessibilityTest.kt` renders home, all apps, the app options panel and settings, and fails if
  anything focusable has no name or if an icon glyph reaches TalkBack.

## Contrast

Every colour pair is computed from the palette and checked in `PaletteTest`, so a new theme can't ship below these
lines. Corner style changes only shapes, so it doesn't affect contrast.

| pair | minimum | where |
|---|---|---|
| body text on tiles and the screen | 4.5:1 | everywhere |
| muted text on tiles and the screen | 4.5:1 | metadata, hints, dates |
| text and muted text on a focused row (the accent-tinted fill) | 4.5:1 | lists, settings values |
| text and muted text in an input box (`surfaceVariant`) | 4.5:1 | the search box |
| secondary accent on tiles | 4.5:1 | keys in the system tile |
| accent focus border on tiles and the screen | 3:1 | WCAG 1.4.11 |
| accent label on a focused row | 3:1 | bold 14 sp (28 px) is large text |
| text on an accent fill | 4.5:1 | the selected workspace chip, focused buttons |

Results per theme:

| theme | muted, focused row | muted, input box | accent label, focused row | 2nd accent on tiles | accent on tiles |
|---|---|---|---|---|---|
| Kimono | 4.5 | 4.6 | 4.9 | 10.6 | 5.7 |
| Tokyo Night | 4.5 | 5.5 | 4.3 | 5.4 | 5.8 |
| Catppuccin | 4.6 | 4.6 | 4.4 | 8.2 | 6.0 |
| Gruvbox | 4.6 | 4.5 | 3.6 | 4.5 (was 4.2) | 4.7 |
| Rosé Pine | 4.5 | 4.7 | 3.4 | 4.5 (was 3.2) | 4.1 |
| Nord | 4.5 | 4.9 | 3.0 (was 2.98) | 4.5 (was 3.6) | 3.8 |
| Kanagawa | 4.5 | 6.1 | 6.0 | 4.5 (was 3.7) | 8.9 |
| Everforest | 4.5 | 5.2 | 3.7 | 4.7 | 5.0 |

The audit found three gaps, now fixed:

- Muted text on a focused row and in the search box was 3.1–4.2:1 in every theme. It now uses `textMutedFocused`,
  tuned to those fills.
- The secondary accent was too dark for text in four themes. It's nudged towards the foreground just far enough,
  keeping its hue.
- Nord's accent label on its own focused-row tint was just under 3:1. The accent nudge now checks that too.

Left as designed: the list numbers in front of rows are muted at 60% (about 2.5:1) on purpose, as a quiet guide to
the number keys. They're also in each row's position, so nothing is lost if they can't be read.

Not covered: over a wallpaper with clear or frosted panels, contrast depends on the image. The panels setting has
`solid` for a wallpaper too busy to read over.

## Text size

Settings has a text size (default and large, 1.15×), on top of the system font scale. The screenshot tests render
home and all apps once with large text, and settings at each size in a dark and a light theme, so a layout that
clips or overflows at large text shows up in review.
