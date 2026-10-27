package io.github.ranjandas.kimono.data.media.cache

import io.github.ranjandas.kimono.data.media.MediaCache
import io.github.ranjandas.kimono.data.media.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomMediaCache(database: MediaDatabase) : MediaCache {
    private val dao = database.items()

    override fun items(): Flow<List<MediaItem>> = dao.observeAll().map { rows -> rows.map { it.toItem() } }

    override suspend fun replace(sourceId: String, items: List<MediaItem>) {
        dao.replaceSource(sourceId, items.map { it.toEntity() })
    }

    override suspend fun retainSources(sourceIds: Set<String>) {
        // SQLite's NOT IN () with an empty list is a syntax error, so clear everything directly.
        if (sourceIds.isEmpty()) dao.deleteAll() else dao.deleteSourcesOtherThan(sourceIds.toList())
    }
}
