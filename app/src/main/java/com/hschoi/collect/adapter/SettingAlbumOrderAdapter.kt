package com.hschoi.collect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.*
import com.hschoi.collect.util.ColorUtils
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_album_list.view.*

class SettingAlbumOrderAdapter(var items: ArrayList<Albums>) : RecyclerView.Adapter<SettingAlbumOrderAdapter.SettingAlbumViewHolder>() {
    private lateinit var mContext : Context


    // 1. 홀더의 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingAlbumViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_album_list, parent, false)
        mContext = parent.context

        // set view size
        val vHeight = LayoutParamsUtils.getItemHeightByPercent(mContext, 52f/716f)
        val vWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, 328f/360f)
        LayoutParamsUtils.setItemSize(v, vWidth, vHeight)


        return SettingAlbumViewHolder(v)
    }

    // 2. 홀더의 바인딩
    override fun onBindViewHolder(holder: SettingAlbumViewHolder, position: Int) {

        if(items[position].id!=(-1).toLong()){
            holder.bindNormal(items[position], position)

            val albumId = items[position].id
            val albumTitle = items[position].title
            val frameType = items[position].frameType
            val color = items[position].color

            holder.itemView.setOnClickListener {

            }
        }




    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(): ArrayList<Albums>{
        return items
    }


    inner class SettingAlbumViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        fun bindNormal(album : Albums, position : Int){
            val albumColor = album.color
            val albumTitle = album.title

            itemView.tv_album_list_title.text = albumTitle
            itemView.cv_album_list_item.setCardBackgroundColor(ColorUtils.getAlbumColor(mContext, albumColor))
            itemView.tv_album_list_title.setTextColor(mContext.getColorStateList(R.color.white))
        }
    }

}