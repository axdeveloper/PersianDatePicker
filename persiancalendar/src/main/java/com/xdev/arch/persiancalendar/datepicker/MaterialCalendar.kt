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
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.GridView
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.iranCalendar
import kotlin.math.abs

/**
 * Fragment for a days of week [com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar]
 * represented as a header row of days labels and
 * [GridView] of days backed by [MonthAdapter].
 */
class MaterialCalendar<S> : PickerFragment<S>() {

    /** The views supported by [MaterialCalendar] */
    enum class CalendarSelector { DAY, YEAR }

    private var themeResId = 0

    override var dateSelector: DateSelector<S>? = null

    private lateinit var current: Month
    private lateinit var calendarSelector: CalendarSelector

    lateinit var calendarConstraints: CalendarConstraints
    lateinit var calendarStyle: CalendarStyle

    private var yearSelector: RecyclerView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var yearFrame: View
    private lateinit var dayFrame: View

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(THEME_RES_ID_KEY, themeResId)
        bundle.putParcelable(GRID_SELECTOR_KEY, dateSelector)
        bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints)
        bundle.putParcelable(CURRENT_MONTH_KEY, current)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val activeBundle = (bundle ?: arguments) as Bundle
        themeResId = activeBundle.getInt(THEME_RES_ID_KEY)
        dateSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY)
        calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY)!!
        current = activeBundle.getParcelable(CURRENT_MONTH_KEY)!!
    }

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?,
        bundle: Bundle?
    ): View {
        val themedContext =
            ContextThemeWrapper(context, themeResId)
        calendarStyle = CalendarStyle(themedContext)
        val themedInflater = layoutInflater.cloneInContext(themedContext)
        val earliestMonth = calendarConstraints.start
        val orientation: Int = LinearLayoutManager.HORIZONTAL
        val root = themedInflater.inflate(R.layout.calendar_horizontal, viewGroup, false)

        val daysHeader = root.findViewById<GridView>(R.id.calendar_days_of_week)

        ViewCompat.setAccessibilityDelegate(
            daysHeader,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    view: View,
                    accessibilityNodeInfoCompat: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat)
                    accessibilityNodeInfoCompat.setCollectionInfo(null)
                }
            })

        daysHeader.adapter = DaysOfWeekAdapter()
        daysHeader.numColumns = earliestMonth.daysInWeek
        daysHeader.isEnabled = false
        recyclerView = root.findViewById(R.id.calendar_months)
        val layoutManager: SmoothCalendarLayoutManager =
            object : SmoothCalendarLayoutManager(
                context,
                orientation,
                true
            ) {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    ints: IntArray
                ) {
                    ints[0] = recyclerView.width
                    ints[1] = recyclerView.width
                }
            }

        recyclerView.layoutManager = layoutManager
        recyclerView.tag = MONTHS_VIEW_GROUP_TAG
        val monthsPagerAdapter =
            MonthsPagerAdapter(
                themedContext,
                dateSelector,
                calendarConstraints,
                object :
                    OnDayClickListener {
                    override fun onDayClick(day: Long) {
                        if (calendarConstraints.dateValidator.isValid(day)) {
                            dateSelector?.select(day)
                            for (listener in onSelectionChangedListeners) {
                                listener.onSelectionChanged(dateSelector?.getSelection())
                            }

                            recyclerView.adapter?.notifyDataSetChanged()
                            yearSelector?.let { it.adapter?.notifyDataSetChanged() }
                        }
                    }
                })
        recyclerView.adapter = monthsPagerAdapter
        val columns = themedContext.resources.getInteger(R.integer.calendar_year_selector_span)
        yearSelector = root.findViewById(R.id.calendar_year_selector_frame)

        yearSelector?.let { yearSelector ->
            yearSelector.setHasFixedSize(true)
            yearSelector.layoutManager =
                RtlGridLayoutManager(themedContext, columns, RecyclerView.VERTICAL, false)
            yearSelector.adapter = YearGridAdapter(this)
            yearSelector.addItemDecoration(createItemDecoration())
        }

        if (root.findViewById<View?>(R.id.month_navigation_fragment_toggle) != null)
            addActionsToMonthNavigation(root, monthsPagerAdapter)

        LinearSnapHelper().attachToRecyclerView(recyclerView)

        recyclerView.scrollToPosition(monthsPagerAdapter.getPosition(current))

        return root
    }

    private fun createItemDecoration(): ItemDecoration {
        return object : ItemDecoration() {
            private val startItem = iranCalendar
            private val endItem = iranCalendar

            override fun onDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                state: RecyclerView.State
            ) {
                if (recyclerView.adapter !is YearGridAdapter || recyclerView.layoutManager !is GridLayoutManager)
                    return

                val adapter = recyclerView.adapter as YearGridAdapter
                val layoutManager = recyclerView.layoutManager as GridLayoutManager

                dateSelector?.let { dateSelector ->
                    for (range in dateSelector.selectedRanges) {

                        if (range.first == null || range.second == null) {
                            continue
                        }

                        startItem.timeInMillis = range.first!!
                        endItem.timeInMillis = range.second!!

                        val firstHighlightPosition =
                            adapter.getPositionForYear(startItem.year)
                        val lastHighlightPosition =
                            adapter.getPositionForYear(endItem.year)
                        val firstView = layoutManager.findViewByPosition(firstHighlightPosition)
                        val lastView = layoutManager.findViewByPosition(lastHighlightPosition)
                        val firstRow = firstHighlightPosition / layoutManager.spanCount
                        val lastRow = lastHighlightPosition / layoutManager.spanCount
                        for (row in firstRow..lastRow) {
                            val firstPositionInRow = row * layoutManager.spanCount
                            val viewInRow =
                                layoutManager.findViewByPosition(firstPositionInRow)
                                    ?: continue
                            val top = viewInRow.top + calendarStyle.year.topInset
                            val bottom = viewInRow.bottom - calendarStyle.year.bottomInset

                            val left =
                                if (row == firstRow) firstView!!.left + firstView.width / 2 else 0
                            val right =
                                if (row == lastRow) lastView!!.left + lastView.width / 2 else recyclerView.width
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
        }
    }

    /**
     * Changes the currently displayed [Month] to `moveTo`.
     *
     * @throws IllegalArgumentException If `moveTo` is not within the allowed [     ].
     */
    var currentMonth: Month
        get() = current
        set(moveTo) {
            val adapter = recyclerView.adapter as MonthsPagerAdapter
            val moveToPosition = adapter.getPosition(moveTo)
            val distance = moveToPosition - adapter.getPosition(current)
            val jump = abs(distance) > SMOOTH_SCROLL_MAX
            val isForward = distance > 0
            current = moveTo
            if (jump && isForward) {
                recyclerView.scrollToPosition(moveToPosition - SMOOTH_SCROLL_MAX)
                postSmoothRecyclerViewScroll(moveToPosition)
            } else if (jump) {
                recyclerView.scrollToPosition(moveToPosition + SMOOTH_SCROLL_MAX)
                postSmoothRecyclerViewScroll(moveToPosition)
            } else {
                postSmoothRecyclerViewScroll(moveToPosition)
            }
        }

    internal interface OnDayClickListener {
        fun onDayClick(day: Long)
    }

    fun setSelector(selector: CalendarSelector) {
        calendarSelector = selector
        if (selector == CalendarSelector.YEAR) {

            yearSelector?.let {
                it.layoutManager?.scrollToPosition(
                    (it.adapter as YearGridAdapter).getPositionForYear(
                        current.year
                    )
                )
            }

            yearFrame.visibility = View.VISIBLE
            dayFrame.visibility = View.GONE
        } else if (selector == CalendarSelector.DAY) {
            yearFrame.visibility = View.GONE
            dayFrame.visibility = View.VISIBLE
            // When visibility is toggled, the RecyclerView default opens to its lowest available id.
            // This id is always one month earlier than current, so we force it to current.
            currentMonth = current
        }
    }

    private fun toggleVisibleSelector() {
        if (calendarSelector == CalendarSelector.YEAR) {
            setSelector(CalendarSelector.DAY)
        } else if (calendarSelector == CalendarSelector.DAY) {
            setSelector(CalendarSelector.YEAR)
        }
    }

    private fun addActionsToMonthNavigation(root: View, monthsPagerAdapter: MonthsPagerAdapter) {
        val monthDropSelect: AppCompatTextView = root.findViewById(R.id.month_navigation_fragment_toggle)
        monthDropSelect.tag = SELECTOR_TOGGLE_TAG
        val monthPrev: AppCompatImageButton = root.findViewById(R.id.month_navigation_previous)
        monthPrev.tag = NAVIGATION_PREV_TAG
        val monthNext: AppCompatImageButton = root.findViewById(R.id.month_navigation_next)
        monthNext.tag = NAVIGATION_NEXT_TAG
        yearFrame = root.findViewById(R.id.calendar_year_selector_frame)
        dayFrame = root.findViewById(R.id.calendar_day_selector_frame)
        setSelector(CalendarSelector.DAY)
        monthDropSelect.text = current.longName
        recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    val currentItem: Int = if (dx < 0) {
                        layoutManager.findFirstVisibleItemPosition()
                    } else {
                        layoutManager.findLastVisibleItemPosition()
                    }
                    current = monthsPagerAdapter.getPageMonth(currentItem)
                    monthDropSelect.text = monthsPagerAdapter.getPageTitle(currentItem)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE)
                        recyclerView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                }
            })

        monthDropSelect.setOnClickListener { toggleVisibleSelector() }
        monthNext.setOnClickListener {
            val currentItem: Int = layoutManager.findLastVisibleItemPosition()
            if (currentItem - 1 >= 0) {
                currentMonth = monthsPagerAdapter.getPageMonth(currentItem - 1)
            }
        }
        monthPrev.setOnClickListener {
            val currentItem: Int = layoutManager.findFirstVisibleItemPosition()
            if (currentItem + 1 < recyclerView.adapter!!.itemCount) {
                currentMonth = monthsPagerAdapter.getPageMonth(currentItem + 1)
            }
        }
    }

    private fun postSmoothRecyclerViewScroll(position: Int) {
        recyclerView.post { recyclerView.smoothScrollToPosition(position) }
    }

    val layoutManager: LinearLayoutManager
        get() = recyclerView.layoutManager as LinearLayoutManager

    companion object {
        private const val THEME_RES_ID_KEY = "THEME_RES_ID_KEY"
        private const val GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY"
        private const val CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY"
        private const val CURRENT_MONTH_KEY = "CURRENT_MONTH_KEY"
        private const val SMOOTH_SCROLL_MAX = 3
        @VisibleForTesting
        val MONTHS_VIEW_GROUP_TAG: Any = "MONTHS_VIEW_GROUP_TAG"
        @VisibleForTesting
        val NAVIGATION_PREV_TAG: Any = "NAVIGATION_PREV_TAG"
        @VisibleForTesting
        val NAVIGATION_NEXT_TAG: Any = "NAVIGATION_NEXT_TAG"
        @VisibleForTesting
        val SELECTOR_TOGGLE_TAG: Any = "SELECTOR_TOGGLE_TAG"

        fun <T> newInstance(
            dateSelector: DateSelector<T>?,
            themeResId: Int,
            calendarConstraints: CalendarConstraints
        ): MaterialCalendar<T> {
            val materialCalendar =
                MaterialCalendar<T>()
            val args = Bundle()
            args.putInt(
                THEME_RES_ID_KEY,
                themeResId
            )
            args.putParcelable(
                GRID_SELECTOR_KEY,
                dateSelector
            )
            args.putParcelable(
                CALENDAR_CONSTRAINTS_KEY,
                calendarConstraints
            )
            args.putParcelable(
                CURRENT_MONTH_KEY,
                calendarConstraints.openAt
            )
            materialCalendar.arguments = args
            return materialCalendar
        }

        /** Returns the pixel height of each [View] representing a day.  */
        @Px
        fun getDayHeight(context: Context): Int {
            return context.resources.getDimensionPixelSize(R.dimen.calendar_day_height)
        }
    }
}