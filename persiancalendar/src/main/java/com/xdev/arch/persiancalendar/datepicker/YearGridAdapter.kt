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
import androidx.recyclerview.widget.RecyclerView
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar

internal class YearGridAdapter(private val materialCalendar: MaterialCalendar<*>) :
    RecyclerView.Adapter<YearGridAdapter.ViewHolder>() {

    class ViewHolder internal constructor(val textView: SimpleTextView) :
        RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val yearTextView = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.calendar_year,
            viewGroup,
            false
        ) as SimpleTextView
        return ViewHolder(yearTextView)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val year = getYearForPosition(position)
        viewHolder.textView.setText(year)
        val styles = materialCalendar.calendarStyle
        val calendar = todayCalendar
        var style = if (calendar.year == year) styles.todayYear else styles.year

        for (day in materialCalendar.dateSelector!!.selectedDays) {
            calendar.timeInMillis = day
            if (calendar.year == year) style = styles.selectedYear
        }

        style.styleItem(viewHolder.textView)
        viewHolder.textView.setOnClickListener(createYearClickListener(year))
    }

    private fun createYearClickListener(year: Int): View.OnClickListener {
        return View.OnClickListener {
            val moveTo: Month = Month.create(year, materialCalendar.currentMonth.month)
            materialCalendar.currentMonth = moveTo
            materialCalendar.setSelector(MaterialCalendar.CalendarSelector.DAY)
        }
    }

    override fun getItemCount(): Int {
        return materialCalendar.calendarConstraints.yearSpan
    }

    fun getPositionForYear(year: Int): Int {
        return year - materialCalendar.calendarConstraints.start.year
    }

    private fun getYearForPosition(position: Int): Int {
        return materialCalendar.calendarConstraints.start.year + position
    }
}