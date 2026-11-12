package io.github.ranjandas.kimono.feature.home

import android.text.format.DateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The top bar's date in [locale]'s own order and punctuation (#206): "Thu, Nov 12" in the US, "Thu 12 Nov" in the UK,
 * "11月12日(木)" in Japan.
 */
internal fun topBarDateFormat(locale: Locale): DateTimeFormatter = localFormat(locale, "EEEdMMM")

/**
 * The top bar's time in [locale]'s own form, on a 12- or 24-hour clock as [twentyFourHour] says, since that's a
 * setting of its own: "2:35 PM" in the US, "午後2:35" in Japan.
 */
internal fun topBarTimeFormat(locale: Locale, twentyFourHour: Boolean): DateTimeFormatter =
    localFormat(locale, if (twentyFourHour) "Hm" else "hm")

private fun localFormat(locale: Locale, skeleton: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
