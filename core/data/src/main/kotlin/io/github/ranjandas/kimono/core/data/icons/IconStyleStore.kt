package io.github.ranjandas.kimono.core.data.icons

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.ranjandas.kimono.core.data.kimonoPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Keeps each app's [IconStyle], by package name. Apps left on the default aren't stored. */
interface IconStyleStore {
    val styles: Flow<Map<String, IconStyle>>

    suspend fun set(packageName: String, style: IconStyle)

    companion object {
        fun create(context: Context): IconStyleStore = DataStoreIconStyleStore(context.kimonoPreferences)
    }
}

internal class DataStoreIconStyleStore(private val dataStore: DataStore<Preferences>) : IconStyleStore {
    override val styles: Flow<Map<String, IconStyle>> = dataStore.data
        .map { prefs ->
            prefs.asMap()
                .filterKeys { it.name.startsWith(PREFIX) }
                .map { (key, value) -> key.name.removePrefix(PREFIX) to IconStyle.fromName(value as? String) }
                .toMap()
        }
        .distinctUntilChanged()

    override suspend fun set(packageName: String, style: IconStyle) {
        val key = stringPreferencesKey(PREFIX + packageName)
        dataStore.edit { if (style == IconStyle.Default) it.remove(key) else it[key] = style.name }
    }

    private companion object {
        const val PREFIX = "icons.style."
    }
}
