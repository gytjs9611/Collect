package com.hschoi.collect

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.UnitConversion

class AddContentsRecyclerDecoration(context : Context) : RecyclerView.ItemDecoration() {
    private val mContext = context
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val topMargin = LayoutParamsUtils.getItemHeightByPercent(mContext, 16f/716f)
        val interval = LayoutParamsUtils.getItemHeightByPercent(mContext, 10f/716f)
        val leftMargin = LayoutParamsUtils.getItemWidthByPercent(mContext, 16f/360f)

        outRect.left =leftMargin

        if(position == 0){
            outRect.top = topMargin
        }
        else{
            outRect.top = interval
            if(position==parent.adapter!!.itemCount-1){
                outRect.bottom = topMargin
            }
        }


    }

}