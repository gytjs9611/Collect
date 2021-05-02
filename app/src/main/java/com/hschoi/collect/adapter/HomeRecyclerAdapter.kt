package com.hschoi.collect.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.*
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.UnitConversion
import kotlinx.android.synthetic.main.item_album_card.view.*


class HomeRecyclerAdapter(var items: ArrayList<Albums>) : RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {
    private lateinit var mContext : Context


    // 1. 홀더의 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        Log.d("TAG", "onCreateViewHolder")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_album_card, parent, false)
        mContext = parent.context

        // set view size
        val vHeight = LayoutParamsUtils.getItemHeightByPercent(mContext, MainActivity.CARD_HEIGHT_PERCENT)
        val vWidth = LayoutParamsUtils.getItemSizeByRatio(vHeight, MainActivity.CARD_DIMENSION_RATIO)
        LayoutParamsUtils.setItemSize(v, vWidth, vHeight)

        v.elevation = UnitConversion.dpToPx(mContext, 30f)

        // 카드 엘리베이션, 카드코너 설정


        return HomeViewHolder(v)
    }

    // 2. 홀더의 바인딩
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        if(items[position].id==(-1).toLong()){
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
                val intent = Intent(mContext, AlbumFeedActivity::class.java)
                intent.putExtra("albumId", albumId)
                intent.putExtra("albumTitle", albumTitle)
                intent.putExtra("frameType", frameType)
                intent.putExtra("color", color)
                mContext.startActivity(intent)
            }

            // 앨범 수정, 삭제 하단 메뉴
            holder.itemView.iv_album_card_menu.setOnClickListener {
                val bottomMenuSheet = AlbumBottomMenuSheet(mContext, albumId)
                bottomMenuSheet.show((mContext as FragmentActivity).supportFragmentManager, bottomMenuSheet.tag)
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
            itemView.cv_album_card.setCardBackgroundColor(mContext.getColor(R.color.white))
            itemView.tv_album_title.visibility = View.INVISIBLE
            itemView.iv_album_card_menu.visibility = View.INVISIBLE
            itemView.iv_album_cover.visibility = View.INVISIBLE
            itemView.tv_album_title.visibility = View.INVISIBLE
            itemView.iv_add_icon_last.visibility = View.VISIBLE

        }

        fun bindNormal(album : Albums, position : Int){
            itemView.tv_album_title.visibility = View.VISIBLE
            itemView.iv_album_card_menu.visibility = View.VISIBLE
            itemView.iv_album_cover.visibility = View.VISIBLE
            itemView.tv_album_title.visibility = View.VISIBLE
            itemView.iv_add_icon_last.visibility = View.INVISIBLE

            val albumColor = album.color
            val albumTitle = album.title
            val albumFrame = album.frameType
            val albumCoverImage = album.imageFileName

            // test image
//            val bitmapImage = BitmapFactory.decodeResource(mContext.resources, R.drawable.image)
            val fis = mContext.openFileInput(albumCoverImage)
            val bitmap = BitmapFactory.decodeStream(fis)
            val croppedBitmap = BitmapCropUtils.getCroppedBitmap(mContext, bitmap, albumFrame)

            index = position
            itemView.tv_album_title.text = albumTitle
            itemView.cv_album_card.setCardBackgroundColor(albumColor)
            itemView.iv_album_cover.setImageBitmap(croppedBitmap)
        }
    }

}