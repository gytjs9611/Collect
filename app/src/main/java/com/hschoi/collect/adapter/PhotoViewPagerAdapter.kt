package com.hschoi.collect.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.R
import kotlinx.android.synthetic.main.item_view_pager.view.*
import kotlin.coroutines.coroutineContext

class PhotoViewPagerAdapter(var items : ArrayList<String>) : RecyclerView.Adapter<PhotoViewPagerAdapter.PhotoViewHolder>(){
    private lateinit var mContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        mContext = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return PhotoViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(position)
    }



    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(position: Int){
            val bitmap = BitmapFactory.decodeFile("${mContext.filesDir}/${items[position]}")
            itemView.iv_view_pager_item.setImageBitmap(bitmap)
        }
    }

}
