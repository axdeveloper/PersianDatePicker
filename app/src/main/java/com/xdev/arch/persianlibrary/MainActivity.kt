package com.xdev.arch.persianlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xdev.arch.persiancalendar.datepicker.CalendarConstraints
import com.xdev.arch.persiancalendar.datepicker.*
import com.xdev.arch.persiancalendar.datepicker.calendar.PersianCalendar
import com.xdev.arch.persianlibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = PersianCalendar()
        calendar.setPersian(1388, Month.FARVARDIN, 1)

        val startDate = calendar.timeInMillis

        calendar.setPersian(1410, Month.ESFAND, 29)
        val endDate = calendar.timeInMillis

        val todayDate = PersianCalendar.getToday().timeInMillis

        val constraints = CalendarConstraints.constraints {
            start = startDate
            end = endDate
            openAt = todayDate
            validator = DateValidatorPointForward.from(startDate)
        }

        val rangePicker = MaterialDatePicker.picker(RangeDateSelector()) {
            selection = Pair(todayDate, todayDate)
            titleText = "محدوده را انتخاب کنید."
            calendarConstraints = constraints
        }

        rangePicker.addOnPositiveButtonClickListener { selection ->
            val selectedStart = PersianCalendar(selection!!.first!!)
            val selectedEnd = PersianCalendar(selection.second!!)
            binding.result.text = "Start: $selectedStart - End: $selectedEnd"
        }

        val datePicker =
            MaterialDatePicker.picker(SingleDateSelector()) {
                titleText = "تاریخ را انتخاب کنید."
                calendarConstraints = constraints
            }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = PersianCalendar(selection!!)
            binding.result.text = "تاریخ انتخاب شده: $date"
        }

        binding.rangeBtn.setOnClickListener {
            if (!rangePicker.isVisible && !rangePicker.isAdded)
                rangePicker.show(supportFragmentManager, "RangePickerTag")
        }

        binding.pickerBtn.setOnClickListener {
            if (!datePicker.isVisible && !datePicker.isAdded)
                datePicker.show(supportFragmentManager, "DatePickerTag")
        }
    }
}
