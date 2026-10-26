package io.github.ranjandas.kimono.core.data.workspace

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// One file for all of Kimono's settings; DataStore allows a single instance per file in a process.
internal val Context.kimonoPreferences: DataStore<Preferences> by preferencesDataStore(name = "kimono")
