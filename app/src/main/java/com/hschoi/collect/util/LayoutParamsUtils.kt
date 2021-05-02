package com.hschoi.collect.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.core.view.updateLayoutParams
import com.hschoi.collect.MainActivity
import kotlin.math.roundToInt

class LayoutParamsUtils {
    companion object{

        fun getScreenWidth(context: Context) : Int{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)

            return metrics.widthPixels
        }

        fun getScreenHeight(context: Context) : Int{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)

            return metrics.heightPixels
        }

        fun getItemWidthByPercent(context: Context, widthPercent : Float) : Int{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)

            val screenWidth = metrics.widthPixels

            return (screenWidth.toFloat() * widthPercent).roundToInt()
        }

        fun getItemHeightByPercent(context: Context, heightPercent : Float) : Int{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)

            val screenHeight = metrics.heightPixels

            return (screenHeight.toFloat() * heightPercent).roundToInt()
        }

        fun getItemSizeByRatio(size : Int, ratio : Float) : Int{

            return (size.toFloat() * ratio).roundToInt()
        }


        fun setItemSize(v : View, itemWidth : Int, itemHeight : Int){
            v.updateLayoutParams {
                width = itemWidth
                height = itemHeight

            }
        }

        fun setItemMarginStart(v : View, margin : Int){
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(margin, params.topMargin, params.rightMargin, params.bottomMargin)
        }

        fun setItemMarginTop(v : View, margin : Int){
            val params = v.layoutParams as MarginLayoutParams
            params.setMargins(params.leftMargin, margin, params.rightMargin, params.bottomMargin)
        }

        fun setItemMarginEnd(v : View, margin : Int){
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(params.leftMargin, params.topMargin, margin, params.bottomMargin)
        }

        fun setItemMarginBottom(v : View, margin : Int){
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin)
        }

        fun setItemMargins(v : View, margin : Int){
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(margin, margin, margin, margin)
        }

        fun setItemMargins(v : View, left : Int, top : Int, right : Int, bottom : Int){
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(left, top, right, bottom)
        }
    }
}