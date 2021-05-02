package com.hschoi.collect.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.provider.Settings.Global.getString
import androidx.core.graphics.PathParser
import com.hschoi.collect.R

class PathDataUtils {

    companion object{
        fun getPath(src : Bitmap, pathData : String) : Path? {
            val path = PathParser.createPathFromPathData(pathData)
            return resizePath(path, src.width.toFloat(), src.height.toFloat())
        }

        fun resizePath(path: Path, width: Float, height: Float): Path {
            val bounds = RectF(0f, 0f, width, height)
            val resizedPath = Path(path)
            val src = RectF()
            resizedPath.computeBounds(src, true)
            val resizeMatrix = Matrix()
            resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER)
            resizedPath.transform(resizeMatrix)
            return resizedPath
        }
    }
}