package io.github.ranjandas.kimono.data.media.cache

import androidx.room.Entity
import io.github.ranjandas.kimono.data.media.MediaItem
import io.github.ranjandas.kimono.data.media.MediaKind

@Entity(tableName = "media_items", primaryKeys = ["sourceId", "id"])
internal data class MediaItemEntity(
    val sourceId: String,
    val id: String,
    val title: String,
    val kind: MediaKind,
    val lastWatchedAt: Long,
    val subtitle: String?,
    val progress: Float?,
    val artworkUrl: String?,
    val launchUri: String?,
    val packageName: String?,
)

internal fun MediaItemEntity.toItem() = MediaItem(
    sourceId = sourceId,
    id = id,
    title = title,
    kind = kind,
    lastWatchedAt = lastWatchedAt,
    subtitle = subtitle,
    progress = progress,
    artworkUrl = artworkUrl,
    launchUri = launchUri,
    packageName = packageName,
)

internal fun MediaItem.toEntity() = MediaItemEntity(
    sourceId = sourceId,
    id = id,
    title = title,
    kind = kind,
    lastWatchedAt = lastWatchedAt,
    subtitle = subtitle,
    progress = progress,
    artworkUrl = artworkUrl,
    launchUri = launchUri,
    packageName = packageName,
)
