package io.github.ranjandas.kimono.data.media

import android.content.Context
import io.github.ranjandas.kimono.data.media.cache.MediaDatabase
import io.github.ranjandas.kimono.data.media.cache.RoomMediaCache
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** "Continue watching" across every connected source, served from the cache and refreshed in the background. */
class MediaRepository(
    private val sources: List<MediaSource>,
    private val cache: MediaCache,
    private val limit: Int = DEFAULT_LIMIT,
) {
    val hasSources: Boolean get() = sources.isNotEmpty()

    /** The most recently watched items first, at most [limit] of them. */
    val continueWatching: Flow<List<MediaItem>> = cache.items()
        .map { items -> items.sortedByDescending { it.lastWatchedAt }.take(limit) }
        .distinctUntilChanged()

    /** Asks each source for its items. A source that fails keeps its cached items and is named in the result. */
    suspend fun refresh(): RefreshResult {
        cache.retainSources(sources.mapTo(mutableSetOf()) { it.id })
        val failed = mutableSetOf<String>()
        for (source in sources) {
            runCatching { source.continueWatching() }
                .onSuccess { cache.replace(source.id, it) }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    failed += source.id
                }
        }
        return RefreshResult(refreshed = sources.mapTo(mutableSetOf()) { it.id } - failed, failed = failed)
    }

    companion object {
        const val DEFAULT_LIMIT = 20

        /** Sources are added here as they're built (Jellyfin, Trakt). There are none yet. */
        fun create(context: Context): MediaRepository =
            MediaRepository(emptyList(), RoomMediaCache(MediaDatabase.get(context)))
    }
}

data class RefreshResult(val refreshed: Set<String>, val failed: Set<String>)
