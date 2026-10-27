package io.github.ranjandas.kimono

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.ranjandas.kimono.core.data.idle.PixelShift
import kotlinx.coroutines.delay

/** Where the screen sits: moving one [PixelShift] step at a time while [active], back home otherwise. */
@Composable
internal fun rememberPixelShift(active: Boolean): DpOffset {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(active) {
        step = 0
        while (active) {
            delay(PixelShift.Interval)
            step++
        }
    }
    val (x, y) = PixelShift.offsetAt(step)
    return if (active) DpOffset(x.dp, y.dp) else DpOffset.Zero
}
