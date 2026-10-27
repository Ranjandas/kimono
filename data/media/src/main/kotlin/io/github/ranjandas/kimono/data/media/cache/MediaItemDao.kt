package io.github.ranjandas.kimono.data.media.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY lastWatchedAt DESC")
    abstract fun observeAll(): Flow<List<MediaItemEntity>>

    @Query("DELETE FROM media_items WHERE sourceId = :sourceId")
    abstract suspend fun deleteSource(sourceId: String)

    @Query("DELETE FROM media_items WHERE sourceId NOT IN (:sourceIds)")
    abstract suspend fun deleteSourcesOtherThan(sourceIds: List<String>)

    @Query("DELETE FROM media_items")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<MediaItemEntity>)

    @Transaction
    open suspend fun replaceSource(sourceId: String, items: List<MediaItemEntity>) {
        deleteSource(sourceId)
        insertAll(items)
    }
}
