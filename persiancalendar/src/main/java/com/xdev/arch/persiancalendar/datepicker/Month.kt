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

package com.xdev.arch.persiancalendar.datepicker

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import com.xdev.arch.persiancalendar.datepicker.utils.formatYearMonth
import com.xdev.arch.persiancalendar.datepicker.utils.getDayCopy
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar
import com.xdev.arch.persiancalendar.datepicker.utils.iranCalendar
import java.util.*

/** Contains convenience operations for a month within a specific year. */
class Month private constructor(rawCalendar: PersianCalendar) :
    Comparable<Month>, Parcelable {

    /** The acceptable int values for month when using [Month.create] */
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        FARVARDIN,
        ORDIBEHESHT,
        KHORDAD,
        TIR,
        MORDAD,
        SHAHRIVAR,
        MEHR,
        ABAN,
        AZAR,
        DAY,
        BAHMAN,
        ESFAND
    )
    annotation class Months

    private val firstOfMonth: PersianCalendar
    val longName: String
    @Months val month: Int
    val year: Int
    val daysInWeek: Int
    val daysInMonth: Int
    val timeInMillis: Long

    fun daysFromStartOfWeekToFirstOfMonth(): Int {
        var difference = firstOfMonth.getFirstDayOfMonth() - firstOfMonth.firstDayOfWeek
        if (difference < 0) {
            difference += daysInWeek
        }
        return difference
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Month) {
            return false
        }

        return month == other.month && year == other.year
    }

    override fun hashCode(): Int {
        val hashedFields = arrayOf<Any>(month, year)
        return hashedFields.contentHashCode()
    }

    override fun compareTo(other: Month): Int {
        return firstOfMonth.compareTo(other.firstOfMonth)
    }

    override fun toString(): String {
        return "$year/$month"
    }

    fun monthsUntil(other: Month): Int {
        return (other.year - year) * 12 + (other.month - month)
    }

    val stableId: Long
        get() = firstOfMonth.timeInMillis

    fun getDay(day: Int): Long {
        val dayCalendar = getDayCopy(firstOfMonth)
        dayCalendar.setDayOfMonth(day)
        return dayCalendar.timeInMillis
    }

    fun monthsLater(months: Int): Month {
        val laterMonth = getDayCopy(firstOfMonth)
        laterMonth.add(Calendar.MONTH, months)
        return Month(laterMonth)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(year)
        dest.writeInt(month)
    }

    companion object {

        const val FARVARDIN = 0
        const val ORDIBEHESHT = 1
        const val KHORDAD = 2
        const val TIR = 3
        const val MORDAD = 4
        const val SHAHRIVAR = 5
        const val MEHR = 6
        const val ABAN = 7
        const val AZAR = 8
        const val DAY = 9
        const val BAHMAN = 10
        const val ESFAND = 11

        /**
         * Creates an instance of Month that contains the provided [timeInMillis] where [timeInMillis]
         * is in milliseconds.
         */
        fun create(timeInMillis: Long): Month {
            val calendar = iranCalendar
            calendar.timeInMillis = timeInMillis

            return Month(calendar)
        }

        /**
         * Creates an instance of Month with the given parameters backed by a [PersianCalendar].
         *
         * @param year The year
         * @param month The 0-index based month. Use [Month] constants (e.g., [Month.FARVARDIN]])
         * @return A Month object backed by a new [PersianCalendar] instance
         */
        fun create(year: Int, @Months month: Int): Month {
            val calendar = iranCalendar
            calendar.setPersian(year, month, 1)
            return Month(calendar)
        }

        /**
         * Returns the [Month] that contains today in the IRST timezone (as per [Calendar.getInstance].
         */
        fun today(): Month {
            return Month(todayCalendar)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Month> =
            object : Parcelable.Creator<Month> {
                override fun createFromParcel(source: Parcel): Month {
                    val year = source.readInt()
                    val month = source.readInt()
                    return create(year, month)
                }

                override fun newArray(size: Int): Array<Month?> {
                    return arrayOfNulls(size)
                }
            }
    }

    init {
        rawCalendar.setDayOfMonth(1)
        firstOfMonth = getDayCopy(rawCalendar)
        month = firstOfMonth.month
        year = firstOfMonth.year
        daysInWeek = firstOfMonth.getMaximum(Calendar.DAY_OF_WEEK)
        daysInMonth = firstOfMonth.getMaxDaysInMonth()
        longName = formatYearMonth(firstOfMonth)
        timeInMillis = firstOfMonth.timeInMillis
    }
}