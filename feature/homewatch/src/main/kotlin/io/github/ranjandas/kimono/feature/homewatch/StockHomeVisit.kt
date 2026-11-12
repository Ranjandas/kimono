package io.github.ranjandas.kimono.feature.homewatch

/**
 * Whether the user is visiting Google TV's own home from Kimono (#198), so the watcher leaves it alone. Kept in
 * memory only: the watcher runs in Kimono's process, so the app and the service share [current].
 *
 * The visit ends when Google TV's home goes behind another app, which is when it reports its home pane gone. Moving
 * between its tabs, its profile chooser, its dashboard and the voice search overlay don't do that, so they stay part
 * of the visit (checked on the Streamer, launcherx 1.0.1002449598). The next time that home comes to the front,
 * Kimono opens over it again. Kimono coming back to the front ends a visit too.
 */
class StockHomeVisit {
    @Volatile
    var isOpen: Boolean = false
        private set

    fun start() {
        isOpen = true
    }

    fun end() {
        isOpen = false
    }

    companion object {
        /** The one visit the app and the watcher share. */
        val current = StockHomeVisit()
    }
}
