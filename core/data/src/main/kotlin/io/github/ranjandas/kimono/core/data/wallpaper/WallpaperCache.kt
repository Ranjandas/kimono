package io.github.ranjandas.kimono.core.data.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Wallpapers aren't bundled (their artists' rights and trademarks aren't ours to ship), so each one is
 * downloaded the first time it's needed, cropped and scaled to the 1080p screen, and kept on the device. Every
 * theme's is kept (Kimono's drawn ones too), up to [KEPT_ON_DISK] images, and the last few shown stay in memory,
 * so switching theme doesn't wait for the network or a decode.
 */
class WallpaperCache(
    private val dir: File,
    private val download: (URL) -> ByteArray = ::httpGet,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val loaded = RecentlyUsed<String, Wallpaper>(KEPT_IN_MEMORY)

    // Loads under way, so a second ask for the same wallpaper waits for the first rather than starting over.
    // Their own scope, since a decode can't be stopped halfway anyway, and its result is kept for next time.
    private val loads = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val inFlight = mutableMapOf<String, Deferred<Wallpaper?>>()

    /** The wallpaper at [url] if it's in memory already, so it can be drawn without waiting a frame. */
    fun peek(url: String): Wallpaper? = loaded[url]

    /**
     * The wallpaper at [url], or null if it can't be downloaded or decoded. A [DrawnWallpaper] link is drawn
     * the first time instead, then kept like a download, since drawing one takes the Streamer about 2 s.
     */
    suspend fun load(url: String): Wallpaper? = loaded[url] ?: synchronized(inFlight) {
        inFlight.getOrPut(url) {
            loads.async {
                try {
                    read(url)?.also { loaded[url] = it }
                } finally {
                    synchronized(inFlight) { inFlight.remove(url) }
                }
            }
        }
    }.await()

    /**
     * Gets each of [urls] onto the device if it isn't yet, one at a time, so a later switch to it is quick:
     * downloaded, or drawn for a [DrawnWallpaper].
     */
    suspend fun prefetch(urls: Collection<String>) = withContext(ioDispatcher) {
        for (url in urls) {
            val file = fileFor(url)
            if (file.exists()) continue
            val drawn = DrawnWallpaper.fromUrl(url)
            val screen = if (drawn != null) draw(drawn, file) else fetch(url, file)
            screen?.recycle()
        }
    }

    private fun read(url: String): Wallpaper? {
        val file = fileFor(url)
        val drawn = DrawnWallpaper.fromUrl(url)
        val screen = cached(file) ?: if (drawn != null) draw(drawn, file) else fetch(url, file)
        return screen?.let { Wallpaper(it, blurred(it)) }
    }

    private fun fileFor(url: String) = File(dir, "${sha256(url)}.webp")

    private fun cached(file: File): Bitmap? = if (file.exists()) {
        // Marks it as used, so it's the last to be pruned.
        file.setLastModified(System.currentTimeMillis())
        BitmapFactory.decodeFile(file.path)
    } else {
        null
    }

    private fun fetch(url: String, file: File): Bitmap? {
        val bytes = try {
            download(URL(url))
        } catch (_: IOException) {
            null
        }
        return bytes?.let(::decodeForScreen)?.also { save(it, file, Bitmap.CompressFormat.WEBP_LOSSY) }
    }

    // Lossless, so the black between the lines stays exactly black (and those pixels off) on an OLED panel.
    private fun draw(wallpaper: DrawnWallpaper, file: File): Bitmap =
        drawForScreen(wallpaper).also { save(it, file, Bitmap.CompressFormat.WEBP_LOSSLESS) }

    private fun save(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat) {
        dir.mkdirs()
        // Written aside and moved into place, so a load running alongside never reads half a file.
        val partial = File.createTempFile("wallpaper", ".part", dir)
        partial.outputStream().use { bitmap.compress(format, WEBP_QUALITY, it) }
        partial.renameTo(file)
        prune()
    }

    private fun prune() {
        val files = dir.listFiles { file: File -> file.name.endsWith(".webp") }?.toList() ?: return
        oldestBeyond(files, KEPT_ON_DISK, File::lastModified).forEach(File::delete)
    }

    companion object {
        /** The Streamer's output; anything bigger is wasted memory. */
        const val SCREEN_WIDTH = 1920
        const val SCREEN_HEIGHT = 1080

        /** Every theme's, with room for a few of the user's own. About 300 KB each. */
        const val KEPT_ON_DISK = 16

        // About 8 MB each, at 1080p.
        private const val KEPT_IN_MEMORY = 3

        private const val WEBP_QUALITY = 90

        fun create(context: Context) = WallpaperCache(File(context.filesDir, "wallpapers"))
    }
}

private fun decodeForScreen(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val width = WallpaperCache.SCREEN_WIDTH
    val height = WallpaperCache.SCREEN_HEIGHT
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, width, height)
    }
    // A failed bounds decode leaves the sizes at -1 and the full decode then returns null too.
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    val crop = centerCrop(decoded.width, decoded.height, width, height)
    val cropped = Bitmap.createBitmap(decoded, crop.x, crop.y, crop.width, crop.height)
    return cropped.scale(width, height)
}

private fun drawForScreen(wallpaper: DrawnWallpaper): Bitmap {
    val width = WallpaperCache.SCREEN_WIDTH
    val height = WallpaperCache.SCREEN_HEIGHT
    return Bitmap.createBitmap(wallpaper.pixels(width, height), width, height, Bitmap.Config.ARGB_8888)
}

/**
 * A blurred copy at an eighth of the size, which is all a blur needs; panels draw it scaled back up. Made on
 * each load rather than cached, since it takes a few milliseconds.
 */
private fun blurred(screen: Bitmap): Bitmap {
    val small = screen.scale(screen.width / BLUR_DOWNSCALE, screen.height / BLUR_DOWNSCALE)
    val pixels = IntArray(small.width * small.height)
    small.getPixels(pixels, 0, small.width, 0, 0, small.width, small.height)
    val blurred = boxBlur(pixels, small.width, small.height, BLUR_RADIUS)
    return Bitmap.createBitmap(blurred, small.width, small.height, Bitmap.Config.ARGB_8888)
}

private fun httpGet(url: URL): ByteArray {
    val connection = url.openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        if (connection.responseCode != HttpURLConnection.HTTP_OK) throw IOException("HTTP ${connection.responseCode}")
        if (connection.contentLengthLong > MAX_BYTES) throw IOException("too large: ${connection.contentLengthLong}")
        return connection.inputStream.use { it.readBytes() }
    } finally {
        connection.disconnect()
    }
}

private fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

private const val TIMEOUT_MS = 15_000

// About a 35 px blur at full size, after three passes.
private const val BLUR_DOWNSCALE = 8
private const val BLUR_RADIUS = 4

// A 5K WebP is well under 1 MB; this only stops a wrong URL filling the disk.
private const val MAX_BYTES = 32L * 1024 * 1024
