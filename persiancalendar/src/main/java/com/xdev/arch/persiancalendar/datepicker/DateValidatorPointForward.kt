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
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar

/**
 * A [CalendarConstraints.DateValidator] that enables dates from a given point forward.
 * Defaults to the current moment in device time forward using
 * [DateValidatorPointForward.now], but can be set to any point, as UTC milliseconds, using
 * [DateValidatorPointForward.from].
 */
class DateValidatorPointForward private constructor(private val point: Long) :
    CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        return date >= point
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(point)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is DateValidatorPointForward) {
            return false
        }
        return point == other.point
    }

    override fun hashCode(): Int {
        val hashedFields = arrayOf<Any>(point)
        return hashedFields.contentHashCode()
    }

    companion object {

        /**
         * Returns a [CalendarConstraints.DateValidator] which enables days from `point`, in
         * UTC milliseconds, forward.
         */
        fun from(point: Long): DateValidatorPointForward {
            return DateValidatorPointForward(point)
        }

        /**
         * Returns a [CalendarConstraints.DateValidator] enabled from the current moment in device
         * time forward.
         */
        fun now(): DateValidatorPointForward {
            return from(todayCalendar.timeInMillis)
        }

        /** Part of [Parcelable] requirements. Do not use.  */
        @JvmField
        val CREATOR: Parcelable.Creator<DateValidatorPointForward> =
            object :
                Parcelable.Creator<DateValidatorPointForward> {
                override fun createFromParcel(source: Parcel): DateValidatorPointForward {
                    return DateValidatorPointForward(source.readLong())
                }

                override fun newArray(size: Int): Array<DateValidatorPointForward?> {
                    return arrayOfNulls(size)
                }
            }
    }

}