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

    /**
     * Moves [packageName] [by] places along [workspace]'s pins (negative is towards the front), stopping at either
     * end. An app that isn't pinned there changes nothing.
     */
    fun move(workspace: Workspace, packageName: String, by: Int): WorkspaceState {
        val current = pinsOf(workspace)
        val from = current.indexOf(packageName)
        if (from < 0) return this
        val to = (from + by).coerceIn(0, current.lastIndex)
        val moved = current.toMutableList().apply { add(to, removeAt(from)) }
        return copy(pins = pins + (workspace to moved))
    }

    /** Whether [packageName] is pinned to each workspace that takes pins, in workspace order. */
    fun pinChoices(packageName: String): List<Pair<Workspace, Boolean>> =
        Workspace.PinTargets.map { it to (packageName in pinsOf(it)) }
}
