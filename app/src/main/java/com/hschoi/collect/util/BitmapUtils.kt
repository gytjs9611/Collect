package com.hschoi.collect.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException


class BitmapUtils {
    companion object {

        fun getResizedBitmap(context: Context, uri: Uri?, viewWidth: Int, viewHeight: Int): Bitmap? {
            if (uri == null)
                return null

            val options = BitmapFactory.Options()
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

            var width = options.outWidth
            var height = options.outHeight
            var sampleSize = 1


            if (width <= height) {  // 세로로 긴 이미지
                while (viewHeight <= height / 2) {
                    width /= 2
                    height /= 2
                    sampleSize *= 2
                }
            } else {   // 가로로 긴 이미지
                while (viewWidth <= width / 2) {
                    width /= 2
                    height /= 2
                    sampleSize *= 2
                }
            }

            options.inSampleSize = sampleSize

            return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
        }

        fun compressBitmap(bitmap: Bitmap): Bitmap? {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)
            val byteArray: ByteArray = stream.toByteArray()
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }

        fun exifOrientationToDegrees(exifOrientation: Int): Int {
            return when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }

        fun rotate(src: Bitmap, degree: Float): Bitmap? {
            // Matrix 객체 생성
            val matrix = Matrix()
            // 회전 각도 셋팅
            matrix.postRotate(degree)
            // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
            return Bitmap.createBitmap(src, 0, 0, src.width,
                    src.height, matrix, true)
        }

        fun getExifDegree(context: Context, uri: Uri): Int {
            var exif: ExifInterface? = null
            try {
                exif = ExifInterface(context.contentResolver.openInputStream(uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val exifOrientation: Int = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            return exifOrientationToDegrees(exifOrientation)
        }

    }
}