package io.github.ranjandas.kimono.core.data.workspace

/**
 * The four workspaces, each a full page of its own (#98). Home, watch and play list apps; all apps is the
 * drawer, last so it's one press past play. The top bar numbers them in this order.
 */
enum class Workspace(val label: String) {
    Home("home"),
    Watch("watch"),
    Play("play"),
    AllApps("all apps"),
    ;

    /** The number shown in the top bar and typed to jump here, from 1. */
    val number: Int get() = ordinal + 1

    /** Where pinning an app from this workspace puts it: all apps lists everything, so its pins go to home. */
    val pinTarget: Workspace get() = if (this == AllApps) Home else this

    /** The next workspace, wrapping from the last to the first. */
    fun next(): Workspace = entries[(ordinal + 1) % entries.size]

    /** The previous workspace, wrapping from the first to the last. */
    fun previous(): Workspace = entries[(ordinal + entries.size - 1) % entries.size]

    companion object {
        val Default = Home

        /** The workspaces an app can be pinned to: all apps lists everything, so it takes none. */
        val PinTargets: List<Workspace> = entries.filter { it != AllApps }

        /** The workspace numbered [number], or null when there is none. */
        fun ofNumber(number: Int): Workspace? = entries.getOrNull(number - 1)

        /**
         * The workspace a stored name means, including the five-workspace names from before #104: media is now
         * watch, games is play, and apps and system fold into home. Null for a name that was never a workspace.
         */
        fun ofStoredName(name: String?): Workspace? = entries.find { it.name == name } ?: LegacyNames[name]

        /** The names a workspace was stored under, its own first. */
        internal fun storedNames(workspace: Workspace): List<String> =
            listOf(workspace.name) + LegacyNames.filterValues { it == workspace }.keys

        private val LegacyNames = mapOf("Media" to Watch, "Games" to Play, "Apps" to Home, "System" to Home)
    }
}
