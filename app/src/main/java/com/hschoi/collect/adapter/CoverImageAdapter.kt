package com.hschoi.collect.adapter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.AddContentsActivity
import com.hschoi.collect.AddContentsCoverActivity
import com.hschoi.collect.R
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_contents_cover_image.view.*

class CoverImageAdapter(var items: ArrayList<String>) : RecyclerView.Adapter<CoverImageAdapter.Holder>(){

    companion object {
        private const val ITEM_WIDTH_PERCENT = 1f/3f
        private const val ITEM_HEIGHT_RATIO = 100f/120f
    }

    private lateinit var mContext: Context

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        mContext = parent.context

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contents_cover_image, parent, false)
        val itemWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, ITEM_WIDTH_PERCENT)
        val itemHeight = LayoutParamsUtils.getItemSizeByRatio(itemWidth, ITEM_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(view, itemWidth, itemHeight)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder : Holder, position : Int){
        Log.d("COVER", "cover image adapter - onBindViewHolder")

        if(position==itemCount-1){
            holder.bindLastItem()
        }
        else{
            val data = items[position]
            holder.bindNormalItem(data)
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
                    if (ContextCompat.checkSelfPermission(mContext,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(mContext as Activity,
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // First time
                            ActivityCompat.requestPermissions(mContext as Activity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    AddContentsActivity.REQ_STORAGE_PERMISSION
                            )
                        } else {
                            // Not first time
                            Toast.makeText(mContext, "기기 설정에서 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Permission has already been granted
                        AddContentsActivity.openImagePicker()
                        items = AddContentsActivity.imageList
                        notifyDataSetChanged()
                    }
                }
                else if(selectedPosition!=pos){
                    selectedPosition = pos

                    val bitmap = BitmapFactory.decodeFile("${mContext.filesDir}/${items[pos]}")

                    AddContentsCoverActivity.imageCroppingView.initView(mContext)
                    AddContentsCoverActivity.imageCroppingView.setImageBitmap(bitmap)
                    notifyDataSetChanged()
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

        fun bindNormalItem(fileName : String){
            itemView.iv_add_icon.visibility = View.GONE
            itemView.iv_cover_image.visibility = View.VISIBLE

            val fis = mContext.openFileInput(fileName)
            val bitmap = BitmapFactory.decodeStream(fis)
            itemView.iv_cover_image.setImageBitmap(bitmap)
        }

    }


}