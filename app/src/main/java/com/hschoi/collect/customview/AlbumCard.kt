package com.hschoi.collect.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.updateLayoutParams
import com.hschoi.collect.MainActivity
import com.hschoi.collect.R
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.UnitConversion
import kotlinx.android.synthetic.main.item_album_card.view.*


class AlbumCard : CardView {
    private lateinit var cardView: CardView
    private lateinit var menu : ImageView
    private lateinit var image : ImageView
    private lateinit var title : TextView

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs) {
        initView()
        getAttrs(attrs, defStyle)
    }

    private fun initView() {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = context.getSystemService(infService) as LayoutInflater
        val v: View = li.inflate(R.layout.item_album_card, this, false)
        addView(v)

        cardView = cv_album_card
        menu = iv_album_card_menu
        image = iv_album_cover
        title = tv_album_title


        cardView.updateLayoutParams {
            cardElevation = UnitConversion.dpToPx(context, MainActivity.CARD_ELEVATION)
            radius = UnitConversion.dpToPx(context, MainActivity.CARD_RADIUS)
        }

    }

    private fun getAttrs(attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.AlbumCard)
        setTypeArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlbumCard, defStyle, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        val colorId = typedArray.getResourceId(R.styleable.AlbumCard_albumColor, R.color.album_color_pink)

        cardView.setCardBackgroundColor(resources.getColor(colorId))

        val str = typedArray.getString(R.styleable.AlbumCard_title)
        title.text = str

        typedArray.recycle()
    }

    fun setCardSize(cardWidth : Int, cardHeight : Int){
        cardView.updateLayoutParams {
            height = cardHeight
            width = cardWidth
        }
    }


    fun setColor(colorId: Int) {
        cardView.setCardBackgroundColor(colorId)
    }


    fun setImage(fileName : String, frameType : Int) {
        val fis = context.openFileInput(fileName)
        val bitmap = BitmapFactory.decodeStream(fis)
        image.setImageBitmap(BitmapCropUtils.getCroppedBitmap(context, bitmap, frameType))
    }


//    fun setImage(path : String, frameType : Int) {
//        val bitmap = BitmapFactory.decodeFile(path)
//        image.setImageBitmap(BitmapCropUtils.getCroppedBitmap(context, bitmap, frameType))
//    }

    // for test
    fun setImage(id: Int, frameType : Int) {
        val bitmap = BitmapFactory.decodeResource(resources, id)
        image.setImageBitmap(BitmapCropUtils.getCroppedBitmap(context, bitmap, frameType))

    }


    fun setAlbumTitle(text : String) {
        title.text = text
    }

}
