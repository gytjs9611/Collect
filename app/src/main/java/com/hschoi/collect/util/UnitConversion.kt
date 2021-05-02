package com.hschoi.collect.util

import android.content.Context
import android.util.TypedValue

class UnitConversion {
    companion object{
        fun dpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }
    }
}