package com.hschoi.collect.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.R
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_contents_cover_image.view.*

class CoverImageAdapter(context: Context) : RecyclerView.Adapter<CoverImageAdapter.Holder>(){

    companion object {
        private const val ITEM_WIDTH_PERCENT = 1f/3f
        private const val ITEM_HEIGHT_RATIO = 100f/120f
    }

    private var mContext = context
    var listData = ArrayList<Uri?>()  // 리스트 데이터 전달받을 변수

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contents_cover_image, parent, false)
        val itemWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, ITEM_WIDTH_PERCENT)
        val itemHeight = LayoutParamsUtils.getItemSizeByRatio(itemWidth, ITEM_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(view, itemWidth, itemHeight)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder : Holder, position : Int){
        val data = listData[position]
        Log.d("COVER", "cover image adapter - onBindViewHolder")


        if(position==itemCount-1){
            holder.bindLastItem()
        }
        else{
            holder.bindNormalItem(data!!)
        }

        if(selectedPosition == position){
            holder.itemView.cl_selected.visibility = View.VISIBLE
        }
        else{
            holder.itemView.cl_selected.visibility = View.INVISIBLE
        }


    }
    
    
    inner class Holder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        init{
            itemView.setOnClickListener {
                val pos = adapterPosition
                // + 버튼 이벤트
                if(pos==itemCount-1){
                    // 이미지 추가 코드 작성 필요
                }
                else{
                    selectedPosition = pos
                    notifyDataSetChanged()
                    
                    // cropping view에 선택한 이미지로 업데이트
                }
            }
        }


        // 마지막 + 버튼
        fun bindLastItem(){
            itemView.iv_add_icon.visibility = View.VISIBLE
            itemView.iv_cover_image.visibility = View.GONE
        }

        // 일반 이미지 버튼
        fun bindNormalItem(data : Uri){
            itemView.iv_add_icon.visibility = View.GONE
            itemView.iv_cover_image.visibility = View.VISIBLE

//            val bitmap = MediaStore.Images.Media.getBitmap(mContext.contentResolver, data)
            itemView.iv_cover_image.setImageURI(data)
        }

    }


}