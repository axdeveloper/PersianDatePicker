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
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ListAdapter
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.xdev.arch.persiancalendar.datepicker.utils.iranCalendar

internal class MaterialCalendarGridView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridView(context, attrs, defStyleAttr) {
    private val dayCompute = iranCalendar

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter?.notifyDataSetChanged()
    }

    override fun setSelection(position: Int) {
        adapter?.let {
            if (position < it.firstPositionInMonth()) {
                super.setSelection(it.firstPositionInMonth())
            } else {
                super.setSelection(position)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val result = super.onKeyDown(keyCode, event)
        if (!result) {
            return false
        }
        if (selectedItemPosition == AdapterView.INVALID_POSITION
            || selectedItemPosition >= adapter!!.firstPositionInMonth()
        ) {
            return true
        }
        if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
            adapter?.let { setSelection(it.firstPositionInMonth()) }
            return true
        }
        return false
    }

    override fun getAdapter(): MonthAdapter? {
        return super.getAdapter() as MonthAdapter?
    }

    override fun setAdapter(adapter: ListAdapter) {
        require(adapter is MonthAdapter) {
            String.format(
                "%1\$s must have its Adapter set to a %2\$s",
                MaterialCalendarGridView::class.java.canonicalName,
                MonthAdapter::class.java.canonicalName
            )
        }
        super.setAdapter(adapter)
    }

    override fun getLayoutDirection(): Int {
        return View.LAYOUT_DIRECTION_RTL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        adapter?.let {
            val monthAdapter = it
            val dateSelector = monthAdapter.dateSelector
            val calendarStyle = monthAdapter.calendarStyle
            val firstOfMonth = monthAdapter.getItem(monthAdapter.firstPositionInMonth())
            val lastOfMonth = monthAdapter.getItem(monthAdapter.lastPositionInMonth())

            for (range in dateSelector!!.selectedRanges) {
                if (range.first == null || range.second == null) {
                    continue
                }

                val startItem = range.first!!
                val endItem = range.second!!
                if (skipMonth(
                        firstOfMonth,
                        lastOfMonth,
                        startItem,
                        endItem
                    )
                ) {
                    return
                }

                var firstHighlightPosition: Int
                var rangeHighlightStart: Int
                if (startItem < firstOfMonth!!) {
                    firstHighlightPosition = monthAdapter.firstPositionInMonth()
                    rangeHighlightStart =
                        if (monthAdapter.isFirstInRow(firstHighlightPosition)) width
                        else getChildAt(firstHighlightPosition - 1).left
                } else {
                    dayCompute.timeInMillis = startItem
                    firstHighlightPosition =
                        monthAdapter.dayToPosition(dayCompute.day)
                    rangeHighlightStart = horizontalMidPoint(getChildAt(firstHighlightPosition))
                }
                var lastHighlightPosition: Int
                var rangeHighlightEnd: Int
                if (endItem > lastOfMonth!!) {
                    lastHighlightPosition = monthAdapter.lastPositionInMonth()
                    rangeHighlightEnd =
                        if (monthAdapter.isLastInRow(lastHighlightPosition)) 0
                        else getChildAt(lastHighlightPosition).left
                } else {
                    dayCompute.timeInMillis = endItem
                    lastHighlightPosition =
                        monthAdapter.dayToPosition(dayCompute.day)
                    rangeHighlightEnd =
                        horizontalMidPoint(getChildAt(lastHighlightPosition))
                }
                val firstRow = monthAdapter.getItemId(firstHighlightPosition).toInt()
                val lastRow = monthAdapter.getItemId(lastHighlightPosition).toInt()
                for (row in firstRow..lastRow) {
                    val firstPositionInRow = row * numColumns
                    val lastPositionInRow = firstPositionInRow + numColumns - 1
                    val firstView = getChildAt(firstPositionInRow)
                    val top = firstView.top + calendarStyle!!.day.topInset
                    val bottom = firstView.bottom - calendarStyle.day.bottomInset

                    val left =
                        if (firstPositionInRow > firstHighlightPosition) width else rangeHighlightStart
                    val right =
                        if (lastHighlightPosition > lastPositionInRow) 0 else rangeHighlightEnd
                    canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        calendarStyle.rangeFill
                    )
                }
            }
        }
    }

    override fun onFocusChanged(
        gainFocus: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        if (gainFocus) {
            gainFocus(direction, previouslyFocusedRect)
        } else {
            super.onFocusChanged(false, direction, previouslyFocusedRect)
        }
    }

    private fun gainFocus(direction: Int, previouslyFocusedRect: Rect?) {
        adapter?.let {
            when (direction) {
                View.FOCUS_UP -> {
                    setSelection(it.lastPositionInMonth())
                }
                View.FOCUS_DOWN -> {
                    setSelection(it.firstPositionInMonth())
                }
                else -> {
                    super.onFocusChanged(true, direction, previouslyFocusedRect)
                }
            }
        }
    }

    companion object {
        private fun skipMonth(
            firstOfMonth: Long?,
            lastOfMonth: Long?,
            startDay: Long?,
            endDay: Long?
        ): Boolean {
            return if (firstOfMonth == null || lastOfMonth == null || startDay == null || endDay == null) {
                true
            } else startDay > lastOfMonth || endDay < firstOfMonth
        }

        private fun horizontalMidPoint(view: View): Int {
            return view.left + view.width / 2
        }
    }

    init {
        ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    view: View,
                    accessibilityNodeInfoCompat: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat)
                    accessibilityNodeInfoCompat.setCollectionInfo(null)
                }
            })
    }
}