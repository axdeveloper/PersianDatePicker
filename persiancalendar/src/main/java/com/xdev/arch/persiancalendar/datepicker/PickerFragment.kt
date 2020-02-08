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

import androidx.fragment.app.Fragment
import java.util.*

abstract class PickerFragment<S> : Fragment() {
    protected val onSelectionChangedListeners =
        LinkedHashSet<OnSelectionChangedListener<S>>()

    abstract val dateSelector: DateSelector<S>?

    /** Adds a listener for selection changes. */
    fun addOnSelectionChangedListener(listener: OnSelectionChangedListener<S>): Boolean {
        return onSelectionChangedListeners.add(listener)
    }

    /** Removes a listener for selection changes. */
    fun removeOnSelectionChangedListener(listener: OnSelectionChangedListener<S>?): Boolean {
        return onSelectionChangedListeners.remove(listener)
    }

    /** Removes all listeners for selection changes. */
    fun clearOnSelectionChangedListeners() {
        onSelectionChangedListeners.clear()
    }
}