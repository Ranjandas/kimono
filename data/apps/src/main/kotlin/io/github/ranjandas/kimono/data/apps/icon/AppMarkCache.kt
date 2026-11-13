package io.github.ranjandas.kimono.data.apps.icon

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import io.github.ranjandas.kimono.data.apps.InstalledApp
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** An app's tintable marks: its logo and its banner wordmark, each null when missing or not clean enough to use. */
class AppMarkBitmaps(val logo: Bitmap?, val wordmark: Bitmap?)

/**
 * Makes each app's [AppMarkBitmaps] once per app version and keeps them as PNGs in Kimono's cache folder, so later
 * launches only read them back (#116). An update to an app makes its marks again, since its logo may have changed.
 */
class AppMarkCache internal constructor(
    private val packageManager: PackageManager,
    private val loader: AppGlyphLoader,
    private val dir: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    // Package name to the version stamp and marks, so a new app list doesn't read every file again.
    private val memory = ConcurrentHashMap<String, Pair<String, AppMarkBitmaps>>()

    /** Marks for [apps] by package name: the ones already made at once, then again as each missing one is made. */
    fun marks(apps: List<InstalledApp>): Flow<Map<String, AppMarkBitmaps>> = flow {
        val marks = HashMap<String, AppMarkBitmaps>()
        val missing = ArrayList<Pair<InstalledApp, String>>()
        for (app in apps) {
            val stamp = stamp(app.packageName) ?: continue
            val known = remembered(app.packageName, stamp) ?: read(app.packageName, stamp)
            if (known == null) missing += app to stamp else marks[app.packageName] = known
        }
        emit(marks.toMap())
        for ((app, stamp) in missing) {
            val made = make(app) ?: continue
            write(app.packageName, stamp, made)
            memory[app.packageName] = stamp to made
            marks[app.packageName] = made
            emit(marks.toMap())
        }
    }.flowOn(dispatcher)

    private fun remembered(packageName: String, stamp: String): AppMarkBitmaps? =
        memory[packageName]?.takeIf { it.first == stamp }?.second

    private fun stamp(packageName: String): String? = runCatching { packageInfo(packageName).lastUpdateTime }
        .getOrNull()
        ?.let { "$it-$PIPELINE_VERSION" }

    private fun packageInfo(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }

    private fun make(app: InstalledApp): AppMarkBitmaps? {
        val glyphs = loader.load(app.packageName, app.activityName) ?: return null
        return AppMarkBitmaps(
            logo = (glyphs.monochrome ?: glyphs.silhouette).usable()?.mipmapped(),
            wordmark = glyphs.wordmark.usable()?.mipmapped(),
        )
    }

    private fun read(packageName: String, stamp: String): AppMarkBitmaps? {
        if (!file(packageName, stamp, DONE).exists()) return null
        val marks = AppMarkBitmaps(
            logo = decode(file(packageName, stamp, LOGO)),
            wordmark = decode(file(packageName, stamp, WORDMARK)),
        )
        memory[packageName] = stamp to marks
        return marks
    }

    // The done marker goes last, so a write cut short is made again next time rather than read half-finished.
    private fun write(packageName: String, stamp: String, marks: AppMarkBitmaps) {
        runCatching {
            dir.mkdirs()
            dir.listFiles { file -> file.name.startsWith("$packageName@") }?.forEach { it.delete() }
            marks.logo?.let { encode(it, file(packageName, stamp, LOGO)) }
            marks.wordmark?.let { encode(it, file(packageName, stamp, WORDMARK)) }
            file(packageName, stamp, DONE).createNewFile()
        }
    }

    private fun file(packageName: String, stamp: String, kind: String) = File(dir, "$packageName@$stamp.$kind")

    private fun decode(file: File): Bitmap? =
        if (file.exists()) BitmapFactory.decodeFile(file.path)?.mipmapped() else null

    private fun encode(bitmap: Bitmap, file: File) {
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
    }

    companion object {
        fun create(context: Context) = AppMarkCache(
            packageManager = context.packageManager,
            loader = AppGlyphLoader.create(context),
            dir = File(context.cacheDir, "marks"),
        )

        // Bump when the way marks are made changes, so every app's are made again.
        private const val PIPELINE_VERSION = 3
        private const val LOGO = "logo.png"
        private const val WORDMARK = "wordmark.png"
        private const val DONE = "done"
        private const val PNG_QUALITY = 100
    }
}

// A shape that fills nearly all of its box is a plate the logo couldn't be cut from, not a logo.
internal fun Glyph?.usable(): Bitmap? = this?.takeIf { it.coverage <= MAX_COVERAGE }?.bitmap

private const val MAX_COVERAGE = 0.95f

// Marks are drawn smaller than they're made on most tiles, and a big bitmap shrunk without mipmaps shimmers and
// looks rough, more so while focus grows the tile (#232).
private fun Bitmap.mipmapped(): Bitmap = apply { setHasMipMap(true) }
