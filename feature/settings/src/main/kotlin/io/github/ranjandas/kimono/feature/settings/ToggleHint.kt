package io.github.ranjandas.kimono.feature.settings

import android.provider.Settings
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

/**
 * A sketch of the system screen that comes next, beside the disclosure: a list under a Services heading, with the
 * "Kimono as home" row picked out and its switch sliding on, every few seconds. It's drawn in plain greys rather
 * than copied from Google's screen, which differs between TVs, so it reads as "the next screen" and never goes
 * stale. With animations turned off in the system it shows the switch on, still. The text says the same thing, so
 * the sketch is decoration to TalkBack.
 */
@Composable
internal fun ToggleHint(modifier: Modifier = Modifier) {
    val still = rememberAnimationsOff()
    val on = if (still) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "toggle hint")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = CYCLE_MILLIS
                    0f at 0
                    0f at OFF_UNTIL_MILLIS
                    1f at ON_FROM_MILLIS
                    1f at CYCLE_MILLIS
                },
            ),
            label = "switch",
        ).value
    }
    Column(
        modifier = modifier
            .width(HintWidth)
            .clearAndSetSemantics { }
            .background(ScreenColor, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PlaceholderRow(widthFraction = 0.7f)
        PlaceholderRow(widthFraction = 0.5f)
        Text("Services", fontSize = 9.sp, color = LabelColor, modifier = Modifier.padding(top = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RowColor, RoundedCornerShape(4.dp))
                .border(1.dp, lerp(RowColor, OnColor, on), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Kimono as home", fontSize = 11.sp, color = TextColor, modifier = Modifier.weight(1f))
            Switch(on)
        }
        PlaceholderRow(widthFraction = 0.6f)
    }
}

/** A switch in the sketch, [on] from 0 (off) to 1 (on), between them as it slides. */
@Composable
private fun Switch(on: Float) {
    Box(
        modifier = Modifier
            .size(width = TrackWidth, height = TrackHeight)
            .background(lerp(TrackOff, OnColor, on), CircleShape),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = 2.dp + (TrackWidth - ThumbSize - 4.dp) * on)
                .size(ThumbSize)
                .background(ThumbColor, CircleShape),
        )
    }
}

/** A line standing for a row of the system list that doesn't matter here. */
@Composable
private fun PlaceholderRow(widthFraction: Float) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(6.dp)
            .background(PlaceholderColor, CircleShape),
    )
}

/** Whether the system has animations turned off (Developer options or accessibility), read once. */
@Composable
private fun rememberAnimationsOff(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

private const val CYCLE_MILLIS = 3_000
private const val OFF_UNTIL_MILLIS = 1_100
private const val ON_FROM_MILLIS = 1_500

private val HintWidth = 190.dp
private val TrackWidth = 26.dp
private val TrackHeight = 14.dp
private val ThumbSize = 10.dp

// Plain greys, so the sketch stands for "the system screen" in every Kimono theme.
private val ScreenColor = Color(0xFF26282C)
private val RowColor = Color(0xFF34373C)
private val PlaceholderColor = Color(0xFF3E4146)
private val LabelColor = Color(0xFF9AA0A6)
private val TextColor = Color(0xFFE8EAED)
private val TrackOff = Color(0xFF5F6368)
private val ThumbColor = Color(0xFFF1F3F4)
private val OnColor = Color(0xFF8AB4F8)
