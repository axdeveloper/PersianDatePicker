package com.xdev.arch.persianlibrary

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
        calendar.setPersian(1342, Month.FARVARDIN, 1)

        val constraints = CalendarConstraints.Builder()
        constraints.setStart(calendar.timeInMillis)

        calendar.setPersian(1420, Month.ESFAND, 29)
        constraints.setEnd(calendar.timeInMillis)

        constraints.setOpenAt(PersianCalendar.getToday().timeInMillis)
        constraints.setValidator(DateValidatorPointForward.now())

        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setCalendarConstraints(constraints.build()).setTitleText("تاریخ").build()

        button.setOnClickListener {
            datePicker.show(supportFragmentManager, "TAG")
        }

        datePicker.addOnPositiveButtonClickListener(
            object : MaterialPickerOnPositiveButtonClickListener<Pair<Long?, Long?>> {
                override fun onPositiveButtonClick(selection: Pair<Long?, Long?>) {
                    if (selection.first == null || selection.second == null) return
                    val cal1 = PersianCalendar(selection.first!!)
                    val sec2 = PersianCalendar(selection.second!!)
                    button.text = "selected range: $cal1 to $sec2$"
                }
            })
    }
}
