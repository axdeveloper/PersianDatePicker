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
import android.os.Parcel
import android.os.Parcelable
import androidx.core.util.Preconditions
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.canonicalYearMonthDay
import com.xdev.arch.persiancalendar.datepicker.utils.getDateRangeString
import com.xdev.arch.persiancalendar.datepicker.utils.getDateString
import com.xdev.arch.persiancalendar.datepicker.utils.resolve
import java.util.*

class RangeDateSelector : DateSelector<Pair<Long?, Long?>> {
    private var selectedStartItem: Long? = null
    private var selectedEndItem: Long? = null
    override fun select(selection: Long) {
        if (selectedStartItem == null) {
            selectedStartItem = selection
        } else if (selectedEndItem == null && isValidRange(selectedStartItem!!, selection)) {
            selectedEndItem = selection
        } else {
            selectedEndItem = null
            selectedStartItem = selection
        }
    }

    override val isSelectionComplete: Boolean
        get() = selectedStartItem != null && selectedEndItem != null && isValidRange(
            selectedStartItem!!,
            selectedEndItem!!
        )

    override fun setSelection(selection: Pair<Long?, Long?>) {
        if (selection.first != null && selection.second != null) {
            Preconditions.checkArgument(
                isValidRange(
                    selection.first!!,
                    selection.second!!
                )
            )
        }
        selectedStartItem =
            if (selection.first == null) null else canonicalYearMonthDay(
                selection.first!!
            )
        selectedEndItem =
            if (selection.second == null) null else canonicalYearMonthDay(
                selection.second!!
            )
    }

    override fun getSelection(): Pair<Long?, Long?> {
        return Pair(
            selectedStartItem,
            selectedEndItem
        )
    }

    override val selectedRanges: Collection<Pair<Long?, Long?>>
        get() {
            if (selectedStartItem == null || selectedEndItem == null) {
                return ArrayList()
            }
            val ranges = ArrayList<Pair<Long?, Long?>>()
            val range = Pair(selectedStartItem, selectedEndItem)
            ranges.add(range)
            return ranges
        }

    override val selectedDays: Collection<Long>
        get() {
            val selections = ArrayList<Long>()
            if (selectedStartItem != null) {
                selections.add(selectedStartItem!!)
            }
            if (selectedEndItem != null) {
                selections.add(selectedEndItem!!)
            }
            return selections
        }

    override fun getDefaultThemeResId(context: Context): Int {
        val theme = resolve(context, R.attr.persianMaterialCalendarTheme)
        return theme?.data ?: R.style.PersianMaterialCalendar_Default_MaterialCalendar
    }

    override fun getSelectionDisplayString(context: Context): String {
        val res = context.resources
        if (selectedStartItem == null && selectedEndItem == null) {
            return res.getString(R.string.picker_range_header_unselected)
        }
        if (selectedEndItem == null) {
            return res.getString(
                R.string.picker_range_header_only_start_selected,
                getDateString(selectedStartItem!!, res)
            )
        }
        if (selectedStartItem == null) {
            return res.getString(
                R.string.picker_range_header_only_end_selected,
                getDateString(selectedEndItem!!, res)
            )
        }
        val dateRangeStrings = getDateRangeString(selectedStartItem, selectedEndItem, res)
        return res.getString(
            R.string.picker_range_header_selected,
            dateRangeStrings.first,
            dateRangeStrings.second
        )
    }

    override val defaultTitleResId: Int
        get() = R.string.picker_range_header_title

    private fun isValidRange(start: Long, end: Long): Boolean {
        return start <= end
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(selectedStartItem)
        dest.writeValue(selectedEndItem)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RangeDateSelector> =
            object :
                Parcelable.Creator<RangeDateSelector> {
                override fun createFromParcel(source: Parcel): RangeDateSelector {
                    val rangeDateSelector =
                        RangeDateSelector()
                    rangeDateSelector.selectedStartItem =
                        source.readValue(Long::class.java.classLoader) as Long?
                    rangeDateSelector.selectedEndItem =
                        source.readValue(Long::class.java.classLoader) as Long?
                    return rangeDateSelector
                }

                override fun newArray(size: Int): Array<RangeDateSelector?> {
                    return arrayOfNulls(size)
                }
            }
    }
}