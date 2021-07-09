package com.hschoi.collect.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.*
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.util.ColorUtils
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_album_list.view.*
import java.util.*
import kotlin.collections.ArrayList

class SettingAlbumOrderAdapter(var items: ArrayList<Albums>,
                               var startDragListener : OnStartDragListener,
                               var db: AlbumDatabase?) : RecyclerView.Adapter<SettingAlbumOrderAdapter.SettingAlbumViewHolder>(),
                                                                SettingAlbumItemTouchHelperCallback.Listener{
    private lateinit var mContext : Context

    interface OnStartDragListener{
        fun onStartDrag(holder: SettingAlbumViewHolder)
    }

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

            holder.itemView.setOnTouchListener { _, event->
                if(event.action==MotionEvent.ACTION_DOWN){
                    startDragListener.onStartDrag(holder)
                }
                return@setOnTouchListener true
            }

        }

    }


    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(): ArrayList<Albums>{
        return items
    }


    private fun swapAlbumList(fromPosition: Int, toPosition: Int){
        Collections.swap(MainActivity.albumList, fromPosition, toPosition)
        MainActivity.homeRecyclerAdapter.notifyDataSetChanged()
        MainActivity.addContentsRecyclerAdapter.notifyDataSetChanged()
    }

    private fun swapDBId(fromPosition: Int, toPosition: Int){
        val id1 = items[fromPosition].id
        val id2 = items[toPosition].id
        val order1 = items[fromPosition].order
        val order2 = items[toPosition].order


        val run = Runnable {
            db?.albumDao()?.setOrder(id1, order2)
            db?.albumDao()?.setOrder(id2, order1)
            Log.d("ddd", "set id${id1} order${order2}, id${id2} order${order1}")
        }


        val thread = Thread(run)
        thread.start()
    }

    private fun swapAlbumListId(fromPosition: Int, toPosition: Int){
        val order1 = items[fromPosition].order
        val order2 = items[toPosition].order
        items[fromPosition].order = order2
        items[toPosition].order = order1
    }


    inner class SettingAlbumViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        fun bindNormal(album : Albums, position : Int){
            val albumColor = album.color
            val albumTitle = album.title

            itemView.tv_album_list_title.text = albumTitle
            itemView.cv_album_list_item.setCardBackgroundColor(ColorUtils.getAlbumColor(mContext, albumColor))
            itemView.tv_album_list_title.setTextColor(mContext.getColorStateList(R.color.white))

            itemView.iv_move.visibility = View.VISIBLE

        }
    }


    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if(fromPosition<toPosition){
            for(i in fromPosition until toPosition){
                Collections.swap(items, i, i+1)
            }
        }
        else{
            for(i in fromPosition downTo toPosition+1){
                Collections.swap(items, i, i-1)
            }
        }

        swapAlbumList(fromPosition, toPosition)
        swapDBId(fromPosition, toPosition)
        swapAlbumListId(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)

    }

    override fun onRowSelected(itemViewHolder: SettingAlbumViewHolder) {

    }

    override fun onRowClear(itemViewHolder: SettingAlbumViewHolder) {

    }






}