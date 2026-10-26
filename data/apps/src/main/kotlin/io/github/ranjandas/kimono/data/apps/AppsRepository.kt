package io.github.ranjandas.kimono.data.apps

import android.content.Context
import java.text.Collator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** The installed apps Kimono can launch, kept up to date as apps are installed, removed and updated. */
class AppsRepository(private val source: AppSource, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
    private val byLabel = compareBy(collator) { app: InstalledApp -> app.label }.thenBy { it.packageName }

    /** Apps sorted by label, ignoring case and accents. Emits the current list first, then after each change. */
    val apps: Flow<List<InstalledApp>> = source.changes()
        .onStart { emit(Unit) }
        .conflate()
        .map { source.launchableApps().sortedWith(byLabel) }
        .flowOn(ioDispatcher)
        .distinctUntilChanged()

    companion object {
        fun create(context: Context): AppsRepository = AppsRepository(PackageManagerAppSource(context))
    }
}
