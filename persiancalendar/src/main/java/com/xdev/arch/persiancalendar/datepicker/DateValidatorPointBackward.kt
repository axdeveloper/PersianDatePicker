package com.xdev.arch.persiancalendar.datepicker

import android.os.Parcel
import android.os.Parcelable
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar

class DateValidatorPointBackward(val point: Long) : CalendarConstraints.DateValidator {

    override fun isValid(date: Long) = date <= point

    override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeLong(point)

    override fun describeContents() = 0

    companion object {

        fun before(point: Long) = DateValidatorPointBackward(point)

        fun now() = before(todayCalendar.timeInMillis)

        @JvmField
        val CREATOR = object : Parcelable.Creator<DateValidatorPointBackward> {
            override fun createFromParcel(parcel: Parcel): DateValidatorPointBackward {
                return DateValidatorPointBackward(parcel.readLong())
            }

            override fun newArray(size: Int): Array<DateValidatorPointBackward?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateValidatorPointBackward) return false
        return point == other.point
    }

    override fun hashCode() = arrayOf(point).contentHashCode()
}