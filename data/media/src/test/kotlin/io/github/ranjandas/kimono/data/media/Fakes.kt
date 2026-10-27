package io.github.ranjandas.kimono.data.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeMediaSource(override val id: String, var items: List<MediaItem> = emptyList()) : MediaSource {
    var error: Throwable? = null
    var calls = 0

    override suspend fun continueWatching(): List<MediaItem> {
        calls++
        error?.let { throw it }
        return items
    }
}

class InMemoryMediaCache(initial: List<MediaItem> = emptyList()) : MediaCache {
    val state = MutableStateFlow(initial)

    override fun items(): Flow<List<MediaItem>> = state

    override suspend fun replace(sourceId: String, items: List<MediaItem>) {
        state.update { cached -> cached.filterNot { it.sourceId == sourceId } + items }
    }

    override suspend fun retainSources(sourceIds: Set<String>) {
        state.update { cached -> cached.filter { it.sourceId in sourceIds } }
    }
}

fun item(sourceId: String, id: String, lastWatchedAt: Long) = MediaItem(
    sourceId = sourceId,
    id = id,
    title = "Title $id",
    kind = MediaKind.Episode,
    lastWatchedAt = lastWatchedAt,
)
