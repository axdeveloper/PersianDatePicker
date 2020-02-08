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

package com.xdev.arch.persiancalendar.datepicker.calendar

import com.xdev.arch.persiancalendar.datepicker.utils.MONTH_NAMES
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar
import java.util.*
import kotlin.math.floor

/**
 * Persian calendar class to handle dates the way we need
 */
class PersianCalendar() : GregorianCalendar() {

    var year = 0
    var month = 0
    var day = 0

    constructor(timeInMillis: Long) : this() {
        this.timeInMillis = timeInMillis
    }

    constructor(calendar: Calendar) : this(calendar.timeInMillis)

    /**
     * First day of week is [Calendar.SATURDAY] in Persian Calendar
     */
    override fun getFirstDayOfWeek(): Int = Calendar.SATURDAY

    override fun setTimeInMillis(millis: Long) {
        super.setTimeInMillis(millis)
        internalCalculate()
    }

    override fun set(field: Int, value: Int) {
        super.set(field, value)
        internalCalculate()
    }

    override fun add(field: Int, a: Int) {
        var amount = a
        if (field == Calendar.MONTH) {
            amount += month
            year += amount / 12
            month = amount % 12

            if (day > PERSIAN_DAYS_IN_MONTH[amount % 12]) {
                day = PERSIAN_DAYS_IN_MONTH[amount % 12]
                if (month == 11 && isLeapYear(year))
                    day = 30
            }

            setPersian(year, month, day)
        }
    }

    override fun toString(): String {
        return "$year/$month/$day"
    }


    /**
     * @return first day of Persian month in this calendar
     */
    fun getFirstDayOfMonth(): Int {
        setDayOfMonth(1)
        return get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Sets day of persian month
     */
    fun setDayOfMonth(day: Int) = internalToGregory(year, month, day)

    /**
     * @return total days in this month (checks for leap)
     */
    fun getMaxDaysInMonth(): Int {
        return if (isPersianLeapYear()) PERSIAN_DAYS_IN_MONTH_LEAP[month]
        else PERSIAN_DAYS_IN_MONTH[month]
    }

    /**
     * Determines if this year is a Persian leap year or not
     * @return true if this year is a Persian leap year
     */
    private fun isPersianLeapYear(): Boolean {
        val a = year - 474L
        val b: Long = mod(a.toDouble(), 2820L.toDouble()) + 474L
        return mod((b + 38.0) * 682.0, 2816.0) < 682L
    }

    /**
     * Sets date in Persian format
     * @param year Persian year
     * @param month Persian month
     * @param day of month
     */
    fun setPersian(year: Int, month: Int, day: Int) = internalToGregory(year, month, day)

    /**
     * Converts Gregorian date to Persian
     * and sets the [year] [month] [day] properties
     */
    private fun internalCalculate() {
        var y = get(YEAR)
        val m = get(MONTH)
        var d = get(Calendar.DAY_OF_MONTH)

        var pYear: Int
        val pMonth: Int
        val pDay: Int
        var pDayNo: Int
        val pNP: Int
        var gDayNo: Int

        y -= 1600
        d -= 1

        gDayNo = (365 * y + floor((y + 3) / 4.toDouble()).toInt()
                - floor((y + 99) / 100.toDouble()).toInt()
                + floor((y + 399) / 400.toDouble()).toInt())

        var i = 0
        while (i < m) {
            gDayNo += GREGORIAN_DAYS_IN_MONTH[i]
            ++i
        }

        if (m > 1 && (y % 4 == 0 && y % 100 != 0 || year % 400 == 0))
            ++gDayNo

        gDayNo += d

        pDayNo = gDayNo - 79
        pNP = floor(pDayNo / 12053.toDouble()).toInt()
        pDayNo %= 12053
        pYear = 979 + 33 * pNP + 4 * (pDayNo / 1461)
        pDayNo %= 1461
        if (pDayNo >= 366) {
            pYear += floor((pDayNo - 1) / 365.toDouble()).toInt()
            pDayNo = (pDayNo - 1) % 365
        }

        i = 0
        while (i < 11 && pDayNo >= PERSIAN_DAYS_IN_MONTH[i]) {
            pDayNo -= PERSIAN_DAYS_IN_MONTH[i]
            ++i
        }

        pMonth = i
        pDay = pDayNo + 1

        this.year = pYear
        this.month = pMonth
        this.day = pDay

    }

    /**
     * Converts Persian date to Gregory and
     * sets [Calendar.YEAR] [Calendar.MONTH] [Calendar.DAY_OF_MONTH] of this calendar
     * @param y Persian year
     * @param m Persian month
     * @param d Persian day
     * @see Calendar.set()
     */
    private fun internalToGregory(y: Int, m: Int, d: Int) {
        var year = y
        var day = d

        var gYear: Int
        val gMonth: Int
        val gDay: Int
        var gDayNo: Int
        var pDayNo: Int
        var leap: Int

        year -= 979
        day -= 1

        pDayNo = 365 * year + (year / 33) * 8 + floor(((year % 33 + 3) / 4).toDouble()).toInt()
        var i = 0
        while (i < m) {
            pDayNo += PERSIAN_DAYS_IN_MONTH[i]
            ++i
        }

        pDayNo += day
        gDayNo = pDayNo + 79
        gYear = 1600 + 400 * floor(gDayNo / 146097.toDouble()).toInt()
        gDayNo %= 146097
        leap = 1
        if (gDayNo >= 36525) {
            gDayNo--
            gYear += 100 * floor(gDayNo / 36524.toDouble()).toInt()
            gDayNo %= 36524
            if (gDayNo >= 365)
                gDayNo++
            else leap = 0
        }
        gYear += 4 * floor(gDayNo / 1461.toDouble()).toInt()
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = 0
            gDayNo--
            gYear += floor(gDayNo / 365.toDouble()).toInt()
            gDayNo %= 365
        }
        i = 0
        while (gDayNo >= GREGORIAN_DAYS_IN_MONTH[i] + if (i == 1 && leap == 1) i else 0) {
            gDayNo -= GREGORIAN_DAYS_IN_MONTH[i] + if (i == 1 && leap == 1) i else 0
            i++
        }

        gMonth = i
        gDay = gDayNo + 1

        set(gYear, gMonth, gDay)
    }

    /**
     * @return name of this month in Persian
     * @see MONTH_NAMES
     */
    fun getMonthName(): String = MONTH_NAMES[month]


    companion object {

        /**
         * Max days in a week in Persian Calendar -1
         */
        const val DAYS_IN_WEEK = 6

        /**
         * Max days in a month in Gregorian calendar
         */
        private val GREGORIAN_DAYS_IN_MONTH = intArrayOf(
            31, 28, 31, 30, 31, 30, 31,
            31, 30, 31, 30, 31
        )

        /**
         * Max days in a month in Persian calendar
         */
        private val PERSIAN_DAYS_IN_MONTH = intArrayOf(
            31, 31, 31, 31, 31, 31, 30, 30,
            30, 30, 30, 29
        )

        /**
         * Max days in a month in Persian leap year calendar
         */
        private val PERSIAN_DAYS_IN_MONTH_LEAP = intArrayOf(
            31, 31, 31, 31, 31, 31, 30, 30,
            30, 30, 30, 30
        )

        /**
         *
         * A modulo function suitable for our purpose.
         *
         * @param a the dividend.
         * @param b the divisor.
         * @return the remainder of integer division.
         */
        fun mod(a: Double, b: Double): Long = (a - b * floor(a / b)).toLong()


        /**
         * @return an instance of [PersianCalendar] for today
         */
        fun getToday(): PersianCalendar = todayCalendar
    }
}