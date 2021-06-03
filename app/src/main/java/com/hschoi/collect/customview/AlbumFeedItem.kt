package com.hschoi.collect.customview

import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hschoi.collect.R
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.item_album_feed.view.*

class AlbumFeedItem(context : Context, imageName : String, frameType : Int, title : String) : ConstraintLayout(context) {
    private var mContext = context
    lateinit var clFeedItem: ConstraintLayout
    lateinit var clImageFrame : ConstraintLayout
    lateinit var ivImage : ImageView
    lateinit var tvTitle : VerticalTextView

    var mFrameType = frameType
        private set


    init {
        initView(imageName, frameType, title)
    }

    private fun initView(imageName: String, frameType: Int, title: String){
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = context.getSystemService(infService) as LayoutInflater
        val v: View = li.inflate(R.layout.item_album_feed, this, false)
        addView(v)

        clFeedItem = cl_feed_item
        clImageFrame = cl_frame
        ivImage = iv_image
        tvTitle = tv_album_name_title

//        clImageFrame.background = context.getDrawable(R.drawable.ic_frame0_out)

        tvTitle.text = title
        val fis = mContext.openFileInput(imageName)
        val bitmap = BitmapFactory.decodeStream(fis)

//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image)
        ivImage.setImageBitmap(BitmapCropUtils.getCroppedBitmap(context, bitmap, frameType))

        clImageFrame.setBackgroundResource(when(frameType){
            BitmapCropUtils.FRAME_TYPE_0->{
                R.drawable.ic_frame0_out
            }
            BitmapCropUtils.FRAME_TYPE_1->{
                R.drawable.ic_frame1_out
            }
            BitmapCropUtils.FRAME_TYPE_2->{
                R.drawable.ic_frame2_out
            }
            BitmapCropUtils.FRAME_TYPE_3->{
                R.drawable.ic_frame3_out
            }
            BitmapCropUtils.FRAME_TYPE_4->{
                R.drawable.ic_frame4_out
            }
            else->{
                R.drawable.ic_frame0_out
            }
        })
    }

    fun setTitleTextSize(textSize : Float, textViewSize : Int, maxWidth : Int){
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tvTitle.height = textViewSize
        tvTitle.width = maxWidth
        tvTitle.maxLines = 1
        tvTitle.ellipsize = TextUtils.TruncateAt.END
    }


/*

    fun setItemSize(itemWidth : Int, itemHeight : Int){
        clFeedItem.updateLayoutParams {
            width = itemWidth
            height = itemHeight

        }
    }

    fun setItemMargins(left : Int, top : Int, right : Int, bottom : Int){
        val params = clFeedItem.layoutParams as MarginLayoutParams
        params.setMargins(left, top, right, bottom)
    }
*/

}