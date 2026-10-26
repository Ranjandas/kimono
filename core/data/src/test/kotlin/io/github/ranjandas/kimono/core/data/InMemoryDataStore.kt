package io.github.ranjandas.kimono.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Stands in for a Preferences DataStore file, which needs a device. */
class InMemoryDataStore : DataStore<Preferences> {
    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(preferences.value)
        preferences.value = updated
        return updated
    }
}
