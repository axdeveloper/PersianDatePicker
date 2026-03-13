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

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.xdev.arch.persiancalendar.R
import com.xdev.arch.persiancalendar.datepicker.utils.getButtonTextColorList
import com.xdev.arch.persiancalendar.datepicker.utils.parcelable
import com.xdev.arch.persiancalendar.datepicker.utils.todayCalendar

/** A [Dialog] with a header, [MaterialCalendar], and set of actions. */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MaterialDatePicker<S> : DialogFragment() {

    val headerText: String
        get() = dateSelector.getSelectionDisplayString(requireContext())

    private val onPositiveButtonClickListeners = LinkedHashSet<(S?) -> Unit>()
    private val onNegativeButtonClickListeners = LinkedHashSet<() -> Unit>()
    private val onCancelListeners = LinkedHashSet<(dialog: DialogInterface) -> Unit>()
    private val onDismissListeners = LinkedHashSet<(dialog: DialogInterface) -> Unit>()

    private lateinit var dateSelector: DateSelector<S>
    private lateinit var pickerFragment: PickerFragment<S>
    private lateinit var calendarConstraints: CalendarConstraints
    private lateinit var calendar: MaterialCalendar<S>

    private var titleText: CharSequence? = null
    @StringRes
    private var titleTextResId = 0
    @ColorRes
    private var headerColorResId: Int = 0
    @ColorInt
    private var headerColor: Int = 0
    @ColorRes
    private var accentColorResId: Int = 0
    @ColorInt
    private var accentColor: Int = 0
    @StyleRes
    private var overrideThemeResId = 0

    private lateinit var headerSelectionText: TextView
    private lateinit var confirmButton: MaterialButton

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)

        val constraintsBuilder = CalendarConstraints.Builder(calendarConstraints)
        constraintsBuilder.setOpenAt(calendar.currentMonth.timeInMillis)

        bundle.putInt(OVERRIDE_THEME_RES_ID, overrideThemeResId)
        bundle.putParcelable(DATE_SELECTOR_KEY, dateSelector)
        bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, constraintsBuilder.build())
        bundle.putInt(TITLE_TEXT_RES_ID_KEY, titleTextResId)
        bundle.putCharSequence(TITLE_TEXT_KEY, titleText)
        bundle.putInt(HEADER_COLOR_RES_ID_KEY, headerColorResId)
        bundle.putInt(HEADER_COLOR_KEY, headerColor)
        bundle.putInt(ACCENT_COLOR_RES_ID_KEY, accentColorResId)
        bundle.putInt(ACCENT_COLOR_KEY, accentColor)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val activeBundle = bundle ?: arguments
        activeBundle?.let { args ->
            overrideThemeResId = args.getInt(OVERRIDE_THEME_RES_ID)
            dateSelector = args.parcelable(DATE_SELECTOR_KEY)!!
            calendarConstraints = args.parcelable(CALENDAR_CONSTRAINTS_KEY)!!
            titleTextResId = args.getInt(TITLE_TEXT_RES_ID_KEY)
            titleText = args.getCharSequence(TITLE_TEXT_KEY)
            headerColorResId = args.getInt(HEADER_COLOR_RES_ID_KEY)
            headerColor = args.getInt(HEADER_COLOR_KEY)
            accentColorResId = args.getInt(ACCENT_COLOR_RES_ID_KEY)
            accentColor = args.getInt(ACCENT_COLOR_KEY)
        }
    }

    private fun getThemeResId(context: Context): Int {
        return if (overrideThemeResId != 0) { overrideThemeResId }
        else dateSelector.getDefaultThemeResId(context)
    }

    override fun onCreateDialog(bundle: Bundle?) = Dialog(requireContext(), getThemeResId(requireContext()))

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?,
        bundle: Bundle?
    ): View {
        val layout = R.layout.picker_dialog
        val root = layoutInflater.inflate(layout, viewGroup)
        val context = root.context
        val pane = root.findViewById<View>(R.id.calendar_main_pane)
        val frame = root.findViewById<View>(R.id.calendar_frame)
        val header = root.findViewById<View>(R.id.picker_header)
        if (headerColorResId != 0)
            header.setBackgroundColor(ContextCompat.getColor(requireContext(), headerColorResId))
        else if (headerColor != 0)
            header.setBackgroundColor(headerColor)

        pane.layoutParams = LinearLayout.LayoutParams(getPaddedPickerWidth(context), LinearLayout.LayoutParams.MATCH_PARENT)
        frame.minimumHeight = getDialogPickerHeight(requireContext())
        headerSelectionText = root.findViewById(R.id.picker_header_selection_text)
        headerSelectionText.accessibilityLiveRegion = ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        val titleTextView = root.findViewById<AppCompatTextView>(R.id.picker_title_text)
        if (titleText != null) titleTextView.text = titleText
        else titleTextView.setText(titleTextResId)

        confirmButton = root.findViewById(R.id.confirm_button)
        confirmButton.isEnabled = dateSelector.isSelectionComplete
        confirmButton.tag = CONFIRM_BUTTON_TAG
        confirmButton.setOnClickListener {
            for (listener in onPositiveButtonClickListeners) {
                listener.invoke(selection)
            }
            dismiss()
        }

        val cancelButton =
            root.findViewById<MaterialButton>(R.id.cancel_button)
        cancelButton.tag = CANCEL_BUTTON_TAG
        cancelButton.setOnClickListener {
            onNegativeButtonClickListeners.forEach { listener -> listener.invoke() }
            dismiss()
        }

        if (accentColorResId != 0 || accentColor != 0) {
            val activeColor =
                if (accentColor != 0) accentColor
                else ContextCompat.getColor(requireContext(), accentColorResId)

            val stateList = ColorStateList.valueOf(activeColor)
            val ripple = stateList.withAlpha(36)

            confirmButton.setTextColor(getButtonTextColorList(activeColor))
            confirmButton.rippleColor = ripple
            cancelButton.setTextColor(getButtonTextColorList(activeColor))
            cancelButton.rippleColor = ripple
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        startPickerFragment()
    }

    override fun onStop() {
        pickerFragment.clearOnSelectionChangedListeners()
        super.onStop()
    }

    override fun onCancel(dialogInterface: DialogInterface) {
        onCancelListeners.forEach { listener -> listener.invoke(dialogInterface) }
        super.onCancel(dialogInterface)
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        onDismissListeners.forEach { listener -> listener.invoke(dialogInterface) }
        val viewGroup = view as ViewGroup?
        viewGroup?.removeAllViews()
        super.onDismiss(dialogInterface)
    }

    /**
     * Returns an [S] instance representing the selection or null if the user has not confirmed
     * a selection.
     */
    val selection: S?
        get() = dateSelector.getSelection()

    private fun updateHeader() {
        val headerText = headerText
        headerSelectionText.contentDescription = String.format(
            getString(R.string.picker_announce_current_selection),
            headerText
        )
        headerSelectionText.text = headerText
    }

    private fun startPickerFragment() {
        calendar = MaterialCalendar.newInstance(
            dateSelector,
            getThemeResId(requireContext()),
            calendarConstraints,
            when {
                accentColor != 0 -> accentColor
                accentColorResId != 0 -> ContextCompat.getColor(requireContext(), accentColorResId)
                else -> 0
            }
        )

        pickerFragment = calendar
        updateHeader()
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.calendar_frame, pickerFragment)
        fragmentTransaction.commitNow()
        pickerFragment.addOnSelectionChangedListener(
            object : OnSelectionChangedListener<S> {
                override fun onSelectionChanged(selection: S?) {
                    updateHeader()
                    confirmButton.isEnabled = dateSelector.isSelectionComplete
                }
            }
        )
    }

    /** The supplied listener is called when the user confirms a valid selection. */
    fun addOnPositiveButtonClickListener(listener: (selection: S?) -> Unit) = onPositiveButtonClickListeners.add(listener)

    /** Removes a listener previously added via [MaterialDatePicker.addOnPositiveButtonClickListener]. */
    fun removeOnPositiveButtonClickListener(listener: (selection: S?) -> Unit) = onPositiveButtonClickListeners.remove(listener)

    /** Removes all listeners added via [MaterialDatePicker.addOnPositiveButtonClickListener]. */
    fun clearOnPositiveButtonClickListeners() = onPositiveButtonClickListeners.clear()

    /** The supplied listener is called when the user clicks the cancel button. */
    fun addOnNegativeButtonClickListener(listener: () -> Unit) = onNegativeButtonClickListeners.add(listener)

    /** Removes a listener previously added via [MaterialDatePicker.addOnNegativeButtonClickListener]. */
    fun removeOnNegativeButtonClickListener(listener: () -> Unit) = onNegativeButtonClickListeners.remove(listener)

    /** Removes all listeners added via [MaterialDatePicker.addOnNegativeButtonClickListener]. */
    fun clearOnNegativeButtonClickListeners() = onNegativeButtonClickListeners.clear()

    /**
     * The supplied listener is called when the user cancels the picker via back button or a touch
     * outside the view. It is not called when the user clicks the cancel button. To add a listener
     * for use when the user clicks the cancel button, use [MaterialDatePicker.addOnNegativeButtonClickListener].
     */
    fun addOnCancelListener(listener: (dialog: DialogInterface) -> Unit) = onCancelListeners.add(listener)

    /** Removes a listener previously added via [MaterialDatePicker.addOnCancelListener]. */
    fun removeOnCancelListener(listener: (dialog: DialogInterface) -> Unit) = onCancelListeners.remove(listener)

    /** Removes all listeners added via [MaterialDatePicker.addOnCancelListener]. */
    fun clearOnCancelListeners() = onCancelListeners.clear()

    /** The supplied listener is called whenever the DialogFragment is dismissed, no matter how it is dismissed. */
    fun addOnDismissListener(listener: (dialog: DialogInterface) -> Unit) = onDismissListeners.add(listener)

    /** Removes a listener previously added via [MaterialDatePicker.addOnDismissListener]. */
    fun removeOnDismissListener(listener: (dialog: DialogInterface) -> Unit) = onDismissListeners.remove(listener)

    /** Removes all listeners added via [MaterialDatePicker.addOnDismissListener]. */
    fun clearOnDismissListeners() = onDismissListeners.clear()

    /** Used to create MaterialDatePicker instances with default and overridden settings  */
    open class Builder<S>(val dateSelector: DateSelector<S>) {
        var calendarConstraints: CalendarConstraints? = null
        var selection: S? = null

        var overrideThemeResId = 0
        var titleTextResId = 0
        var titleText: CharSequence? = null
        var headerColorResId: Int = 0
        var headerColor: Int = 0
        var accentColorResId: Int = 0
        var accentColor: Int = 0

        fun setSelection(selection: S): Builder<S> {
            this.selection = selection
            return this
        }

        fun setTheme(@StyleRes themeResId: Int): Builder<S> {
            overrideThemeResId = themeResId
            return this
        }

        /** Sets the first, last, and starting [Month].  */
        fun setCalendarConstraints(bounds: CalendarConstraints?): Builder<S> {
            calendarConstraints = bounds
            return this
        }

        /**
         * Sets the text used to guide the user at the top of the picker. Defaults to a standard title
         * based upon the type of selection.
         */
        fun setTitleText(@StringRes titleTextResId: Int): Builder<S> {
            this.titleTextResId = titleTextResId
            titleText = null
            return this
        }

        /**
         * Sets the text used to guide the user at the top of the picker. Setting to null will use a
         * default title based upon the type of selection.
         */
        fun setTitleText(charSequence: CharSequence?): Builder<S> {
            titleText = charSequence
            titleTextResId = 0
            return this
        }

        fun setHeaderColorRes(@ColorRes headerColor: Int): Builder<S> {
            headerColorResId = headerColor
            return this
        }

        fun setHeaderColor(@ColorInt headerColor: Int): Builder<S> {
            this.headerColor = headerColor
            return this
        }

        fun setAccentColorRes(@ColorRes accentColor: Int): Builder<S> {
            accentColorResId = accentColor
            return this
        }

        fun setAccentColor(@ColorInt accentColor: Int): Builder<S> {
            this.accentColor = accentColor
            return this
        }

        /** Creates a [MaterialDatePicker] with the provided options.  */
        fun build(): MaterialDatePicker<S> {
            if (calendarConstraints == null) calendarConstraints = CalendarConstraints.Builder().build()
            if (titleTextResId == 0) titleTextResId = dateSelector.defaultTitleResId
            if (selection != null) dateSelector.setSelection(selection!!)

            return newInstance(this)
        }

        companion object {
            /** Sets the Builder's selection manager to the provided [DateSelector]. */
            fun <S> customDatePicker(dateSelector: DateSelector<S>): Builder<S> = Builder(dateSelector)

            /** Used to create a Builder that allows for choosing a single date in the [MaterialDatePicker]. */
            fun datePicker(): Builder<Long?> = Builder(SingleDateSelector())

            /** Used to create a Builder that allows for choosing a date range in the [MaterialDatePicker]. */
            fun dateRangePicker(): Builder<Pair<Long?, Long?>> = Builder(RangeDateSelector())
        }
    }

    companion object {
        private const val OVERRIDE_THEME_RES_ID = "OVERRIDE_THEME_RES_ID"
        private const val DATE_SELECTOR_KEY = "DATE_SELECTOR_KEY"
        private const val CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY"
        private const val TITLE_TEXT_RES_ID_KEY = "TITLE_TEXT_RES_ID_KEY"
        private const val TITLE_TEXT_KEY = "TITLE_TEXT_KEY"
        private const val HEADER_COLOR_RES_ID_KEY = "HEADER_COLOR_RES_ID_KEY"
        private const val ACCENT_COLOR_RES_ID_KEY = "ACCENT_COLOR_RES_ID_KEY"
        private const val HEADER_COLOR_KEY = "HEADER_COLOR"
        private const val ACCENT_COLOR_KEY = "ACCENT_COLOR"

        val CONFIRM_BUTTON_TAG: Any = "CONFIRM_BUTTON_TAG"
        val CANCEL_BUTTON_TAG: Any = "CANCEL_BUTTON_TAG"

        inline fun<S> picker(
            selector: DateSelector<S>,
            buildPicker: CalendarBuilder<S>.() -> Unit
        ): MaterialDatePicker<S> {
            val builder = CalendarBuilder(selector)
            builder.buildPicker()
            return builder.build()
        }

        inline fun<S> Builder<S>.constraints(buildConstraints: ConstraintsBuilder.() -> Unit) {
            val builder = ConstraintsBuilder()
            builder.buildConstraints()
            setCalendarConstraints(builder.build())
        }

        /** Returns the UTC milliseconds representing the first moment of today in local timezone.  */
        fun todayInUtcMilliseconds() = todayCalendar.timeInMillis

        /** Returns the UTC milliseconds representing the first moment in current month in local timezone. */
        fun thisMonthInUtcMilliseconds() = Month.today().timeInMillis

        fun <S> newInstance(options: Builder<S>): MaterialDatePicker<S> =
                MaterialDatePicker<S>()
                    .apply {
                        arguments = Bundle().apply {
                            putInt(OVERRIDE_THEME_RES_ID, options.overrideThemeResId)
                            putParcelable(DATE_SELECTOR_KEY, options.dateSelector)
                            putParcelable(CALENDAR_CONSTRAINTS_KEY, options.calendarConstraints)
                            putInt(TITLE_TEXT_RES_ID_KEY, options.titleTextResId)
                            putCharSequence(TITLE_TEXT_KEY, options.titleText)
                            putInt(HEADER_COLOR_RES_ID_KEY, options.headerColorResId)
                            putInt(ACCENT_COLOR_RES_ID_KEY, options.accentColorResId)
                            putInt(HEADER_COLOR_KEY, options.headerColor)
                            putInt(ACCENT_COLOR_KEY, options.accentColor)
                        }
                    }

        private fun getDialogPickerHeight(context: Context): Int {
            val resources = context.resources
            val navigationHeight =
                (resources.getDimensionPixelSize(R.dimen.calendar_navigation_height)
                        + resources.getDimensionPixelOffset(R.dimen.calendar_navigation_top_padding)
                        + resources.getDimensionPixelOffset(R.dimen.calendar_navigation_bottom_padding))
            val daysOfWeekHeight =
                resources.getDimensionPixelSize(R.dimen.calendar_days_of_week_height)
            val calendarHeight: Int =
                (MonthAdapter.MAXIMUM_WEEKS
                        * resources.getDimensionPixelSize(R.dimen.calendar_day_height)
                        + (MonthAdapter.MAXIMUM_WEEKS - 1)
                        * resources.getDimensionPixelOffset(R.dimen.calendar_month_vertical_padding))
            val calendarPadding = resources.getDimensionPixelOffset(R.dimen.calendar_bottom_padding)
            return navigationHeight + daysOfWeekHeight + calendarHeight + calendarPadding
        }

        private fun getPaddedPickerWidth(context: Context): Int {
            val resources = context.resources
            val padding = resources.getDimensionPixelOffset(R.dimen.calendar_content_padding)
            val daysInWeek: Int = Month.today().daysInWeek
            val dayWidth = resources.getDimensionPixelSize(R.dimen.calendar_day_width)
            val horizontalSpace = resources.getDimensionPixelOffset(R.dimen.calendar_month_horizontal_padding)
            return 2 * padding + daysInWeek * dayWidth + (daysInWeek - 1) * horizontalSpace
        }
    }
}