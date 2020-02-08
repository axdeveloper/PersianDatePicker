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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.canonicalYearMonthDay
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar
import java.util.*

/**
 * Represents the days of a month with [SimpleTextView] instances for each day.
 *
 *
 * The number of rows is always equal to the maximum number of weeks that can exist across all
 * months (e.g., 6 for the GregorianCalendar).
 */
internal class MonthAdapter(
    val month: Month,
    /**
     * The [DateSelector] dictating the draw behavior of [getView].
     */
    val dateSelector: DateSelector<*>?,
    private val calendarConstraints: CalendarConstraints
) : BaseAdapter() {
    var calendarStyle: CalendarStyle? = null
    override fun hasStableIds(): Boolean {
        return true
    }

    /**
     * Returns a [Long] object for the given grid position
     *
     * @param position Index for the item. 0 matches the
     * [com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar.getFirstDayOfWeek] for the
     * first week of the month represented by [Month].
     * @return A [Long] representing the day at the position or null if the position does not
     * represent a valid day in the month.
     */
    override fun getItem(position: Int): Long? {
        return if (position < month.daysFromStartOfWeekToFirstOfMonth() || position > lastPositionInMonth()) {
            null
        } else month.getDay(positionToDay(position))
    }

    override fun getItemId(position: Int): Long {
        return (position / month.daysInWeek).toLong()
    }

    /**
     * Returns the number of days in a month plus the amount required to off-set for the first day to
     * the correct position within the week.
     *
     *
     * [MonthAdapter.firstPositionInMonth].
     *
     * @return The maximum valid position index
     */
    override fun getCount(): Int {
        return month.daysInMonth + firstPositionInMonth()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): SimpleTextView {
        initializeStyles(parent.context)
        var day = convertView

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent.context)
            day = layoutInflater.inflate(R.layout.calendar_day, parent, false) as SimpleTextView
        }

        day as SimpleTextView

        val offsetPosition = position - firstPositionInMonth()
        if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
            day.visibility = View.GONE
            day.isEnabled = false
        } else {
            val dayNumber = offsetPosition + 1
            day.setText(dayNumber)
            day.visibility = View.VISIBLE
            day.isEnabled = true
        }
        val date = getItem(position) ?: return day
        return if (calendarConstraints.dateValidator.isValid(date)) {
            day.isEnabled = true
            for (selectedDay in dateSelector!!.selectedDays) {
                if (canonicalYearMonthDay(date) == canonicalYearMonthDay(selectedDay)) {
                    calendarStyle!!.selectedDay.styleItem(day)
                    println("selected item $day")
                    return day
                }
            }

            if (canonicalYearMonthDay(todayCalendar.timeInMillis) == date) {
                calendarStyle!!.todayDay.styleItem(day)
                day
            } else {
                calendarStyle!!.day.styleItem(day)
                day
            }
        } else {
            day.isEnabled = false
            calendarStyle!!.invalidDay.styleItem(day)
            day
        }
    }

    private fun initializeStyles(context: Context) {
        if (calendarStyle == null) {
            calendarStyle = CalendarStyle(context)
        }
    }

    /**
     * Returns the index of the first position which is part of the month.
     *
     *
     * For example, this returns the position index representing Ordibehesht 1st. Since position 0
     * represents a day which must be the first day of the week, the first position in the month may
     * be greater than 0.
     */
    fun firstPositionInMonth(): Int {
        return month.daysFromStartOfWeekToFirstOfMonth()
    }

    /**
     * Returns the index of the last position which is part of the month.
     *
     *
     * For example, this returns the position index representing Khordad 31th. Since position 0
     * represents a day which must be the first day of the week, the last position in the month may
     * not match the number of days in the month.
     */
    fun lastPositionInMonth(): Int {
        return month.daysFromStartOfWeekToFirstOfMonth() + month.daysInMonth - 1
    }

    /**
     * Returns the day representing the provided adapter index
     *
     * @param position The adapter index
     * @return The day corresponding to the adapter index. May be non-positive for position inputs
     * less than [MonthAdapter.firstPositionInMonth].
     */
    private fun positionToDay(position: Int): Int {
        return position - month.daysFromStartOfWeekToFirstOfMonth() + 1
    }

    /** Returns the adapter index representing the provided day.  */
    fun dayToPosition(day: Int): Int {
        val offsetFromFirst = day - 1
        return firstPositionInMonth() + offsetFromFirst
    }

    /** True when a provided adapter position is within the calendar month  */
    fun withinMonth(position: Int): Boolean {
        return position >= firstPositionInMonth() && position <= lastPositionInMonth()
    }

    /**
     * True when the provided adapter position is the smallest position for a value of [ ][MonthAdapter.getItemId].
     */
    fun isFirstInRow(position: Int): Boolean {
        return position % month.daysInWeek == 0
    }

    /**
     * True when the provided adapter position is the largest position for a value of [ ][MonthAdapter.getItemId].
     */
    fun isLastInRow(position: Int): Boolean {
        return (position + 1) % month.daysInWeek == 0
    }

    companion object {
        /**
         * The maximum number of weeks possible in any month. 6 for [java.util.GregorianCalendar].
         */
        const val MAXIMUM_WEEKS = 6
    }

}