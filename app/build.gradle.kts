plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.ranjandas.kimono"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.ranjandas.kimono"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        // A tagged release passes its own (see .github/workflows/release.yml).
        versionCode = providers.gradleProperty("kimono.versionCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("kimono.versionName").orNull ?: "0.1.0"
    }

    signingConfigs {
        // Shared debug key, committed on purpose: debug APKs from CI and from any machine share a
        // signature, so `adb install -r` updates in place and keeps settings. Never used for release.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    // The release key lives outside the repo: CI decodes it from secrets for a tagged release, and anyone can
    // build a signed release locally by setting the same variables (docs/DEVELOPING.md). Without them, release
    // builds come out unsigned.
    val releaseKeystore = providers.environmentVariable("KIMONO_RELEASE_KEYSTORE").orNull
    if (releaseKeystore != null) {
        signingConfigs.create("release") {
            storeFile = file(releaseKeystore)
            storePassword = providers.environmentVariable("KIMONO_RELEASE_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("KIMONO_RELEASE_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("KIMONO_RELEASE_KEY_PASSWORD").get()
        }
    }

    buildTypes {
        // Optimised and shrunk like the benchmark build that's measured on the TV, so what ships is what was timed.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
        // A release build for measuring speed on the TV: optimised and not debuggable, but signed with the
        // debug key so it installs over a debug build in place and keeps settings (docs/DEVELOPING.md).
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // The accessibility test renders real screens, the theme fonts among them.
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
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
    implementation(project(":core:design"))
    implementation(project(":core:data"))
    implementation(project(":feature:home"))
    implementation(project(":feature:drawer"))
    implementation(project(":feature:settings"))
    implementation(project(":data:apps"))
    implementation(project(":data:media"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    // Robolectric runs on JUnit 4, so its tests run through the vintage engine.
    testRuntimeOnly(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
