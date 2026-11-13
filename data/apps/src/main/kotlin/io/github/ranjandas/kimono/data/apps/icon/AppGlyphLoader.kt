package io.github.ranjandas.kimono.data.apps.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import kotlin.math.max
import kotlin.math.roundToInt

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
        val icon = sharpest(activity, activity.iconResource)
            ?: runCatching { activity.loadIcon(packageManager) }.getOrNull()
        val banner = sharpest(activity, activity.bannerResource())
            ?: runCatching { activity.loadBanner(packageManager) }.getOrNull()
        return AppGlyphs(
            icon = icon?.toBitmap(ICON_PX, ICON_PX),
            monochrome = icon?.monochromeLayer()?.let {
                glyphOf(it.toBitmap(ADAPTIVE_PX, ADAPTIVE_PX), plate = false, cutWidth = ADAPTIVE_CUT)
            },
            silhouette = icon?.let(::silhouette),
            wordmark = banner?.let {
                glyphOf(it.toBitmap(BANNER_WIDTH_PX, BANNER_HEIGHT_PX), plate = true, cutWidth = GlyphMask.CUT_WIDTH)
            },
        )
    }

    /** [load] for an enabled app's activity, by name; null when it can't be found, such as just after an uninstall. */
    fun load(packageName: String, activityName: String): AppGlyphs? =
        runCatching { activityInfo(ComponentName(packageName, activityName)) }.getOrNull()?.let(::load)

    /**
     * [resource] from [activity]'s app at the highest density it ships (#232). Loading at the TV's own density hands
     * back its xhdpi images, about 96 px for a plain icon, which the tiles then stretch two to three times and blur.
     * The whole lookup runs at that density, not only the top file: an adaptive icon's layers are often further
     * files (10 Play's foreground is an inset around a PNG), and those would otherwise still come at xhdpi (#234).
     */
    private fun sharpest(activity: ActivityInfo, resource: Int): Drawable? = resource.takeIf { it != 0 }?.let {
        runCatching {
            atTopDensity(packageManager.getResourcesForApplication(activity.applicationInfo)).getDrawable(it, null)
        }.getOrNull()
    }

    // A fresh Resources over the same files, so the shared one the rest of Kimono uses keeps the TV's density.
    @Suppress("DEPRECATION")
    private fun atTopDensity(resources: Resources): Resources {
        val metrics = DisplayMetrics().apply {
            setTo(resources.displayMetrics)
            densityDpi = DisplayMetrics.DENSITY_XXXHIGH
            density = DisplayMetrics.DENSITY_XXXHIGH.toFloat() / DisplayMetrics.DENSITY_DEFAULT
        }
        val configuration = Configuration(resources.configuration).apply { densityDpi = DisplayMetrics.DENSITY_XXXHIGH }
        return Resources(resources.assets, metrics, configuration)
    }

    private fun ActivityInfo.bannerResource(): Int = if (banner != 0) banner else applicationInfo.banner

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
        return foreground?.let { glyphOf(it.toBitmap(ADAPTIVE_PX, ADAPTIVE_PX), plate = true, cutWidth = ADAPTIVE_CUT) }
            ?: glyphOf(icon.toBitmap(ICON_PX, ICON_PX), plate = true)
    }

    private fun Drawable.monochromeLayer(): Drawable? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) (this as? AdaptiveIconDrawable)?.monochrome else null

    // The colour gaps were tuned on a 192 px icon and a 288 px adaptive layer, so they widen with the bigger
    // drawings (#232) to keep their look; banners are drawn at the size they always were.
    private fun glyphOf(source: Bitmap, plate: Boolean, cutWidth: Int = GlyphMask.CUT_WIDTH * ICON_SCALE): Glyph? {
        val width = source.width
        val pixels = IntArray(width * source.height)
        source.getPixels(pixels, 0, width, 0, 0, width, source.height)
        val plateColour = if (plate) GlyphMask.plateColour(pixels) else null
        val alpha = GlyphMask.extract(pixels, width, plateColour, cutWidth)
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
            bitmap = Bitmap.createBitmap(out, box.width, box.height, Bitmap.Config.ARGB_8888).capped(),
            coverage = ink.toFloat() / (MAX_ALPHA * out.size),
            removedPlate = plateColour != null,
        )
    }

    // A glyph bigger than the largest mark on screen only costs memory, so a large one is brought down to it.
    private fun Bitmap.capped(): Bitmap {
        val longest = max(width, height)
        if (longest <= MAX_GLYPH_PX) return this
        val ratio = MAX_GLYPH_PX.toFloat() / longest
        return scale((width * ratio).roundToInt().coerceAtLeast(1), (height * ratio).roundToInt().coerceAtLeast(1))
    }

    companion object {
        fun create(context: Context) = AppGlyphLoader(context.applicationContext.packageManager)

        // A plain icon is 192 px at xxxhdpi. Drawn at twice that, its trimmed logo still covers home's last used
        // mark (about 330 px across on a 1080p TV) (#232).
        private const val ICON_SCALE = 2
        private const val ICON_PX = 192 * ICON_SCALE

        // An adaptive layer is 108 dp, 432 px at xxxhdpi: drawn at its own size, its images aren't stretched.
        private const val ADAPTIVE_PX = 432
        private const val ADAPTIVE_CUT = GlyphMask.CUT_WIDTH * ADAPTIVE_PX / 288
        private const val MAX_GLYPH_PX = 384
        private const val BANNER_WIDTH_PX = 640
        private const val BANNER_HEIGHT_PX = 360
        private const val MIN_GLYPH_PIXELS = 64
        private const val ALPHA_SHIFT = 24
        private const val WHITE = 0xFFFFFF
        private const val MAX_ALPHA = 255f
    }
}
