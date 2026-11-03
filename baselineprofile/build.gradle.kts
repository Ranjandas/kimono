// Generates the baseline profile that ships in the app, and measures what it buys on a connected device. Nothing
// here runs on CI, which has no device (docs/DEVELOPING.md).

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "io.github.ranjandas.kimono.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // The shared debug key (see :app).
        getByName("debug") {
            storeFile = rootProject.file("app/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"

    // Macrobenchmark drives the app from outside its process, so the tests instrument themselves.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    useConnectedDevices = true
}

// Each run takes only its own tests: a rule that's switched off skips its tests, and connected tests count those
// as failures.
androidComponents {
    onVariants { variant ->
        val test = if (variant.name.startsWith("nonMinified")) "BaselineProfileGenerator" else "StartupBenchmark"
        variant.instrumentationRunnerArguments.put("class", "io.github.ranjandas.kimono.baselineprofile.$test")
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
}
