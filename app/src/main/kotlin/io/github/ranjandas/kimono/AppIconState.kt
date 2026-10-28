package io.github.ranjandas.kimono

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import io.github.ranjandas.kimono.core.data.icons.IconStyle
import io.github.ranjandas.kimono.core.design.component.AppMarks
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.icon.AppMarkBitmaps
import io.github.ranjandas.kimono.data.apps.icon.AppMarkCache

/** What the tiles show for each app ([marks]), and the icon style each app is set to ([styles]). */
@Immutable
internal class AppIconState(val marks: AppMarks, val styles: Map<String, IconStyle>)

/**
 * Each app's mark in the style chosen for it (#116). Marks already made show at once; the rest fill in as they're
 * made, and their tiles show monograms until then. The marks from before stay up while a new app list loads.
 */
@Composable
internal fun rememberAppMarks(
    cache: AppMarkCache,
    apps: List<InstalledApp>?,
    styles: Map<String, IconStyle>,
): AppMarks {
    val bitmaps by produceState(emptyMap<String, AppMarkBitmaps>(), cache, apps) {
        apps?.let { list -> cache.marks(list).collect { value = it } }
    }
    return remember(bitmaps, styles) {
        AppMarks(
            bitmaps.mapNotNull { (packageName, marks) ->
                marks.forStyle(styles[packageName] ?: IconStyle.Default)?.let { packageName to it.asImageBitmap() }
            }.toMap(),
        )
    }
}

// A wordmark falls back to the logo for apps without a banner; monogram shows no mark.
internal fun AppMarkBitmaps.forStyle(style: IconStyle): Bitmap? = when (style) {
    IconStyle.Logo -> logo
    IconStyle.Wordmark -> wordmark ?: logo
    IconStyle.Monogram -> null
}
