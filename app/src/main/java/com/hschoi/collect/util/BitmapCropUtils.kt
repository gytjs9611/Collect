package com.hschoi.collect.util

import android.content.Context
import android.graphics.*
import com.hschoi.collect.R

class BitmapCropUtils {

    companion object{
        const val FRAME_TYPE_0 = 0
        const val FRAME_TYPE_1 = 1
        const val FRAME_TYPE_2 = 2
        const val FRAME_TYPE_3 = 3
        const val FRAME_TYPE_4 = 4

        fun getCroppedBitmap(context : Context, src: Bitmap, frameType : Int): Bitmap {
            val pathData = getPathData(context, frameType)

            val path = PathDataUtils.getPath(src, pathData)
            val output = Bitmap.createBitmap(src.width,
                    src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = -0x1000000
            canvas.drawPath(path!!, paint)

            // Keeps the source pixels that cover the destination pixels,
            // discards the remaining source and destination pixels.
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(src, 0f, 0f, paint)
            return output
        }

        fun getPathData(context : Context, type : Int) : String{
            return when(type){
                FRAME_TYPE_0->{
                    context.getString(R.string.frame0_stroke)
                }
                FRAME_TYPE_1->{
                    context.getString(R.string.frame1_stroke)
                }
                FRAME_TYPE_2->{
                    context.getString(R.string.frame2_stroke)
                }
                FRAME_TYPE_3->{
                    context.getString(R.string.frame3_stroke)
                }
                FRAME_TYPE_4->{
                    context.getString(R.string.frame4_stroke)
                }

                else->{
                    context.getString(R.string.frame0_stroke)
                }
            }
        }

        /*fun getPathStokeData(context : Context, type : Int) : String{
            return when(type){
                FRAME_TYPE_0->{
                    context.getString(R.string.frame0_stroke)
                }

                else->{
                    context.getString(R.string.frame0_fill)
                }
            }
        }*/


    }






}
