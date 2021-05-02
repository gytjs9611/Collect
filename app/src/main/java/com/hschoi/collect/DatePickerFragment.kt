package com.hschoi.collect

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DatePickerFragment(private val year: Int,
                         private val month: Int,
                         private val dayOfMonth: Int) : DialogFragment(), OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(activity!!, this, year, month, dayOfMonth)
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val activity = activity as AddContentsActivity
        val c = Calendar.getInstance()
        c.set(year, month, dayOfMonth)
        val dayOfWeek = c[Calendar.DAY_OF_WEEK]
        activity.processDatePickerResult(year, month, dayOfMonth, dayOfWeek)

    }
}