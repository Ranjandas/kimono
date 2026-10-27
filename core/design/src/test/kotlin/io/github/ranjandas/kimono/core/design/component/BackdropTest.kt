package io.github.ranjandas.kimono.core.design.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BackdropTest {
    private val blurred = IntSize(240, 135)

    @Test
    fun `a panel on a 16-9 window takes the matching eighth of the blurred image`() {
        val crop = backdropCrop(blurred, Size(1920f, 1080f), Offset(80f, 160f), Size(800f, 400f))
        assertEquals(IntOffset(10, 20) to IntSize(100, 50), crop)
    }

    @Test
    fun `a wider window crops the image top and bottom, as the wallpaper does`() {
        // 2400 x 1080 scales the 240 x 135 image by 10, leaving 1350 tall: 135 px off the top.
        val crop = backdropCrop(blurred, Size(2400f, 1080f), Offset(0f, 0f), Size(2400f, 1080f))
        assertEquals(IntOffset(0, 14) to IntSize(240, 108), crop)
    }

    @Test
    fun `a panel past the edge is clamped to the image`() {
        val crop = backdropCrop(blurred, Size(1920f, 1080f), Offset(1900f, 1070f), Size(400f, 400f))
        assertEquals(IntOffset(238, 134) to IntSize(2, 1), crop)
    }
}
