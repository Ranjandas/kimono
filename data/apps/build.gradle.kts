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

    testOptions {
        unitTests.all { it.useJUnitPlatform() }
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
    api(project(":core:data"))
    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
