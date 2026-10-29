package io.github.ranjandas.kimono.core.data.wallpaper

/**
 * The last [capacity] values put in, by key, dropping the one used longest ago to make room. Safe to use from
 * any thread.
 */
internal class RecentlyUsed<K, V>(private val capacity: Int) {
    // Access order, so the eldest entry is the one used longest ago.
    private val entries = object : LinkedHashMap<K, V>(capacity, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > capacity
    }

    @Synchronized
    operator fun get(key: K): V? = entries[key]

    @Synchronized
    operator fun set(key: K, value: V) {
        entries[key] = value
    }
}

/** The items to drop to keep only the [limit] newest by [age], a timestamp: the oldest first. */
internal fun <T> oldestBeyond(items: List<T>, limit: Int, age: (T) -> Long): List<T> =
    items.sortedByDescending(age).drop(limit).reversed()

private const val LOAD_FACTOR = 0.75f
