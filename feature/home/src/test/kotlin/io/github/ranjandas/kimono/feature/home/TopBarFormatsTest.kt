package io.github.ranjandas.kimono.feature.home

import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The top bar's date and time follow the TV's locale (#206). Runs on Robolectric for Android's ICU patterns. */
@RunWith(RobolectricTestRunner::class)
class TopBarFormatsTest {
    @Test
    fun `date is in the locale's own order`() {
        assertEquals("Thu, Nov 12", date(Locale.US))
        assertEquals("Thu 12 Nov", date(Locale.UK))
        assertEquals("11月12日(木)", date(Locale.JAPAN))
    }

    @Test
    fun `time follows the clock setting, in the locale's own form`() {
        assertEquals("14:35", time(Locale.US, twentyFourHour = true))
        assertEquals("2:35 PM", time(Locale.US, twentyFourHour = false))
        assertEquals("14:35", time(Locale.JAPAN, twentyFourHour = true))
        assertEquals("午後2:35", time(Locale.JAPAN, twentyFourHour = false))
    }

    private fun date(locale: Locale) = Afternoon.format(topBarDateFormat(locale))

    // Compared without the narrow no-break space some ICU versions put before "PM".
    private fun time(locale: Locale, twentyFourHour: Boolean) =
        Afternoon.format(topBarTimeFormat(locale, twentyFourHour)).replace(' ', ' ')

    private companion object {
        val Afternoon: LocalDateTime = LocalDateTime.of(2026, 11, 12, 14, 35)
    }
}
