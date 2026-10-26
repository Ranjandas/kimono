package io.github.ranjandas.kimono.core.design.theme

/** How rounded every tile, button and focus border is. Independent of the colour theme (PLAN D9). */
enum class CornerStyle(val radiusDp: Int) {
    Square(0),
    Soft(6),
    Round(12),
    ;

    /**
     * Radius for an element inset [paddingDp] inside a shape with this style, so nested curves stay parallel.
     * Never below zero.
     */
    fun nestedRadiusDp(paddingDp: Int): Int = (radiusDp - paddingDp).coerceAtLeast(0)

    companion object {
        val Default = Square
    }
}
