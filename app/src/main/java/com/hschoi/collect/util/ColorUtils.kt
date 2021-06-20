package com.hschoi.collect.util

import android.content.Context
import com.hschoi.collect.R

class ColorUtils {
    companion object{
        const val PINK = 0
        const val YELLOW = 1
        const val ORANGE = 2
        const val BROWN = 3
        const val LIGHT_PURPLE = 4
        const val PURPLE = 5
        const val GREEN = 6
        const val MINT = 7
        const val BLUE = 8
        const val NAVY = 9
        const val GRAY = 10
        const val BLACK = 11


        fun getAlbumColor(context: Context, color: Int): Int{
            val colorId =  when(color){
                PINK-> R.color.album_color_pink
                YELLOW-> R.color.album_color_yellow
                ORANGE-> R.color.album_color_orange
                BROWN-> R.color.album_color_brown
                LIGHT_PURPLE-> R.color.album_color_light_purple
                PURPLE-> R.color.album_color_purple
                GREEN-> R.color.album_color_green
                MINT-> R.color.album_color_mint
                BLUE-> R.color.album_color_blue
                NAVY-> R.color.album_color_navy
                GRAY-> R.color.album_color_gray
                BLACK-> R.color.album_color_black
                else -> R.color.album_color_pink
            }
            return context.getColor(colorId)
        }

        fun getStatusBarColor(context: Context, color: Int): Int{
            val colorId = when(color){
                PINK-> R.color.album_feed_status_bar_pink
                YELLOW-> R.color.album_feed_status_bar_yellow
                ORANGE-> R.color.album_feed_status_bar_orange
                BROWN-> R.color.album_feed_status_bar_brown
                LIGHT_PURPLE-> R.color.album_feed_status_bar_light_purple
                PURPLE-> R.color.album_feed_status_bar_purple
                GREEN-> R.color.album_feed_status_bar_green
                MINT-> R.color.album_feed_status_bar_mint
                BLUE-> R.color.album_feed_status_bar_blue
                NAVY-> R.color.album_feed_status_bar_navy
                GRAY-> R.color.album_feed_status_bar_gray
                BLACK-> R.color.album_feed_status_bar_black
                else -> R.color.album_feed_status_bar_pink
            }

            return context.getColor(colorId)
        }
    }
}