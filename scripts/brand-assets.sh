#!/usr/bin/env bash
# Renders the launcher banners and the Play graphics from docs/brand/mark.svg. Needs rsvg-convert and ImageMagick.
set -euo pipefail
cd "$(dirname "$0")/.."

mark=docs/brand/mark.svg
bold=core/design/fonts/NotoSans-Bold.ttf
regular=core/design/fonts/NotoSans-Regular.ttf
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

# banner <width> <height> <title> <title size> <output> [subtitle]
# The mark is trimmed to the garment and centred with the title beside it, well inside the safe area.
banner() {
    local width=$1 height=$2 title=$3 size=$4 out=$5 subtitle=${6:-}
    local markSize=$((height * 5 / 8))
    rsvg-convert -w "$markSize" "$mark" -o "$tmp/mark.png"
    magick "$tmp/mark.png" -trim +repage "$tmp/trimmed.png"
    magick -background none -fill '#ffe9d6' -font "$bold" -pointsize "$size" label:"$title" "$tmp/title.png"
    if [ -n "$subtitle" ]; then
        magick -background none -fill '#9a93a3' -font "$regular" -pointsize "$((size * 2 / 5))" label:"$subtitle" "$tmp/sub.png"
        magick "$tmp/title.png" "$tmp/sub.png" -background none -gravity West -append "$tmp/title.png"
    fi
    magick "$tmp/trimmed.png" \( -size "$((size * 2 / 5))x1" xc:none \) "$tmp/title.png" \
        -background none -gravity Center +append "$tmp/lockup.png"
    magick -size "${width}x${height}" xc:'#000000' "$tmp/lockup.png" -gravity Center -composite \
        -alpha off -strip -define png:color-type=2 "$out"
}

res=app/src/main/res
banner 320 180 kimono 40 "$res/drawable-xhdpi/banner.png"
banner 640 360 kimono 80 "$res/drawable-xxxhdpi/banner.png"

# Play Console. It applies its own mask to the icon, so that one is full bleed with no circle.
rsvg-convert -w 512 -b '#000000' "$mark" -o docs/brand/play-icon-512.png
banner 1024 500 "Kimono Launcher" 64 docs/brand/play-feature-1024x500.png "A calm home screen for your TV"
banner 1280 720 "Kimono Launcher" 80 docs/brand/play-tv-banner-1280x720.png
