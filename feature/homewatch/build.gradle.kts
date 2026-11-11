// Brings Kimono back when Google TV's own home comes to the front (route B in docs/adr/0001-home-takeover.md).
// An accessibility service, so it's only in builds that don't go to Play: debug, benchmark and the sideload APK
// (see :app). Play's accessibility policy is for apps that help people with disabilities, which this isn't.

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.ranjandas.kimono.feature.homewatch"
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
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
