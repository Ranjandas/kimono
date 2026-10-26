// Installed apps, launch intents, package-change updates and usage stats.

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.ranjandas.kimono.data.apps"
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
