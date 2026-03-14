package com.xdev.arch.persiancalendar.datepicker

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

class CompositeDateValidator(private val validators: List<CalendarConstraints.DateValidator?>)
    : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        for (validator in validators) {
            if (validator == null) continue
            if (!validator.isValid(date)) return false
        }
        return true
    }

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeList(validators)
    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CompositeDateValidator> {
        override fun createFromParcel(parcel: Parcel): CompositeDateValidator {
            val validators: MutableList<CalendarConstraints.DateValidator> = ArrayList()
            ParcelCompat.readList(
                parcel,
                validators,
                CalendarConstraints.DateValidator::class.java.classLoader,
                CalendarConstraints.DateValidator::class.java
            )
            return CompositeDateValidator(validators)
        }

        override fun newArray(size: Int): Array<CompositeDateValidator?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeDateValidator) return false
        return validators == other.validators
    }

    override fun hashCode() = validators.hashCode()
}