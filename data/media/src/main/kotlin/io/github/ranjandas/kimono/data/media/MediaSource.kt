package io.github.ranjandas.kimono.data.media

/** Somewhere Kimono reads "continue watching" from, such as a Jellyfin server or a Trakt account. */
interface MediaSource {
    /** Stable across launches, since cached items are kept under it. */
    val id: String

    /** What the user is part-way through. Throws if the source can't be reached; the cache is kept then. */
    suspend fun continueWatching(): List<MediaItem>
}
