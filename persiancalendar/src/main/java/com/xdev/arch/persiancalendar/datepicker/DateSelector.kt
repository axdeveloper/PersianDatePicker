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
import android.os.Parcelable
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.xdev.arch.persiancalendar.R

/**
 * Interface for users of [&lt;S&gt;][MaterialCalendar] to control how the Calendar displays and
 * returns selections.
 *
 *
 * Implementors must implement [Parcelable] so that selection can be maintained through
 * Lifecycle events (e.g., Fragment destruction).
 *
 *
 * Dates are represented as times in UTC milliseconds.
 *
 * @param <S> The type of item available when cells are selected in the [AdapterView]
 * @hide
</S> */
interface DateSelector<S> : Parcelable {

    /** Returns the current selection.  */
    fun getSelection(): S?

    /** Returns true if the current selection is acceptable.  */
    val isSelectionComplete: Boolean

    /**
     * Sets the current selection to `selection`.
     *
     * @throws IllegalArgumentException If `selection` creates an invalid state.
     */
    fun setSelection(selection: S)

    /**
     * Allows this selection handler to respond to clicks within the [AdapterView].
     *
     * @param selection The selected day represented as time in UTC milliseconds.
     */
    fun select(selection: Long)

    /**
     * Returns a list of longs whose time value represents days that should be marked selected.
     *
     *
     * Uses [R.styleable.PersianMaterialCalendar_daySelectedStyle] for styling.
     */
    val selectedDays: Collection<Long>

    /**
     * Returns a list of ranges whose time values represent ranges that should be filled.
     *
     *
     * Uses [R.styleable.PersianMaterialCalendar_rangeFillColor] for styling.
     */
    val selectedRanges: Collection<Pair<Long?, Long?>>

    fun getSelectionDisplayString(context: Context): String

    @get:StringRes
    val defaultTitleResId: Int

    @StyleRes
    fun getDefaultThemeResId(context: Context): Int
}