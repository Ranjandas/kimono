package io.github.ranjandas.kimono.core.data.icons

/** How an app's tile shows it, chosen per app from its options (#116). */
enum class IconStyle(val label: String) {
    /** Its logo in the theme accent, or its monogram when the logo doesn't come out clean. */
    Logo("logo"),

    /** The logo from its TV banner, often with the name; its logo when it has no banner. */
    Wordmark("wordmark"),

    /** Two letters of its name. */
    Monogram("monogram"),
    ;

    /** The style after this one, wrapping round, for an option that steps through them. */
    val next: IconStyle get() = entries[(ordinal + 1) % entries.size]

    companion object {
        val Default = Logo

        /** The entry stored as [name], or [Default] when nothing is stored or the name no longer exists. */
        fun fromName(name: String?): IconStyle = entries.find { it.name == name } ?: Default
    }
}
