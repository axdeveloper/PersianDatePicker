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
import android.graphics.Paint
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.getColorStateList
import com.xdev.arch.persiancalendar.datepicker.utils.resolve

/**
 * Data class for loaded [R.styleable.PersianMaterialCalendar] and
 * [R.styleable.PersianMaterialCalendarItem] attributes.
 */
class CalendarStyle(context: Context) {
    val day: CalendarItemStyle
    val selectedDay: CalendarItemStyle
    val todayDay: CalendarItemStyle
    val year: CalendarItemStyle
    val selectedYear: CalendarItemStyle

    val todayYear: CalendarItemStyle
    val invalidDay: CalendarItemStyle

    val rangeFill: Paint

    init {
        val calendarStyle: Int
        val style = resolve(context, R.attr.persianMaterialCalendarStyle)

        calendarStyle = style?.data ?: R.style.PersianMaterialCalendar_Default

        val calendarAttributes =
            context.obtainStyledAttributes(calendarStyle, R.styleable.PersianMaterialCalendar)

        val typeface: Int = calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_typeface, -1)

        day = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayStyle, 0),
            typeface
        )

        selectedDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_daySelectedStyle, 0),
            typeface
        )

        invalidDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayInvalidStyle, 0),
            typeface
        )
        todayDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayTodayStyle, 0),
            typeface
        )
        val rangeFillColorList =
            getColorStateList(
                context, calendarAttributes, R.styleable.PersianMaterialCalendar_rangeFillColor)

        year = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_yearStyle, 0),
            typeface
        )

        selectedYear = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(
                R.styleable.PersianMaterialCalendar_yearSelectedStyle,
                0
            ), typeface
        )

        todayYear = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_yearTodayStyle, 0),
            typeface
        )
        rangeFill = Paint()
        rangeFill.color = rangeFillColorList!!.defaultColor
        calendarAttributes.recycle()
    }
}