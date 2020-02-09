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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.BoringLayout
import android.text.Layout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.xdev.arch.persiancalendar.R
import kotlin.math.min

/** Simple [android.widget.TextView] to avoid using too much memory */
class SimpleTextView @JvmOverloads constructor(
    context: Context,
    var attrs: AttributeSet? = null,
    var defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var text = ""
        set(value) {
            field = value
            init()
            invalidate()
        }

    private lateinit var mTextPaint: TextPaint
    private lateinit var mMetrics: BoringLayout.Metrics
    private lateinit var mLayout: BoringLayout

    init {
        if (!TextUtils.isEmpty(this.text))
            init()
    }

    @SuppressLint("CustomViewStyleable", "PrivateResource")
    private fun init() {
        val r = context.resources

        val style = context.obtainStyledAttributes(attrs, R.styleable.TextAppearance, defStyleAttr, 0)

        var typeface: Typeface? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                typeface = style.getFont(R.styleable.TextAppearance_android_typeface)
            } catch (e: Exception) { }
        }

        if (typeface == null) {
            var font = style.getResourceId(R.styleable.TextAppearance_fontFamily, -1)
            if (font == -1)
                font = style.getResourceId(R.styleable.TextAppearance_android_typeface, -1)

            if (font != -1)
                typeface = ResourcesCompat.getFont(context, font)
        }

        val textSize = style.getDimension(R.styleable.TextAppearance_android_textSize, r.getDimension(R.dimen.day_text_size))
        val textColor = style.getColor(R.styleable.TextAppearance_android_textColor, Color.BLACK)

        style.recycle()

        mTextPaint = TextPaint()
        mTextPaint.isAntiAlias = true
        mTextPaint.color = textColor
        mTextPaint.textSize = textSize
        if (typeface != null)
            mTextPaint.typeface = typeface

        val width = mTextPaint.measureText(this.text).toInt()

        mMetrics = BoringLayout.Metrics()

        val metrics = mTextPaint.fontMetricsInt

        mMetrics.ascent = metrics.ascent
        mMetrics.bottom = metrics.bottom
        mMetrics.descent = metrics.descent
        mMetrics.leading = metrics.leading
        mMetrics.top = metrics.top

        mLayout = BoringLayout(
            this.text, mTextPaint, width,
            Layout.Alignment.ALIGN_CENTER,
            0f,
            0f,
            mMetrics,
            false
        )
    }

    fun setText(number: Int) {
        this.text = number.toString()
        init()
        invalidate()
    }

    fun setTextColor(color: Int) {
        mTextPaint.color = color
    }

    fun setTextColor(color: ColorStateList) {
        mTextPaint.color = color.defaultColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthRequirement = MeasureSpec.getSize(widthMeasureSpec)

        width = if (widthMode == MeasureSpec.EXACTLY)
            widthRequirement
        else
            mLayout.width + paddingLeft + paddingRight

        var height: Int
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightRequirement = MeasureSpec.getSize(heightMeasureSpec)

        if (heightMode == MeasureSpec.EXACTLY)
            height = heightRequirement
        else {
            height = mLayout.height + paddingTop + paddingBottom

            if (heightMode == MeasureSpec.AT_MOST)
                height = min(height, heightRequirement)
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerStart = width / 2 - mLayout.width / 2
        val centerTop = height / 2 - mLayout.height / 2

        canvas.save()
        canvas.translate(centerStart.toFloat(), centerTop.toFloat())
        mLayout.draw(canvas)
        canvas.restore()
    }
}