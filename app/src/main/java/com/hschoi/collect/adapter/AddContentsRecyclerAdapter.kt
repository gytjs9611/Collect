package com.hschoi.collect.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.*
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_album_list.view.*


class AddContentsRecyclerAdapter(var items: ArrayList<Albums>) : RecyclerView.Adapter<AddContentsRecyclerAdapter.HomeViewHolder>() {
    private lateinit var mContext : Context


    // 1. 홀더의 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        Log.d("TAG", "onCreateViewHolder")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_album_list, parent, false)
        mContext = parent.context

        // set view size
        val vHeight = LayoutParamsUtils.getItemHeightByPercent(mContext, 52f/716f)
        val vWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, 328f/360f)
        LayoutParamsUtils.setItemSize(v, vWidth, vHeight)

        return HomeViewHolder(v)
    }

    // 2. 홀더의 바인딩
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        if(items[position].title==""){
            holder.bindLastItem()
            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, CreateNewAlbumActivity::class.java)
                mContext.startActivity(intent)
            }
        }
        else{
            holder.bindNormal(items[position], position)
            val albumId = items[position].id
            val albumTitle = items[position].title
            val frameType = items[position].frameType
            val color = items[position].color

            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, AddContentsActivity::class.java)
                intent.putExtra("albumId", albumId)
                intent.putExtra("albumTitle", albumTitle)
                intent.putExtra("frameType", frameType)
                intent.putExtra("color", color)

                mContext.startActivity(intent)
                MainActivity.isAddFromHome = true
            }


        }



    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(): ArrayList<Albums>{
        return items
    }

    inner class HomeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var index : Int? = null

        fun bindLastItem(){
            itemView.tv_album_list_title.text = mContext.getString(R.string.create_new_album)
            itemView.cv_album_list_item.setCardBackgroundColor(mContext.getColor(R.color.album_color_add_new))
            itemView.tv_album_list_title.setTextColor(mContext.getColorStateList(R.color.text_color_add_new))
        }

        fun bindNormal(album : Albums, position : Int){
            val albumColor = album.color
            val albumTitle = album.title

            index = position
            itemView.tv_album_list_title.text = albumTitle
            itemView.cv_album_list_item.setCardBackgroundColor(albumColor)
            itemView.tv_album_list_title.setTextColor(mContext.getColorStateList(R.color.white))
        }
    }

}