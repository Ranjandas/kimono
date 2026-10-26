// Theme model, palettes, typography and base components (Tile, Kbd, TopBar).

import javax.inject.Inject
import org.gradle.process.ExecOperations

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Subsets the bundled Nerd Font to the text glyphs plus the icons Kimono uses (PLAN D8), so the APK
 * carries a few hundred KB instead of 2.5 MB per weight. Icons are the private-use code points
 * written as `0x…` literals in [glyphSources]. Needs `pyftsubset` from fonttools on the PATH, or its
 * path in the `kimono.pyftsubset` Gradle property.
 */
abstract class SubsetFontsTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDir: DirectoryProperty

    /** Source TTF file names in [sourceDir], mapped to the font resource name each one becomes. */
    @get:Input
    abstract val fonts: MapProperty<String, String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val glyphSources: ConfigurableFileCollection

    @get:Input
    abstract val pyftsubset: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun subset() {
        val icons = glyphSources.files
            .flatMap { ICON_LITERAL.findAll(it.readText()).map { match -> match.groupValues[1].toInt(16) } }
            .filter { it.isPrivateUse() }
            .toSortedSet()
        val unicodes = (TEXT_RANGES + icons.map { "U+%X".format(it) }).joinToString(",")

        val fontDir = outputDir.get().dir("font").asFile
        fontDir.deleteRecursively()
        fontDir.mkdirs()
        fonts.get().forEach { (source, resourceName) ->
            runCatching {
                execOperations.exec {
                    commandLine(
                        pyftsubset.get(),
                        sourceDir.file(source).get().asFile.path,
                        "--unicodes=$unicodes",
                        "--layout-features=*",
                        "--name-IDs=*",
                        "--no-hinting",
                        "--drop-tables+=PfEd",
                        "--output-file=${fontDir.resolve("$resourceName.ttf")}",
                    )
                }
            }.onFailure {
                throw GradleException(
                    "Font subsetting failed. Install fonttools (see docs/DEVELOPING.md) or set kimono.pyftsubset.",
                    it,
                )
            }
        }
        logger.info("Subset ${fonts.get().size} fonts with ${icons.size} icons")
    }

    private fun Int.isPrivateUse() = this in 0xE000..0xF8FF || this >= 0xF0000

    private companion object {
        val ICON_LITERAL = Regex("""\b0x([0-9A-Fa-f]{4,6})\b""")

        // Everything outside the private-use areas, i.e. all of JetBrains Mono's own glyphs.
        val TEXT_RANGES = listOf("U+0000-DFFF", "U+F900-EFFFF")
    }
}

androidComponents {
    onVariants { variant ->
        val subsetFonts = tasks.register<SubsetFontsTask>(
            "subset${variant.name.replaceFirstChar(Char::uppercase)}Fonts",
        ) {
            sourceDir.set(layout.projectDirectory.dir("fonts"))
            fonts.set(
                mapOf(
                    "JetBrainsMonoNerdFont-Regular.ttf" to "kimono_mono_regular",
                    "JetBrainsMonoNerdFont-Bold.ttf" to "kimono_mono_bold",
                ),
            )
            glyphSources.from("src/main/kotlin/io/github/ranjandas/kimono/core/design/icon/KimonoIcon.kt")
            pyftsubset.set(providers.gradleProperty("kimono.pyftsubset").orElse("pyftsubset"))
        }
        // AGP sets outputDir to a per-variant generated res folder.
        variant.sources.res?.addGeneratedSourceDirectory(subsetFonts, SubsetFontsTask::outputDir)
    }
}

android {
    namespace = "io.github.ranjandas.kimono.core.design"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            // KimonoIconTest checks every icon against the source fonts.
            it.inputs.dir("fonts").withPathSensitivity(PathSensitivity.RELATIVE)
            it.systemProperty("kimono.fontDir", file("fonts").path)
            it.systemProperty("java.awt.headless", "true")
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
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
