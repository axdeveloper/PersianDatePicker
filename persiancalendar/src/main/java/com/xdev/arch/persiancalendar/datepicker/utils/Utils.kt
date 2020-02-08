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
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources


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