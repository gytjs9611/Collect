package com.hschoi.collect

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.adapter.SettingAlbumOrderAdapter
import com.hschoi.collect.database.AlbumDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_modify_album_order.*
import kotlinx.android.synthetic.main.activity_modify_album_order.layout_bottom_menu_bar
import kotlinx.android.synthetic.main.layout_bottom_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*

class ModifyAlbumOrderActivity: AppCompatActivity(), SettingAlbumOrderAdapter.OnStartDragListener {

    private lateinit var settingAlbumOrderAdapter : SettingAlbumOrderAdapter
    private lateinit var mItemTouchHelper : ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_album_order)

        initView()
        setClickListeners()


        // set array
        val albumList = ArrayList<Albums>()
        albumList.addAll(MainActivity.albumList)    // 깊은 복사를 통해 Main의 album list 에 영향 주지 않도록 함
        albumList.removeAt(albumList.size-1)

        // set adapter, touch helper
        val db = AlbumDatabase.getInstance(this)
        settingAlbumOrderAdapter = SettingAlbumOrderAdapter(albumList, this, AlbumDatabase.getInstance(this))
        val mCallback = SettingAlbumItemTouchHelperCallback(settingAlbumOrderAdapter)
        mItemTouchHelper = ItemTouchHelper(mCallback)
        mItemTouchHelper.attachToRecyclerView(rv_setting_album_order)

        // set recycler view
        rv_setting_album_order.adapter = settingAlbumOrderAdapter
        rv_setting_album_order.layoutManager = GridLayoutManager(applicationContext, 1, RecyclerView.VERTICAL, false)
        val decoration = AddContentsRecyclerDecoration(applicationContext)
        rv_setting_album_order.addItemDecoration(decoration)

    }


    override fun onStartDrag(holder: SettingAlbumOrderAdapter.SettingAlbumViewHolder) {
        mItemTouchHelper.startDrag(holder)
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