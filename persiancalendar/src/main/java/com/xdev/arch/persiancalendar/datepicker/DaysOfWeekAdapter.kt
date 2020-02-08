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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar.Companion.DAYS_IN_WEEK
import com.xdev.arch.persiancalendar.datepicker.utils.getAbbrDayName

/**
 * A single row adapter representing the days of the week for [PersianCalendar].
 * This [android.widget.Adapter] respects the [PersianCalendar.getFirstDayOfWeek]
 */
internal class DaysOfWeekAdapter : BaseAdapter() {

    override fun getItem(position: Int): Int? {
        return if (position > DAYS_IN_WEEK) {
            null
        } else position
    }

    override fun getItemId(position: Int): Long = 0

    override fun getCount(): Int = DAYS_IN_WEEK + 1

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View? {
        var dayOfWeek = convertView
        if (dayOfWeek == null) {
            val layoutInflater = LayoutInflater.from(parent.context)
            dayOfWeek = layoutInflater.inflate(R.layout.calendar_day_of_week, parent, false)
        }

        (dayOfWeek as SimpleTextView).text = getAbbrDayName(position)
        return dayOfWeek
    }
}