package io.github.ranjandas.kimono.core.data.idle

import kotlin.time.Duration.Companion.minutes

/**
 * OLED care while idle: the screen walks slowly round a small square, one step every [Interval], so the static
 * parts (borders, the top bar, text) don't sit on the same pixels for hours.
 */
object PixelShift {
    val Interval = 1.minutes

    /** How far the screen moves from its home position, in dp. */
    const val MAX_DP = 3

    // Round the edge of the square and back to the middle; the middle is where it starts and wakes up.
    private val Steps = listOf(0 to 0, 1 to 0, 1 to 1, 0 to 1, -1 to 1, -1 to 0, -1 to -1, 0 to -1, 1 to -1)

    /** The offset in dp, x to y, for the [step]th move since the screen went idle. */
    fun offsetAt(step: Int): Pair<Int, Int> {
        val (x, y) = Steps[step.mod(Steps.size)]
        return x * MAX_DP to y * MAX_DP
    }
}
