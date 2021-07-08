package com.hschoi.collect

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_modify_album_order.*
import kotlinx.android.synthetic.main.activity_modify_album_order.layout_bottom_menu_bar
import kotlinx.android.synthetic.main.layout_bottom_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*

class ModifyAlbumOrderActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_album_order)

        initView()
        setClickListeners()



    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }


    private fun setClickListeners(){
        layout_top_menu_modify_album_order.cl_icon_left.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun initView(){
        layout_top_menu_modify_album_order.apply {
            setBackgroundColor(getColor(R.color.white))
            tv_album_name_title.text = getString(R.string.setting_change_album_order)
            tv_album_name_title.setTextColor(getColor(R.color.setting_item_color))
            iv_icon_left.setImageDrawable(getDrawable(R.drawable.ic_back_black))
            iv_icon_right.visibility = View.INVISIBLE

        }



        layout_bottom_menu_bar.iv_menu_setting.imageTintList = ColorStateList.valueOf(getColor(R.color.black))

    }


}