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

import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.util.Pair
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import java.util.*


/**
 * Persian month names
 */
internal val MONTH_NAMES = arrayOf(
    "فروردین",
    "اردیبهشت",
    "خرداد",
    "تیر",
    "مرداد",
    "شهریور",
    "مهر",
    "آبان",
    "آذر",
    "دی",
    "بهمن",
    "اسفند"
)

/**
 * Persian days of week abbreviated
 */
private val ABBR_WEEK_DAY_NAMES = arrayOf(
    "ج",
    "پ",
    "چ",
    "س",
    "د",
    "ی",
    "ش"
)

/**
 * @param day of week
 * @return Abbreviated day name
 */
fun getAbbrDayName(day: Int): String {
    return ABBR_WEEK_DAY_NAMES[day]
}

/**
 * Formats date
 * @param timeInMillis time in millis representing the date
 * @return Formatted string: year/month/day
 * @see Calendar.getTimeInMillis
 */
fun getYearMonthDay(timeInMillis: Long): String {
    val calendar = PersianCalendar()
    calendar.timeInMillis = timeInMillis
    return "${calendar.day} ${MONTH_NAMES[calendar.month]}، ${calendar.year}"
}

/**
 * Formats date without year
 * @param timeInMillis time in millis representing the date
 * @return Formatted string: month/day
 * @see Calendar.getTimeInMillis
 */
fun getMonthDay(timeInMillis: Long): String {
    val calendar = PersianCalendar()
    calendar.timeInMillis = timeInMillis
    return "${calendar.day} ${MONTH_NAMES[calendar.month]}"
}

/**
 * Formats date
 * if selected date was in the same year as [todayCalendar] was
 * it wont show the year
 *
 * @param timeInMillis representing selected date
 * @param res [Resources] to retrieve strings from resource
 * @return formatted date
 */
fun getDateString(timeInMillis: Long, res: Resources): String {
    val currentCalendar = todayCalendar
    val calendarDate = iranCalendar
    calendarDate.timeInMillis = timeInMillis

    if (currentCalendar.year == calendarDate.year)
        return getMonthDay(timeInMillis)

    return if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        "${getMonthDay(timeInMillis)}\n${calendarDate.year}"
    else getYearMonthDay(timeInMillis)
}

/**
 * Formats date to show in [com.xdev.arch.persiancalendar.datepicker.MonthsPagerAdapter.getPageTitle]
 * @return formatted date
 */
fun formatYearMonth(calendar: PersianCalendar): String {
    return "${calendar.getMonthName()}، ${calendar.year}"
}

/**
 * Formats two dates for [com.xdev.arch.persiancalendar.datepicker.RangeDateSelector]
 * @return formatted string
 */
fun getDateRangeString(start: Long?, end: Long?, res: Resources): Pair<String, String> {
    if (start == null && end == null) return Pair.create(null, null)
    else if (start == null && end != null) return Pair.create(null, getDateString(end, res))
    else if (start != null && end == null) return Pair.create(getDateString(start, res), null)

    start as Long
    end as Long

    val currentCalendar = todayCalendar
    val startCalendar = iranCalendar
    startCalendar.timeInMillis = start
    val endCalendar = iranCalendar
    endCalendar.timeInMillis = end

    if (startCalendar.year == endCalendar.year) {
        return if (startCalendar.year == currentCalendar.year)
            Pair.create(getMonthDay(start), getMonthDay(end))
        else {
            if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                Pair.create(getMonthDay(start), "${getMonthDay(end)}\n${endCalendar.year}")
            else
                Pair.create(getMonthDay(start), getYearMonthDay(end))
        }
    }

    return if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        Pair.create(
            "${getMonthDay(start)}\n${startCalendar.year}",
            "${getMonthDay(end)}\n${endCalendar.year}"
        ) else
        Pair.create(getYearMonthDay(start), getYearMonthDay(end))
}