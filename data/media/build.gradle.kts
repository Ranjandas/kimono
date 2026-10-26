// The MediaSource interface and its providers (Trakt, TMDB, later Plex/Jellyfin), with a Room cache.

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.ranjandas.kimono.data.media"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        baseline = file("lint-baseline.xml")
        warningsAsErrors = true
        abortOnError = true
        // Version-freshness checks change with every upstream release, so they'd break CI on unrelated PRs.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)
}
