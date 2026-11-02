package io.github.ranjandas.kimono

import android.content.ContentResolver
import android.database.Cursor
import android.util.Log
import androidx.core.net.toUri

/**
 * Debug builds only: dumps the TV provider's Watch Next and preview rows to logcat, tag `KimonoWatchNext` (#143).
 * The query needs `READ_TV_LISTINGS`. With it, TvProvider returns this app's rows and every row marked searchable.
 */
internal object WatchNextProbe {
    const val TAG = "KimonoWatchNext"

    private val tables = listOf(
        "watch_next_program" to "content://android.media.tv/watch_next_program".toUri(),
        "preview_program" to "content://android.media.tv/preview_program".toUri(),
    )

    private val columns = listOf(
        "package_name",
        "title",
        "episode_title",
        "type",
        "watch_next_type",
        "channel_id",
        "last_playback_position_millis",
        "duration_millis",
        "last_engagement_time_utc_millis",
        "intent_uri",
        "poster_art_uri",
        "thumbnail_uri",
        "browsable",
        "searchable",
    )

    /** Logs every row and returns a one-line summary per table. */
    fun dump(resolver: ContentResolver): List<String> = tables.map { (name, uri) ->
        try {
            resolver.query(uri, null, null, null, null)?.use { dumpRows(name, it) } ?: "$name: no cursor"
        } catch (e: SecurityException) {
            Log.w(TAG, "$name: permission denied", e)
            "$name: permission denied"
        }
    }

    private fun dumpRows(name: String, cursor: Cursor): String {
        val packages = sortedMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val row = columns.mapNotNull { column ->
                cursor.getColumnIndex(column).takeIf { it >= 0 }?.let { "$column=${cursor.getString(it)}" }
            }
            Log.i(TAG, "$name #${cursor.position}: ${row.joinToString(" | ")}")
            val pkg = cursor.getColumnIndex("package_name").takeIf { it >= 0 }?.let(cursor::getString) ?: "?"
            packages[pkg] = (packages[pkg] ?: 0) + 1
        }
        val summary = "$name: ${cursor.count} rows $packages"
        Log.i(TAG, summary)
        return summary
    }
}
