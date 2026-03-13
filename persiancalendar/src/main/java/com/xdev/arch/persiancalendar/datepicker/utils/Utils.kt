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

package com.xdev.arch.persiancalendar.datepicker.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils


fun getColorStateList(
    context: Context, attributes: TypedArray, @StyleableRes index: Int
): ColorStateList? {
    if (attributes.hasValue(index)) {
        val resourceId = attributes.getResourceId(index, 0)
        if (resourceId != 0) {
            val value =
                AppCompatResources.getColorStateList(context, resourceId)
            if (value != null) {
                return value
            }
        }
    }

    return attributes.getColorStateList(index)
}

fun resolve(context: Context, @AttrRes attributeResId: Int): TypedValue? {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attributeResId, typedValue, true)) {
        typedValue
    } else null
}

@JvmField
val BUTTON_STATES = arrayOf(
    intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked, android.R.attr.state_checkable),
    intArrayOf(android.R.attr.state_checkable, -android.R.attr.state_checked, android.R.attr.state_enabled),
    intArrayOf(android.R.attr.state_enabled),
    intArrayOf())

internal fun getButtonTextColorList(color: Int): ColorStateList {
    val colors = intArrayOf(
        color,
        ColorUtils.setAlphaComponent(Color.BLACK, 153),
        color,
        ColorUtils.setAlphaComponent(Color.BLACK, 97))

    return ColorStateList(BUTTON_STATES, colors)
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}

inline fun <reified T : Parcelable> Parcel.parcelable(): T? {
    val loader = T::class.java.classLoader
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readParcelable(loader, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        readParcelable(loader)
    }
}

inline fun <reified T> Parcel.readListCompat(list: MutableList<T>, classLoader: ClassLoader?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readList(list, classLoader, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        readList(list, classLoader)
    }
}