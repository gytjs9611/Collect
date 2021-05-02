package com.hschoi.collect.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class SelectAndSaveImageUtils {

    companion object{
        fun getByteByBitmap(bmp: Bitmap): ByteArray? {
            var compressData: ByteArray? = null
            val outStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            compressData = outStream.toByteArray()
            try {
                outStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return compressData
        }

        @Throws(IOException::class)
        fun getBytes(inputStream: InputStream): ByteArray? {
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (inputStream.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        }

    }
}