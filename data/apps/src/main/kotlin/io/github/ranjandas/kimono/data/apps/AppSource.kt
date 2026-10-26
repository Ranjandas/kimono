package io.github.ranjandas.kimono.data.apps

import kotlinx.coroutines.flow.Flow

/** The platform side of [AppsRepository], so the repository can be tested with a fake. */
interface AppSource {
    /** Reads the launchable apps now. Blocking; called off the main thread. */
    fun launchableApps(): List<InstalledApp>

    /** Emits whenever an app is installed, removed or updated. */
    fun changes(): Flow<Unit>
}
