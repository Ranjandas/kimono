plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.roborazzi) apply false
}

// Also in .editorconfig for IDEs; Spotless doesn't read ktlint settings from there.
val ktlintSettings =
    mapOf(
        "ktlint_code_style" to "android_studio",
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        "ktlint_standard_chain-method-continuation" to "disabled",
        "compose_allowed_composition_locals" to "LocalKimonoColors,LocalKimonoShapes,LocalKimonoTypography",
    )

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(ktlintSettings)
            .customRuleSets(listOf(libs.composeRules.ktlint.get().toString()))
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(ktlintSettings)
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        source.setFrom("src/main/kotlin", "src/test/kotlin", "src/androidTest/kotlin")
    }

    dependencies {
        "detektPlugins"(rootProject.libs.composeRules.detekt)
    }

    // The Kotlin plugin aligns every org.jetbrains.kotlin dependency to the project's Kotlin version,
    // but Detekt must run on the compiler it was built against.
    configurations.matching { it.name == "detekt" }.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(rootProject.libs.versions.detektKotlin.get())
            }
        }
    }
}
