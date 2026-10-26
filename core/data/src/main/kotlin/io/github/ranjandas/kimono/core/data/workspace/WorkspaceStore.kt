package io.github.ranjandas.kimono.core.data.workspace

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Keeps the selected workspace and the per-workspace pins across restarts. */
interface WorkspaceStore {
    val state: Flow<WorkspaceState>

    suspend fun select(workspace: Workspace)

    suspend fun pin(workspace: Workspace, packageName: String)

    suspend fun unpin(workspace: Workspace, packageName: String)

    companion object {
        fun create(context: Context): WorkspaceStore = DataStoreWorkspaceStore(context.kimonoPreferences)
    }
}
