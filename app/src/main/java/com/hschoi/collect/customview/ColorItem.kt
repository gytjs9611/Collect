package com.hschoi.collect.customview

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.hschoi.collect.R
import kotlinx.android.synthetic.main.layout_album_color_icon.view.*


class ColorItem : ConstraintLayout {
    private var mColor = 0

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs) {
        initView()
    }

    private fun initView(){
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = context.getSystemService(infService) as LayoutInflater
        val v = li.inflate(R.layout.layout_album_color_icon, this, false)
        addView(v)

        if(mColor==0){
            iv_color_icon.setColorFilter(resources.getColor(R.color.album_color_pink), PorterDuff.Mode.SRC_IN)
        }
        else{
            iv_color_icon.setColorFilter(mColor, PorterDuff.Mode.SRC_IN)
        }

    }




    fun setColor(color : Int){
        iv_color_icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun setFocused(){
        iv_color_icon_check.visibility = View.VISIBLE
    }

    fun setUnfocused(){
        iv_color_icon_check.visibility = View.GONE
    }

}