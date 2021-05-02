package com.hschoi.collect.customview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import com.hschoi.collect.MainActivity
import com.hschoi.collect.R
import com.hschoi.collect.util.UnitConversion
import kotlinx.android.synthetic.main.item_add_album_card.view.*

class AddAlbumCard(context: Context, isFirst : Boolean) : CardView(context) {
    private lateinit var cardView : CardView
    private lateinit var space : Space
    private lateinit var addIcon : ImageView
    private lateinit var text : TextView

    init {
        initView(isFirst)
    }

    private fun initView(isFirst : Boolean){
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = context.getSystemService(infService) as LayoutInflater
        val v: View = li.inflate(R.layout.item_add_album_card, this, false)
        addView(v)

        cardView = cv_add_album_card
        space = sp_add_icon_top
        addIcon = iv_add_icon_default
        text = tv_description

        cardView.updateLayoutParams {
            cardElevation = UnitConversion.dpToPx(context, MainActivity.CARD_ELEVATION)
            radius = UnitConversion.dpToPx(context, MainActivity.CARD_RADIUS)
        }

        if(!isFirst){
            space.visibility = View.GONE
            text.visibility = View.GONE

            val constraintSet = ConstraintSet()
            constraintSet.apply{
                clone(cl_add_album_card)
                connect(R.id.iv_add_icon_default, ConstraintSet.TOP, R.id.cl_add_album_card, ConstraintSet.TOP)
                connect(R.id.iv_add_icon_default, ConstraintSet.BOTTOM, R.id.cl_add_album_card, ConstraintSet.BOTTOM)
                applyTo(cl_add_album_card)
            }
        }

    }

    /*fun setCardSize(cardWidth : Int, cardHeight : Int){
        cardView.updateLayoutParams {
            width = cardWidth
            height = cardHeight
        }
    }*/

}