package com.hschoi.collect.util

import android.content.Context
import android.content.res.Resources
import android.provider.Settings.Global.getString
import com.hschoi.collect.R
import java.util.*

class DateUtils {
    companion object{
        fun getDayOfWeekString(context:Context, value: Int): String{
            return when(value){
                Calendar.SUNDAY->context.getString(R.string.sunday)
                Calendar.MONDAY->context.getString(R.string.monday)
                Calendar.TUESDAY->context.getString(R.string.tuesday)
                Calendar.WEDNESDAY->context.getString(R.string.wednesday)
                Calendar.THURSDAY->context.getString(R.string.thursday)
                Calendar.FRIDAY->context.getString(R.string.friday)
                Calendar.SATURDAY->context.getString(R.string.saturday)
                else->context.getString(R.string.friday)
            }
        }

        fun getDayOfWeekFromDate(context: Context, data: String): String{
            val date = data.split(".")
            val c = Calendar.getInstance()
            // calendar에서는 month가 0부터 시작이기 때문에 -1 해줘야 함
            c.set(date[0].toInt(), date[1].toInt()-1, date[2].toInt())
            return getDayOfWeekString(context, c[Calendar.DAY_OF_WEEK])
        }
    }

}