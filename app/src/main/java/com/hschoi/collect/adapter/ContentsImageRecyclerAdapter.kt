package com.hschoi.collect.adapter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.AddContentsActivity
import com.hschoi.collect.AddContentsActivity.Companion.ADD_IMAGE_BACK_HEIGHT_RATIO
import com.hschoi.collect.AddContentsActivity.Companion.IMAGE_ADD_BUTTON_SIDE_MARGIN
import com.hschoi.collect.AddContentsActivity.Companion.IMAGE_SIDE_MARGIN
import com.hschoi.collect.AddContentsActivity.Companion.IMAGE_TOP_MARGIN_RATIO
import com.hschoi.collect.AddContentsActivity.Companion.IMAGE_WIDTH
import com.hschoi.collect.AddContentsActivity.Companion.REQ_STORAGE_PERMISSION
import com.hschoi.collect.CreateNewAlbumActivity
import com.hschoi.collect.R
import com.hschoi.collect.util.LayoutParamsUtils
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_create_new_album.*
import kotlinx.android.synthetic.main.layout_add_contents_image.view.*

class ContentsImageRecyclerAdapter(var items: ArrayList<String>): RecyclerView.Adapter<ContentsImageRecyclerAdapter.ContentsImageViewHolder>() {
    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentsImageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_add_contents_image, parent, false)
        mContext = parent.context

        // set view size
        val imageWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, IMAGE_WIDTH)
        val imageHeight = LayoutParamsUtils.getItemSizeByRatio(imageWidth, ADD_IMAGE_BACK_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(v, imageWidth, imageHeight)

        return ContentsImageViewHolder(v)
    }

    override fun onBindViewHolder(holder: ContentsImageViewHolder, position: Int) {
        if(items[position]==""){    // dummy
            holder.bindLastItem()

            holder.itemView.setOnClickListener {
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mContext as Activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // First time
                        ActivityCompat.requestPermissions(mContext as Activity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQ_STORAGE_PERMISSION
                        )
                    } else {
                        // Not first time
                        Toast.makeText(mContext, "기기 설정에서 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Permission has already been granted
                    AddContentsActivity.openImagePicker()
                }
            }
        }
        else{
            holder.bindNormal(items[position], position)
            holder.itemView.cv_contents_image.setOnClickListener {
            }
            holder.itemView.iv_delete_button.setOnClickListener {
                mContext.deleteFile(items[position])
                items.removeAt(position)
                if(items.size==1) {
                    items.clear()
                    AddContentsActivity.defaultAddView.visibility = View.VISIBLE
                    AddContentsActivity.defaultAddView.bringToFront()
                }
                notifyDataSetChanged()
            }
        }


    }
    override fun getItemCount(): Int {
        return items.size
    }



    inner class ContentsImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        fun bindNormal(fileName: String, position: Int){
            val bitmap = BitmapFactory.decodeFile("${mContext.filesDir}/${fileName}")
            itemView.iv_content_image.setImageBitmap(bitmap)

            val imageWidth = LayoutParamsUtils.getItemWidthByPercent(mContext, IMAGE_WIDTH)
            val imageHeight = LayoutParamsUtils.getItemSizeByRatio(imageWidth, ADD_IMAGE_BACK_HEIGHT_RATIO)
            LayoutParamsUtils.setItemSize(itemView, imageWidth, imageHeight)
            itemView.iv_delete_button.visibility = View.VISIBLE
        }

        fun bindLastItem(){
            val size = LayoutParamsUtils.getItemWidthByPercent(mContext, AddContentsActivity.ADD_BUTTON_WIDTH_RATIO)
            LayoutParamsUtils.setItemSize(itemView, size, size)
            itemView.iv_content_image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_image_small))
            itemView.iv_delete_button.visibility = View.INVISIBLE
        }
    }


}