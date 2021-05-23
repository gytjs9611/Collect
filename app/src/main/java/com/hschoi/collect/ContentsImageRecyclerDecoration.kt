package com.hschoi.collect

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.UnitConversion

class ContentsImageRecyclerDecoration(context : Context) : RecyclerView.ItemDecoration() {
    private val mContext = context
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        val addButtonSize = LayoutParamsUtils.getItemWidthByPercent(mContext, AddContentsActivity.ADD_BUTTON_WIDTH_RATIO)
        val imageWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, AddContentsActivity.IMAGE_WIDTH)
        val imageHeight = LayoutParamsUtils.getItemSizeByRatio(imageWidth, AddContentsActivity.ADD_IMAGE_BACK_HEIGHT_RATIO)

        val imageLeftMargin = LayoutParamsUtils.getItemWidthByPercent(mContext, AddContentsActivity.IMAGE_SIDE_MARGIN)

        val addButtonSideMargin = LayoutParamsUtils.getItemWidthByPercent(mContext, AddContentsActivity.IMAGE_ADD_BUTTON_SIDE_MARGIN)
        val addButtonTopMargin = (imageHeight-addButtonSize)/2


        if(position == AddContentsActivity.imageList.size-1){
            outRect.top = addButtonTopMargin
            outRect.left = addButtonSideMargin
            outRect.right = addButtonSideMargin
        }
        else{
            outRect.left = if(position==0) imageLeftMargin/2 else imageLeftMargin
        }

    }

}