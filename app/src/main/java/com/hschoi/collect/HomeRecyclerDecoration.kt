package com.hschoi.collect

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.UnitConversion

class HomeRecyclerDecoration(context : Context) : RecyclerView.ItemDecoration() {
    private val mContext = context
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val topMargin = LayoutParamsUtils.getItemHeightByPercent(mContext, 0.103f)

        outRect.top = topMargin

        if(position == 0){
            outRect.left = UnitConversion.dpToPx(mContext, 26f).toInt()
        }


        if(position == parent.adapter!!.itemCount -1){
            outRect.right = UnitConversion.dpToPx(mContext, 26f).toInt()
        }
        else{
            outRect.right = UnitConversion.dpToPx(mContext, 20f).toInt()
        }



    }

}