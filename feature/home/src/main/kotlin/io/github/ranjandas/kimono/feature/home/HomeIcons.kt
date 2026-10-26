package io.github.ranjandas.kimono.feature.home

import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.icon.appIcon
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp

internal val Workspace.icon: KimonoIcon
    get() = when (this) {
        Workspace.Home -> KimonoIcon.Home
        Workspace.Media -> KimonoIcon.Media
        Workspace.Games -> KimonoIcon.Games
        Workspace.Apps -> KimonoIcon.Apps
        Workspace.System -> KimonoIcon.System
    }

/** The app's brand icon, or one for what kind of app it is. */
internal val InstalledApp.icon: KimonoIcon
    get() = appIcon(
        packageName,
        fallback = when {
            !isTvApp -> KimonoIcon.PhoneApp
            category == AppCategory.Video -> KimonoIcon.Media
            category == AppCategory.Audio -> KimonoIcon.Music
            category == AppCategory.Game -> KimonoIcon.Games
            else -> KimonoIcon.AppFallback
        },
    )
