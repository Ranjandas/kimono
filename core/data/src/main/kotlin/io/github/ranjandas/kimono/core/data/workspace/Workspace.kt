package io.github.ranjandas.kimono.core.data.workspace

/** The five workspaces in the top bar (decision O6). Each shows its pinned apps first, then its default set. */
enum class Workspace(val label: String) {
    Home("home"),
    Media("media"),
    Games("games"),
    Apps("apps"),
    System("system"),
    ;

    /** The number shown in the top bar and typed to jump here, from 1. */
    val number: Int get() = ordinal + 1

    /** The next workspace, wrapping from the last to the first. */
    fun next(): Workspace = entries[(ordinal + 1) % entries.size]

    /** The previous workspace, wrapping from the first to the last. */
    fun previous(): Workspace = entries[(ordinal + entries.size - 1) % entries.size]

    companion object {
        val Default = Home

        /** The workspace numbered [number], or null when there is none. */
        fun ofNumber(number: Int): Workspace? = entries.getOrNull(number - 1)
    }
}
