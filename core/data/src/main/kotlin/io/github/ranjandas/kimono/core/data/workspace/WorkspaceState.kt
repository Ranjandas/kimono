package io.github.ranjandas.kimono.core.data.workspace

/** The selected workspace and the apps pinned to each workspace, as package names in pin order. */
data class WorkspaceState(
    val current: Workspace = Workspace.Default,
    val pins: Map<Workspace, List<String>> = emptyMap(),
) {
    fun pinsOf(workspace: Workspace): List<String> = pins[workspace].orEmpty()

    /** Adds [packageName] to the end of [workspace]'s pins. Pinning an app that is already pinned changes nothing. */
    fun pin(workspace: Workspace, packageName: String): WorkspaceState {
        val current = pinsOf(workspace)
        if (packageName in current) return this
        return copy(pins = pins + (workspace to current + packageName))
    }

    fun unpin(workspace: Workspace, packageName: String): WorkspaceState =
        copy(pins = pins + (workspace to pinsOf(workspace) - packageName))
}
