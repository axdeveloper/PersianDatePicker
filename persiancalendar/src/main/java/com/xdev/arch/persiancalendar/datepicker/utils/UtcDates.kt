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
 * Iran's timezone code
 */
private const val IRST = "IRST"

/**
 * Iran's timezone
 */
private val timeZone: TimeZone
    get() = TimeZone.getTimeZone(IRST)

/**
 * A [PersianCalendar] instance representing the day
 */
val todayCalendar: PersianCalendar
    get() {
        val calendar = getInstance(timeZone)
        return PersianCalendar(calendar)
    }

/**
 * A [PersianCalendar] with no data
 */
val iranCalendar: PersianCalendar
    get() = getIranCalendar(null)

/**
 * Converts a [Calendar] to a [PersianCalendar] with [IRST] timezone
 * @return [PersianCalendar] with [IRST] timezone
 */
private fun getIranCalendar(rawCalendar: Calendar?): PersianCalendar {
    val utc = getInstance(timeZone)

    if (rawCalendar == null) utc.clear()
    else utc.timeInMillis = rawCalendar.timeInMillis

    return PersianCalendar(utc)
}

/**
 * Clears all data from rawCalendar except date and
 * returns a [PersianCalendar] with same date as rawCalendar
 */
fun getDayCopy(rawCalendar: Calendar): PersianCalendar {
    val raw = getIranCalendar(rawCalendar)
    val irCalendar = iranCalendar

    irCalendar.set(raw.get(YEAR), raw.get(MONTH), raw.get(DAY_OF_MONTH))
    return irCalendar
}

/**
 * Sets date to midnight 00:00:00:00
 * @param rawDate date in millis
 * @return midnight date in millis
 */
fun canonicalYearMonthDay(rawDate: Long): Long {
    val calendar = PersianCalendar()
    calendar.timeInMillis = rawDate
    calendar.timeZone = TimeZone.getTimeZone(IRST)
    calendar.set(HOUR_OF_DAY, 0)
    calendar.set(MINUTE, 0)
    calendar.set(SECOND, 0)
    calendar.set(MILLISECOND, 0)
    return calendar.timeInMillis
}