package com.xdev.arch.persianlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.xdev.arch.persiancalendar.datepicker.*
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import com.xdev.arch.persiancalendar.datepicker.utils.getYearMonthDay
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val resultTextView = findViewById<TextView>(R.id.result)
        val rangeBtn = findViewById<MaterialButton>(R.id.rangeBtn)
        val pickerBtn = findViewById<MaterialButton>(R.id.pickerBtn)

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

                    resultTextView.text = "تاریخ شروع:  $first تاریخ پایان:  $second"
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

                    resultTextView.text = "تاریخ انتخاب شده:  $date"
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
