/*
 * Copyright 2020 Rahman Mohammadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xdev.arch.persiancalendar.datepicker.utils

import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import java.util.*
import java.util.Calendar.*

/**
 * utc timezone code
 */
private const val UTC = "UTC"

/**
 * utc timezone
 */
private val timeZone: TimeZone
    get() = TimeZone.getTimeZone(UTC)

/**
 * A [PersianCalendar] instance representing the day
 */
val todayCalendar: PersianCalendar
    get() {
        val calendar = PersianCalendar(getInstance(timeZone))
        calendar.timeZone = timeZone
        calendar.set(HOUR_OF_DAY, 0)
        calendar.set(MINUTE, 0)
        calendar.set(MILLISECOND, 0)
        calendar.set(SECOND, 0)
        return getDayCopy(calendar)
    }

/**
 * A [PersianCalendar] with no data
 */
val iranCalendar: PersianCalendar
    get() = getIranCalendar(null)

/**
 * Converts a [Calendar] to a [PersianCalendar] with [UTC] timezone
 * @return [PersianCalendar] with [UTC] timezone
 */
private fun getIranCalendar(rawCalendar: Calendar?): PersianCalendar {
    val utc = getInstance(timeZone)

    if (rawCalendar == null) utc.clear()
    else utc.timeInMillis = rawCalendar.timeInMillis

    val calendar = PersianCalendar(utc)
    calendar.timeZone = timeZone
    return calendar
}

/**
 * Clears all data from rawCalendar except date and
 * returns a [PersianCalendar] with same date as rawCalendar
 */
fun getDayCopy(rawCalendar: Calendar): PersianCalendar {
    val rawCalendarInUtc = getInstance(timeZone)
    rawCalendarInUtc.timeInMillis = rawCalendar.timeInMillis

    val utcCalendar = PersianCalendar(getInstance(timeZone).apply { clear() })
    utcCalendar.clear()
    utcCalendar.timeZone = timeZone

    utcCalendar.setAll(
        rawCalendarInUtc[YEAR],
        rawCalendarInUtc[MONTH],
        rawCalendarInUtc[DATE]
    )

    return utcCalendar
}

/**
 * Sets date to midnight 00:00:00:00
 * @param rawDate date in millis
 * @return midnight date in millis
 */
fun canonicalYearMonthDay(rawDate: Long): Long {
    val calendar = getInstance(timeZone)
    calendar.clear()
    calendar.timeInMillis = rawDate
    val copy = getDayCopy(calendar)
    return copy.timeInMillis
}