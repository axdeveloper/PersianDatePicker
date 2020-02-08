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
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.canonicalYearMonthDay
import com.xdev.arch.persiancalendar.datepicker.utils.getYearMonthDay
import com.xdev.arch.persiancalendar.datepicker.utils.resolve
import java.util.*

/**
 * A [DateSelector] that uses a [Long] for its selection state.
 *
 * @hide
 */
class SingleDateSelector : DateSelector<Long?> {
    private var selectedItem: Long? = null
    override fun select(selection: Long) {
        selectedItem = selection
    }

    override fun setSelection(selection: Long?) {
        selectedItem = if (selection == null) null else canonicalYearMonthDay(selection)
    }

    override val isSelectionComplete: Boolean
        get() = selectedItem != null

    override val selectedRanges: Collection<Pair<Long?, Long?>>
        get() = ArrayList()

    override val selectedDays: Collection<Long>
        get() {
            val selections = ArrayList<Long>()
            if (selectedItem != null) {
                selections.add(selectedItem!!)
            }
            return selections
        }

    override fun getSelection(): Long? {
        return selectedItem
    }

    override fun getDefaultThemeResId(context: Context): Int {
        val theme = resolve(context, R.attr.persianMaterialCalendarTheme)
        return theme?.data ?: R.style.PersianMaterialCalendar_Default
    }

    override fun getSelectionDisplayString(context: Context): String {
        val res = context.resources
        if (selectedItem == null) {
            return res.getString(R.string.picker_date_header_unselected)
        }
        val startString = getYearMonthDay(selectedItem!!)
        return res.getString(R.string.picker_date_header_selected, startString)
    }

    override val defaultTitleResId: Int
        get() = R.string.picker_date_header_title

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(selectedItem)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SingleDateSelector> =
            object :
                Parcelable.Creator<SingleDateSelector> {
                override fun createFromParcel(source: Parcel): SingleDateSelector {
                    val singleDateSelector =
                        SingleDateSelector()
                    singleDateSelector.selectedItem =
                        source.readValue(Long::class.java.classLoader) as Long?
                    return singleDateSelector
                }

                override fun newArray(size: Int): Array<SingleDateSelector?> {
                    return arrayOfNulls(size)
                }
            }
    }
}