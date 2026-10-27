package io.github.ranjandas.kimono.data.media

import kotlinx.coroutines.flow.Flow

/** Keeps the last items from each source, so the row shows straight away and survives the network being down. */
interface MediaCache {
    fun items(): Flow<List<MediaItem>>

    /** Replaces everything cached for [sourceId] with [items]. */
    suspend fun replace(sourceId: String, items: List<MediaItem>)

    /** Drops items from any source not in [sourceIds], such as one the user disconnected. */
    suspend fun retainSources(sourceIds: Set<String>)
}
