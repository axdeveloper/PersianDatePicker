package com.xdev.arch.persianlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xdev.arch.persiancalendar.datepicker.*
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calendar = PersianCalendar()
        calendar.setPersian(1340, Month.FARVARDIN, 1)

        val start = calendar.timeInMillis

        calendar.setPersian(1409, Month.ESFAND, 29)
        val end = calendar.timeInMillis

        val openAt = PersianCalendar.getToday().timeInMillis

        val constraints = CalendarConstraints.Builder()
            .setStart(start)
            .setEnd(end)
            .setOpenAt(openAt)
            .setValidator(DateValidatorPointForward.from(start)).build()

        val rangePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTitleText("محدوده را انتخاب کنید.")
            .setCalendarConstraints(constraints).build()

        rangePicker.addOnPositiveButtonClickListener(
            object : MaterialPickerOnPositiveButtonClickListener<Pair<Long?, Long?>> {
                @SuppressLint("SetTextI18n")
                override fun onPositiveButtonClick(selection: Pair<Long?, Long?>) {
                    val first = PersianCalendar(selection.first!!)
                    val second = PersianCalendar(selection.second!!)

                    result.text = "تاریخ شروع:‌ $first تاریخ پایان:‌ $second"
                }
            }
        )


        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText("تاریخ را انتخاب کنید.")
            .setCalendarConstraints(constraints).build()

        datePicker.addOnPositiveButtonClickListener(
            object : MaterialPickerOnPositiveButtonClickListener<Long?> {
                @SuppressLint("SetTextI18n")
                override fun onPositiveButtonClick(selection: Long?) {
                    val date = PersianCalendar(selection!!)

                    result.text = "تاریخ انتخاب شده:‌ $date"
                }
            }
        )

        rangeBtn.setOnClickListener {
            rangePicker.show(supportFragmentManager, "RangePickerTag")
        }

        pickerBtn.setOnClickListener {
            datePicker.show(supportFragmentManager, "DatePickerTag")
        }
    }
}
