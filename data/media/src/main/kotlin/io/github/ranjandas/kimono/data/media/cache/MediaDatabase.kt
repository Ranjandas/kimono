package io.github.ranjandas.kimono.data.media.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Only a cache: on a schema change it's dropped and refilled from the sources, so there are no migrations. */
@Database(entities = [MediaItemEntity::class], version = 1, exportSchema = false)
internal abstract class MediaDatabase : RoomDatabase() {
    abstract fun items(): MediaItemDao

    companion object {
        @Volatile private var instance: MediaDatabase? = null

        fun get(context: Context): MediaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, MediaDatabase::class.java, "media-cache.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}
