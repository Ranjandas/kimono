package io.github.ranjandas.kimono.data.apps.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

/** A glyph ready to tint: white, with the shape in its alpha, trimmed to the shape's bounds. */
class Glyph(
    val bitmap: Bitmap,
    /** How much of its own box the shape fills, 0 to 1. A solid blob is near 1 and won't read as a logo. */
    val coverage: Float,
    /** True when a background plate colour was taken out, rather than using the image's transparency. */
    val removedPlate: Boolean,
)

/** One app's icon sources: the colour icon as shipped, and the single-colour glyphs made from it. */
class AppGlyphs(
    val icon: Bitmap?,
    /** The app's own themed-icon layer (Android 13+), when it ships one. */
    val monochrome: Glyph?,
    /** Cut from the square icon. */
    val silhouette: Glyph?,
    /** Cut from the TV banner. */
    val wordmark: Glyph?,
)

/** Makes [AppGlyphs] from what an app ships. Slow enough to keep off the main thread. */
class AppGlyphLoader private constructor(private val packageManager: PackageManager) {
    /**
     * Reads [activity]'s own icon and banner, falling back to the app's. Taking the [ActivityInfo] from a query
     * works for disabled apps too, where looking the component up again would fail.
     */
    fun load(activity: ActivityInfo): AppGlyphs {
        val icon = runCatching { activity.loadIcon(packageManager) }.getOrNull()
        val banner = runCatching { activity.loadBanner(packageManager) }.getOrNull()
        return AppGlyphs(
            icon = icon?.toBitmap(ICON_PX, ICON_PX),
            monochrome = icon?.monochromeLayer()?.let { glyphOf(it.toBitmap(ADAPTIVE_PX, ADAPTIVE_PX), plate = false) },
            silhouette = icon?.let(::silhouette),
            wordmark = banner?.let { glyphOf(it.toBitmap(BANNER_WIDTH_PX, BANNER_HEIGHT_PX), plate = true) },
        )
    }

    /** [load] for an enabled app's activity, by name; null when it can't be found, such as just after an uninstall. */
    fun load(packageName: String, activityName: String): AppGlyphs? =
        runCatching { activityInfo(ComponentName(packageName, activityName)) }.getOrNull()?.let(::load)

    private fun activityInfo(component: ComponentName): ActivityInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getActivityInfo(component, PackageManager.ComponentInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getActivityInfo(component, 0)
        }

    // An adaptive icon's foreground usually holds the logo alone. Some apps draw the logo into the background
    // layer and leave the foreground empty, so fall back to the whole icon.
    private fun silhouette(icon: Drawable): Glyph? {
        val foreground = (icon as? AdaptiveIconDrawable)?.foreground
        return foreground?.let { glyphOf(it.toBitmap(ADAPTIVE_PX, ADAPTIVE_PX), plate = true) }
            ?: glyphOf(icon.toBitmap(ICON_PX, ICON_PX), plate = true)
    }

    private fun Drawable.monochromeLayer(): Drawable? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) (this as? AdaptiveIconDrawable)?.monochrome else null

    private fun glyphOf(source: Bitmap, plate: Boolean): Glyph? {
        val width = source.width
        val pixels = IntArray(width * source.height)
        source.getPixels(pixels, 0, width, 0, 0, width, source.height)
        val plateColour = if (plate) GlyphMask.plateColour(pixels) else null
        val alpha = GlyphMask.extract(pixels, width, plateColour)
        val box = GlyphMask.bounds(alpha, width)?.takeIf { it.width * it.height >= MIN_GLYPH_PIXELS }
            ?: return null
        val out = IntArray(box.width * box.height)
        var ink = 0L
        for (y in 0 until box.height) {
            for (x in 0 until box.width) {
                val a = alpha[(box.top + y) * width + box.left + x]
                ink += a
                out[y * box.width + x] = (a shl ALPHA_SHIFT) or WHITE
            }
        }
        return Glyph(
            bitmap = Bitmap.createBitmap(out, box.width, box.height, Bitmap.Config.ARGB_8888),
            coverage = ink.toFloat() / (MAX_ALPHA * out.size),
            removedPlate = plateColour != null,
        )
    }

    companion object {
        fun create(context: Context) = AppGlyphLoader(context.applicationContext.packageManager)

        private const val ICON_PX = 192
        private const val ADAPTIVE_PX = 288
        private const val BANNER_WIDTH_PX = 640
        private const val BANNER_HEIGHT_PX = 360
        private const val MIN_GLYPH_PIXELS = 64
        private const val ALPHA_SHIFT = 24
        private const val WHITE = 0xFFFFFF
        private const val MAX_ALPHA = 255f
    }
}
