package io.github.ranjandas.kimono.data.media

/** Something the user has started watching, from one [MediaSource]. */
data class MediaItem(
    /** The [MediaSource.id] this came from. */
    val sourceId: String,
    /** The item's id within its source. */
    val id: String,
    val title: String,
    val kind: MediaKind,
    /** When it was last watched, in epoch milliseconds. Rows are ordered by this, newest first. */
    val lastWatchedAt: Long,
    /** For an episode, the show and episode, such as "Severance · S2 E4". */
    val subtitle: String? = null,
    /** How far through, from 0 to 1, or null if the source doesn't say. */
    val progress: Float? = null,
    val artworkUrl: String? = null,
    /** A deep link that resumes or opens the item, if the source has one. */
    val launchUri: String? = null,
    /** The app that plays it, used when there's no deep link or it fails. */
    val packageName: String? = null,
)

enum class MediaKind { Movie, Episode, Video, Other }
