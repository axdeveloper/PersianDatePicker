package com.xdev.arch.persiancalendar.datepicker

@DslMarker
internal annotation class CalendarDsl

@CalendarDsl
class CalendarBuilder<S>(dateSelector: DateSelector<S>) : MaterialDatePicker.Builder<S>(dateSelector)

@CalendarDsl
class ConstraintsBuilder : CalendarConstraints.Builder()
