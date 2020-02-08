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

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Used to limit the display range of [MaterialCalendar] and set an openAt month.
 *
 * Implements [Parcelable] in order to maintain the [CalendarConstraints] across
 * device configuration changes. Parcelable breaks when passed between processes.
 */
class CalendarConstraints private constructor(

    /** Earliest [Month] allowed by this set of bounds. */
    val start: Month,
    /** Latest [Month] allowed by this set of bounds. */
    val end: Month,
    /** OpenAt [Month] within this set of bounds. */
    val openAt: Month,

    /**
     * The [DateValidator] that determines whether a date can be clicked and selected.
     */
    val dateValidator: DateValidator
) : Parcelable {

    /**
     * The total number of [java.util.Calendar.MONTH] included in [start] to [end].
     */
    val yearSpan: Int

    /**
     * The total number of [java.util.Calendar.YEAR] included in [start] to [end].
     */
    val monthSpan: Int

    /**
     * Used to determine whether [MaterialCalendar] days are enabled.
     *
     * Extends [Parcelable] in order to maintain the [DateValidator] across device
     * configuration changes. Parcelable breaks when passed between processes.
     */
    interface DateValidator : Parcelable {

        /** Returns true if the provided [date] is enabled. */
        fun isValid(date: Long): Boolean
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CalendarConstraints) {
            return false
        }
        return start == other.start
                && end == other.end
                && openAt == other.openAt
                && dateValidator == other.dateValidator
    }

    override fun hashCode(): Int {
        val hashedFields =
            arrayOf<Any?>(start, end, openAt, dateValidator)
        return hashedFields.contentHashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(start, 0)
        dest.writeParcelable(end, 0)
        dest.writeParcelable(openAt, 0)
        dest.writeParcelable(dateValidator, 0)
    }

    /** Builder for [CalendarConstraints] */
    class Builder {
        private var start = DEFAULT_START
        private var end = DEFAULT_END
        private var openAt: Long? = null
        private var validator: DateValidator? = DateValidatorPointForward.from(Long.MIN_VALUE)

        constructor()

        internal constructor(clone: CalendarConstraints) {
            start = clone.start.timeInMillis
            end = clone.end.timeInMillis
            openAt = clone.openAt.timeInMillis
            validator = clone.dateValidator
        }

        /**
         * A timeInMilliseconds contained within the earliest month the calendar will page to.
         * Defaults [DEFAULT_START].
         */
        fun setStart(month: Long): Builder {
            start = month
            return this
        }

        /**
         * A timeInMilliseconds contained within the latest month the calendar will page to.
         * Defaults [DEFAULT_END].
         */
        fun setEnd(month: Long): Builder {
            end = month
            return this
        }


        /**
         * A timeInMilliseconds contained within the month the calendar should openAt. Defaults to
         * the month containing today if within bounds; otherwise, defaults to the starting month.
         */
        fun setOpenAt(month: Long): Builder {
            openAt = month
            return this
        }

        /**
         * Limits valid dates to those for which [DateValidator.isValid] is true. Defaults
         * to all dates as valid.
         */
        fun setValidator(validator: DateValidator?): Builder {
            this.validator = validator
            return this
        }

        /** Builds the [CalendarConstraints] object using the set parameters or defaults. */
        fun build(): CalendarConstraints {
            if (openAt == null) {
                val today: Long = MaterialDatePicker.thisMonthInIrstMilliseconds()
                openAt = if (today in start..end) today else start
            }
            val deepCopyBundle = Bundle()
            deepCopyBundle.putParcelable(
                DEEP_COPY_VALIDATOR_KEY,
                validator
            )

            return CalendarConstraints(
                Month.create(start),
                Month.create(end),
                Month.create(openAt!!),
                deepCopyBundle.getParcelable<Parcelable>(DEEP_COPY_VALIDATOR_KEY) as DateValidator
            )
        }

        companion object {

            /**
             * Default IRST timeInMilliseconds for the first selectable month unless [Builder.setStart]
             * is called. Set to Farvardin, 1388.
             */
            @JvmField
            val DEFAULT_START = Month.create(1388, Month.FARVARDIN).timeInMillis

            /**
             * Default IRST timeInMilliseconds for the last selectable month unless [Builder.setEnd] is
             * called. Set to Esfand, 1409.
             */
            @JvmField
            val DEFAULT_END = Month.create(1409, Month.ESFAND).timeInMillis

            private const val DEEP_COPY_VALIDATOR_KEY = "DEEP_COPY_VALIDATOR_KEY"
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CalendarConstraints> =
            object :
                Parcelable.Creator<CalendarConstraints> {
                override fun createFromParcel(source: Parcel): CalendarConstraints {
                    val start: Month = source.readParcelable(Month::class.java.classLoader)!!
                    val end: Month = source.readParcelable(Month::class.java.classLoader)!!
                    val openAt: Month = source.readParcelable(Month::class.java.classLoader)!!
                    val validator: DateValidator =
                        source.readParcelable(DateValidator::class.java.classLoader)!!
                    return CalendarConstraints(start, end, openAt, validator)
                }

                override fun newArray(size: Int): Array<CalendarConstraints?> {
                    return arrayOfNulls(size)
                }
            }
    }

    init {
        require(start <= openAt) { "start Month cannot be after current Month $start $openAt" }
        require(openAt <= end) { "current Month cannot be after end Month  $openAt $end" }
        monthSpan = start.monthsUntil(end) + 1
        yearSpan = end.year - start.year + 1
    }
}