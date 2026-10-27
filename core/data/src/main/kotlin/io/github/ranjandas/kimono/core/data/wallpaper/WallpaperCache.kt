package io.github.ranjandas.kimono.core.data.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wallpapers aren't bundled (their artists' rights and trademarks aren't ours to ship), so each one is
 * downloaded the first time it's shown, cropped and scaled to the 1080p screen, and kept on the device. Only the
 * current wallpaper is kept.
 */
class WallpaperCache(
    private val dir: File,
    private val download: (URL) -> ByteArray = ::httpGet,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /** The wallpaper at [url], or null if it can't be downloaded or decoded. */
    suspend fun load(url: String): Bitmap? = withContext(ioDispatcher) {
        val file = File(dir, "${sha256(url)}.webp")
        cached(file) ?: fetch(url, file)
    }

    private fun cached(file: File): Bitmap? = if (file.exists()) BitmapFactory.decodeFile(file.path) else null

    private fun fetch(url: String, file: File): Bitmap? {
        val bytes = try {
            download(URL(url))
        } catch (_: IOException) {
            null
        }
        return bytes?.let(::decodeForScreen)?.also { bitmap ->
            dir.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, WEBP_QUALITY, it) }
            dir.listFiles()?.filter { it != file }?.forEach(File::delete)
        }
    }

    companion object {
        /** The Streamer's output; anything bigger is wasted memory. */
        const val SCREEN_WIDTH = 1920
        const val SCREEN_HEIGHT = 1080

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
    return Bitmap.createScaledBitmap(cropped, width, height, true)
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

// A 5K WebP is well under 1 MB; this only stops a wrong URL filling the disk.
private const val MAX_BYTES = 32L * 1024 * 1024
