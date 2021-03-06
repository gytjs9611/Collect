package com.hschoi.collect.customview

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatImageView
import com.hschoi.collect.customview.ImageCroppingView.State.ANIMATE_ZOOM
import com.hschoi.collect.customview.ImageCroppingView.State.DRAG
import com.hschoi.collect.customview.ImageCroppingView.State.FLING
import com.hschoi.collect.customview.ImageCroppingView.State.NONE
import com.hschoi.collect.customview.ImageCroppingView.State.ZOOM
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.BitmapUtils
import com.hschoi.collect.util.SelectAndSaveImageUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * base on :https://github.com/MikeOrtiz/TouchImageView
 * @author mytcking@gmail.com
 */
class ImageCroppingView : AppCompatImageView {

    companion object {
        private const val DEBUG = "DEBUG"
        const val ACTION_MEASURE = "com.hschoi.collect.customview.ACTION_MEASURE"

        //
        // SuperMin and SuperMax multipliers. Determine how much the image can be
        // zoomed below or above the zoom boundaries, before animating back to the
        // min/max zoom boundary.
        //
        private const val SUPER_MIN_MULTIPLIER = .75f
        private const val SUPER_MAX_MULTIPLIER = 1f
        private const val ZOOM_TIME = 500f

        const val TRANS_TYPE_X = 0
        const val TRANS_TYPE_Y = 1
    }
    /**
     * Get the current zoom. This is the zoom relative to the initial
     * scale, not the original resource.
     * @return current zoom multiplier.
     */

    var currentZoom = 0f
//        private set // getter??? ???????????? setter??? ??????. (???????????? ?????? ??????)


    var mMatrix: Matrix? = null

    enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    private var state: State? = null
    var minScale = 0f
        private set
    private var maxScale = 0f
    private var superMinScale = 0f
    private var superMaxScale = 0f
    var matrixFloatArray: FloatArray? = null
        private set
    private var mContext: Context? = null
    private var fling: Fling? = null

    private var viewWidth = 0       // ????????? ??????
    private var viewHeight = 0      // ????????? ??????


    var matchViewWidth = 0f
    var matchViewHeight = 0f
    private var redundantXSpace = 0f
    private var redundantYSpace = 0f


    private var mScaleDetector: ScaleGestureDetector? = null    // ???????????? ?????????
    private var mGestureDetector: GestureDetector? = null       // ?????? ?????????
    var x0 = 0f     // ????????? ????????? ?????? ??? ????????? x ??????
        private set
    var y0 = 0f     // ????????? ????????? ?????? ??? ????????? y ??????
        private set
    var imageCroppingViewWidth = 0
        private set
    var imageCroppingViewHeight = 0
        private set

    private var frameWidth = 0
    private var frameHeight = 0
    private lateinit var pathData : String

    private var isAlreadyExecuted = false
    private var isXOver = false
    private var isYOver = false


    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        sharedConstructing(context)
    }

    fun initView(context : Context){
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        Log.d("LIFE CYCLE", "sharedConstructing")

        super.setClickable(true)
        this.mContext = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mGestureDetector = GestureDetector(context,GestureListener())
        mMatrix = Matrix()
        matrixFloatArray = FloatArray(9)

        currentZoom = 1f
//        minScale = 0.2f
//        maxScale = 3f
//        superMinScale = SUPER_MIN_MULTIPLIER * minScale
//        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        setState(NONE)
        setOnTouchListener(TouchImageViewListener())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
//        setImageCalled()
//        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
//        setImageCalled()
//        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
//        setImageCalled()
//        savePreviousImageValues()
        fitImageToView()
    }

    fun setImageURI(uri: Uri?, w:Int, h:Int) {
//        super.setImageURI(uri)
        var bitmap = BitmapUtils.getResizedBitmap(context, uri, w, h) ?: return
        // ????????? ???????????? ????????? ??????, ????????? ???????????? ????????????
        if(uri==null) return
        val exifDegree = BitmapUtils.getExifDegree(context, uri)
        if(exifDegree!=0){
            bitmap = BitmapUtils.rotate(bitmap, exifDegree.toFloat()) ?: return
        }
        setImageBitmap(bitmap)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        fitImageToView()
    }

    fun setFrameStyle(type : Int, width: Int, height : Int){
        pathData = BitmapCropUtils.getPathData(context, type)
        frameWidth = width
        frameHeight = height
        x0 = (imageCroppingViewWidth - frameWidth).toFloat() / 2
        y0 = (imageCroppingViewHeight - frameHeight).toFloat() / 2
        // ????????? ????????? ??????????????????, ????????? ?????? ????????? x0, y0??? ???????????? ??????
    }


    /**
     * Get the max zoom multiplier.
     * @return max zoom multiplier.
     */
    /**
     * Set the max zoom multiplier. Default value: 3.
     * @param max max zoom multiplier.
     */
    var maxZoom: Float
        get() = maxScale
        set(max) {
            maxScale = max
            superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        }

    /**
     * Get the min zoom multiplier.
     * @return min zoom multiplier.
     */
    /**
     * Set the min zoom multiplier. Default value: 1.
     * @param min min zoom multiplier.
     */
    var minZoom: Float
        get() = minScale
        set(min) {
            minScale = min
            superMinScale = SUPER_MIN_MULTIPLIER * minScale
        }

    /**
     * After setting image, a value of true means the new image should maintain
     * the zoom of the previous image. False means the image should be resized within
     * the view. Defaults value is true.
     * @param maintainZoom
     */
    /*fun maintainZoomAfterSetImage(maintainZoom: Boolean) {
        maintainZoomAfterSetImage = maintainZoom
    }*/

    /**
     * For a given point on the view (ie, a touch event), returns the
     * point relative to the original drawable's coordinate system.
     * @param x
     * @param y
     * @return PointF relative to original drawable's coordinate system.
     */
    fun getDrawablePointFromTouchPoint(x: Float, y: Float): PointF {
        return transformCoordTouchToBitmap(x, y, true)
    }

    /**
     * For a given point on the view (ie, a touch event), returns the
     * point relative to the original drawable's coordinate system.
     * @param p
     * @return PointF relative to original drawable's coordinate system.
     */
    fun getDrawablePointFromTouchPoint(p: PointF): PointF {
        return transformCoordTouchToBitmap(p.x, p.y, true)
    }

    /**
     * Performs boundary checking and fixes the image matrix if it
     * is out of bounds.
     */

    // ????????? ???????????? ???????????? ????????????
    private fun fixTrans() {
        Log.d("TAG", "???????????? ?????? ??????")
        mMatrix!!.getValues(matrixFloatArray)
        val transX = matrixFloatArray!![Matrix.MTRANS_X]
        val transY = matrixFloatArray!![Matrix.MTRANS_Y]
        val fixTransX = getFixTrans(TRANS_TYPE_X, transX)
        val fixTransY = getFixTrans(TRANS_TYPE_Y, transY)
        if (fixTransX != 0f || fixTransY != 0f) {
            mMatrix!!.postTranslate(fixTransX, fixTransY)   // ????????? ??????
        }

        imageMatrix = mMatrix

        Log.d("TRANS", "transX=$transX, transY=$transY, fixTransX=$fixTransX, fixTransY=$fixTransY")
//        isXOver = fixTransX==0f
        isYOver = fixTransY==0f
    }

    /**
     * When transitioning from zooming from focus to zoom from center (or vice versa)
     * the image can become unaligned within the view. This is apparent when zooming
     * quickly. When the content size is less than the view size, the content will often
     * be centered incorrectly within the view. fixScaleTrans first calls fixTrans() and
     * then makes sure the image is centered correctly within the view.
     */

    // ???????????? ?????? ?????? ?????? ????????? ??????????????? ??????
    private fun fixScaleTrans() {
        Log.d("TAG", "???????????? ?????? ?????? ?????? ????????? ??????????????? ??????")
        fixTrans()
        mMatrix!!.getValues(matrixFloatArray)
        if (imageWidth < viewWidth) {
            matrixFloatArray!![Matrix.MTRANS_X] = (viewWidth - imageWidth) / 2
//            m!![Matrix.MTRANS_X] = (viewWidth - viewHeight.toFloat()*0.5).toFloat()/2
        }
        if (imageHeight < viewHeight) {
            matrixFloatArray!![Matrix.MTRANS_Y] = (viewHeight - imageHeight) / 2
//            m!![Matrix.MTRANS_Y] = (viewWidth - viewHeight.toFloat()*0.5).toFloat()/2
        }
        mMatrix!!.setValues(matrixFloatArray)
    }

    // ????????? ???????????? ???????????? ????????? ?????? ????????????
    private fun getFixTrans(transType : Int, trans: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        /* ceil, floor ????????? ?????? */
        // viewWidth, frameWidth ??? ???????????????, imageWidth ??? ???????????? ?????????
        // minTrans, maxTrans ??? ????????? ????????????
        // ?????? ???????????? ????????? ?????????????????? frame ??? ?????? ???????????? ????????? ???????????? ????????? ????????? ??????
        // minTrans??? ?????????, maxTrans??? ????????? ????????? ???????????? ????????? ?????? ????????? ????????? ?????? ??????
        Log.d("VIEW", "ICV CLASS viewWidth=$viewWidth, viewHeight=$viewHeight")
        if(transType == TRANS_TYPE_X){
            minTrans = ceil((viewWidth+frameWidth).toFloat()/2) - imageWidth
            maxTrans = floor((viewWidth-frameWidth).toFloat()/2)
            Log.d("TRANS", "trans=$trans, minTrans=$minTrans, maxTrans=$maxTrans")
        }
        else {  // TRANS_TYPE_Y
            minTrans = ceil((viewHeight+frameHeight).toFloat()/2) - imageHeight
            maxTrans = floor((viewHeight-frameHeight).toFloat()/2)
        }
        
        if (trans < minTrans)   // ??????/?????? ?????? ?????? ??????, minTrans ????????? ??????
            return -trans + minTrans

        // ?????????/????????? ?????? ?????? ??????, maxTrans??? ??????
        // minTrans < trans < maxTrans ??? ??????(??????) ???????????? ???????????? ??????
        return if (trans > maxTrans) -trans + maxTrans else 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return delta
    }

    private val imageWidth: Float
        private get() = matchViewWidth * currentZoom

    private val imageHeight: Float
        private get() = matchViewHeight * currentZoom

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("LIFE CYCLE", "onMeasure")
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        // ????????? ??????, ?????? ??????
        viewWidth = setViewSize(widthMode, widthSize, drawableWidth)
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight)

        Log.d("TAG", "drawable_width $drawableWidth, height $drawableHeight")
        
        setMeasuredDimension(viewWidth, viewHeight)
        Log.d("TAG", "icv width=$viewWidth, icv height=$viewHeight")


        // ?????? ???????????? ???????????? ???????????? ??????
        // ?????? 1?????? ???????????? ?????? isAlreadyExecuted ?????? ??????
        // (?????? ???????????? ????????? ?????? ?????????4??? ???????????? onMeasure ?????? ???????????? ????????? ??????????????? ??? ??????)
        // Measure mode??? exactly??? ??? ????????? viewWidth, viewHeight??? ??????????????? ????????????
        if(!isAlreadyExecuted){
            Log.d("TAG", "fitImageToView ?????????")
            isAlreadyExecuted = true
            fitImageToView()

            val intent = Intent(ACTION_MEASURE)
            mContext?.sendBroadcast(intent)
        }

        if(isXOver){
//            mMatrix!!.postTranslate(-300f, 0f)
        }
        if(isYOver){
//            mMatrix!!.postTranslate(0f, 100f)
        }

        // minScale ??????(Main oncreate ?????? ??? onMeasure ????????? ????????????????????? ????????? ??????..)
        // min, max scale ??????
        setScale()

    }

    fun setScale(){
        val scaleX = frameWidth.toFloat()/matchViewWidth
        val scaleY = frameHeight.toFloat()/matchViewHeight
        minScale = max(scaleX, scaleY)
        Log.d("TAG", "minScale=$minScale")
        maxScale = 3f

        Log.d("TAG", "min=$minScale, max=$maxScale")
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
    }

    /**
     * If the normalizedScale is equal to 1, then the image is made to fit the screen. Otherwise,
     * it is made to fit the screen according to the dimensions of the previous image matrix. This
     * allows the image to maintain its zoom after rotation.
     */
    private fun fitImageToView() {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }
        if (mMatrix == null) {
            return
        }

        // ?????? ????????? ?????? ??????
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight


        // ?????? ???????????? ?????? ??? ?????? ??? ?????? ??????????????? ?????? ?????? ??????
        val scaleX = viewWidth.toFloat() / drawableWidth.toFloat()
        val scaleY = viewHeight.toFloat() / drawableHeight.toFloat()
        var scale = min(scaleX, scaleY)
        Log.d("TAG", "viewWidth=$viewWidth, viewHeight=$viewHeight, scale=$scale")

        Log.d("TAG", "frameHeight=$frameHeight, drawableHeight*scale=$drawableHeight*$scale=${drawableHeight*scale}")
        
        // ????????? ???????????? ????????? ????????? ??? ????????? ????????? ?????? scale ????????? ?????????
        if(frameHeight>scale*drawableHeight){    // ????????? ???????????? ?????? ?????? ??????
            val extraScale = frameHeight.toFloat()/(drawableHeight.toFloat()*scale)
            scale *= extraScale
            Log.d("TAG", "?????? ?????? extraScale=$extraScale, resultScale=$scale")
        }
        else if(frameWidth>scale*drawableWidth){ // ????????? ???????????? ?????? ?????? ??????
            val extraScale = frameWidth.toFloat()/(drawableWidth.toFloat()*scale)
            scale *= extraScale
            Log.d("TAG", "?????? ?????? extraScale=$extraScale, resultScale=$scale")
        }
        
        

        // ??????????????? ?????? ??????
        redundantYSpace = viewHeight - scale * drawableHeight
        redundantXSpace = viewWidth - scale * drawableWidth


        // ?????? ??? ???????????? ????????? ??????
        matchViewWidth = viewWidth - redundantXSpace
        matchViewHeight = viewHeight - redundantYSpace


        if (currentZoom == 1f) { // ??? ????????? ????????? ?????? ???????????? ????????? ??? ????????? ???????????????

            mMatrix!!.setScale(scale, scale)    // ?????? ??????
            if(redundantXSpace<redundantYSpace){    // ??? ?????? ?????? ?????? ?????? ??????
                mMatrix!!.postTranslate(0f, redundantYSpace/2)
            }
            else{   // ??? ???????????? ?????? ?????? ??????
                mMatrix!!.postTranslate(redundantXSpace/2, 0f)
            }
        }

        imageMatrix = mMatrix
    }

    /**
     * Set view dimensions based on layout params
     *
     * @param mode
     * @param size
     * @param drawableWidth
     * @return
     */
    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
    /*    when(mode){
            MeasureSpec.EXACTLY -> Log.d("TAG", "mode exactly")
            MeasureSpec.AT_MOST -> Log.d("TAG", "mode at most")
            MeasureSpec.UNSPECIFIED -> Log.d("TAG", "mode unspecified")
        }
*/
        val viewSize: Int
        viewSize = when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> Math.min(drawableWidth, size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
        return viewSize
    }

    private fun setState(state: State) {
        this.state = state
    }

    /**
     * Gesture Listener detects a single click or long click and passes that on
     * to the view's listener.
     * @author Ortiz
     */
    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        /*override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (fling != null) {
                //
                // If a previous fling is still active, it should be cancelled so that two flings
                // are not run simultaenously.
                //
                fling?.cancelFling()
            }
            fling = Fling(velocityX.toInt(), velocityY.toInt())
            compatPostOnAnimation(fling)
            return super.onFling(e1, e2, velocityX, velocityY)
        }*/

    }

    /**
     * Responsible for all touch events. Handles the heavy lifting of drag and also sends
     * touch events to Scale Detector and Gesture Detector.
     * @author Ortiz
     */
    private inner class TouchImageViewListener : OnTouchListener {
        //
        // Remember last point position for dragging
        //
        private val last = PointF()
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            mScaleDetector!!.onTouchEvent(event)
            mGestureDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            if (state == NONE || state == DRAG || state == FLING) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        last.set(curr)
                        if (fling != null) fling?.cancelFling()
                        setState(DRAG)
                    }
                    MotionEvent.ACTION_MOVE -> if (state == DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
//                        val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), imageWidth)
//                        val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), imageHeight)
//                        mMatrix!!.postTranslate(fixTransX, fixTransY)
                        mMatrix!!.postTranslate(deltaX, deltaY)
                        fixTrans()
//                        fixScaleTrans() // **
                        last[curr.x] = curr.y
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> setState(NONE)
                }
            }
            imageMatrix = mMatrix
            //
            // indicate event was handled
            //
            return true
        }
    }

    /**
     * ScaleListener detects user two finger scaling and scales image.
     * @author Ortiz
     */
    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        // ??????,?????? ????????? ?????????
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            setState(ZOOM)
            return true
        }

        // ??????,?????? ???
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.scaleFactor, detector.focusX, detector.focusY, true)
            return true
        }

        // ??????, ?????? ???
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            setState(NONE)
            var animateToZoomBoundary = false
            var targetZoom: Float = currentZoom
            if (currentZoom > maxScale) {
                targetZoom = maxScale
                animateToZoomBoundary = true
            } else if (currentZoom < minScale) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }
            // ?????? ????????? ?????? ???????????? ?????????, ?????? ????????? ?????? ???????????? ?????? ??????
            if (animateToZoomBoundary) {
                /*val doubleTap: DoubleTapZoom = DoubleTapZoom(targetZoom, viewWidth.toFloat() / 2, viewHeight.toFloat() / 2, true)
                compatPostOnAnimation(doubleTap)*/
                setZoomInBoundary(targetZoom)
            }
            fixTrans()
        }
    }

    fun setZoomInBoundary(targetZoom : Float){
        val doubleTap: DoubleTapZoom = DoubleTapZoom(targetZoom, viewWidth.toFloat() / 2, viewHeight.toFloat() / 2, true)
        compatPostOnAnimation(doubleTap)
    }

    fun setZoomMinScale(){
        val doubleTap: DoubleTapZoom = DoubleTapZoom(minScale, viewWidth.toFloat() / 2, viewHeight.toFloat() / 2, true)
        compatPostOnAnimation(doubleTap)
    }

    fun getMatrixTransX() :Float{
        mMatrix!!.getValues(matrixFloatArray)
        return matrixFloatArray!![Matrix.MTRANS_X]
    }

    fun getMatrixTransY() :Float{
        mMatrix!!.getValues(matrixFloatArray)
        return matrixFloatArray!![Matrix.MTRANS_Y]
    }

    fun getSrcImageHeight() : Float{
        return imageHeight
    }

    fun moveImage(dx : Float, dy : Float){
        mMatrix!!.postTranslate(dx, dy)
        imageMatrix = mMatrix
        fixTrans()
    }

    fun getMinTransY() : Float{
        return ceil((viewHeight+frameHeight).toFloat()/2) - imageHeight
    }

    fun getMaxTransY() : Float{
        return floor((viewHeight-frameHeight).toFloat()/2)
    }





    fun scaleImage(deltaScale: Float, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var deltaScale = deltaScale
        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale
        } else {
            lowerScale = minScale
            upperScale = maxScale
        }
        val origScale = currentZoom
        currentZoom *= deltaScale
        if (currentZoom > upperScale) {
            currentZoom = upperScale
            deltaScale = upperScale / origScale
        } else if (currentZoom < lowerScale) {
            currentZoom = lowerScale
            deltaScale = lowerScale / origScale
        }
        mMatrix!!.postScale(deltaScale, deltaScale, focusX, focusY) // ??????, ??????

//        fixScaleTrans();  // ???????????? ?????? ???????????? ?????? ???, ?????? ?????? ?????? ????????? ??????????????? ??????
    }

    /**
     * DoubleTapZoom calls a series of runnables which apply
     * an animated zoom in/out graphic to the image.
     * @author Ortiz
     */
    private inner class DoubleTapZoom internal constructor(targetZoom: Float, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) : Runnable {
        private val startTime: Long
        private val startZoom: Float
        private val targetZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val stretchImageToSuper: Boolean
        private val interpolator = AccelerateDecelerateInterpolator()
        private val startTouch: PointF
        private val endTouch: PointF

        init {
            setState(ANIMATE_ZOOM)
            startTime = System.currentTimeMillis()
            startZoom = currentZoom
            this.targetZoom = targetZoom
            this.stretchImageToSuper = stretchImageToSuper
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false)
            bitmapX = bitmapPoint.x
            bitmapY = bitmapPoint.y

            //
            // Used for translating image during scaling
            //
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat())
        }

        override fun run() {
            val t = interpolate()
            Log.d("TAG", "interpolate=$t")
            val deltaScale = calculateDeltaScale(t)
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
//            fixScaleTrans()
            fixTrans()
            imageMatrix = mMatrix
            if (t < 1f) {   // ??????/???????????? ???
                compatPostOnAnimation(this)
            } else {    // ??????/?????? ??????
                setState(NONE)
                invalidate()
            }
        }

        /**
         * Interpolate between where the image should start and end in order to translate
         * the image so that the point that is touched is what ends up centered at the end
         * of the zoom.
         * @param t
         */
        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
            mMatrix!!.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        /**
         * Use interpolator to get t
         * @return
         */
        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed: Float = (currTime - startTime) / ZOOM_TIME
            elapsed = Math.min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        /**
         * Interpolate the current targeted zoom and get the delta
         * from the current zoom.
         * @param t
         * @return
         */
        private fun calculateDeltaScale(t: Float): Float {
            val zoom = startZoom + t * (targetZoom - startZoom)
            return zoom / currentZoom
        }




    }

    /**
     * This function will transform the coordinates in the touch event to the coordinate
     * system of the drawable that the imageview contain
     * @param x x-coordinate of touch event
     * @param y y-coordinate of touch event
     * @param clipToBitmap Touch event may occur within view, but outside image content. True, to clip return value
     * to the bounds of the bitmap size.
     * @return Coordinates of the point touched, in the coordinate system of the original drawable.
     */
    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        mMatrix!!.getValues(matrixFloatArray)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = matrixFloatArray!![Matrix.MTRANS_X]
        val transY = matrixFloatArray!![Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / imageWidth
        var finalY = (y - transY) * origH / imageHeight
        if (clipToBitmap) {
            finalX = Math.min(Math.max(x, 0f), origW)
            finalY = Math.min(Math.max(y, 0f), origH)
        }
        return PointF(finalX, finalY)
    }

    /**
     * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
     * drawable's coordinate system to the view's coordinate system.
     * @param bx x-coordinate in original bitmap coordinate system
     * @param by y-coordinate in original bitmap coordinate system
     * @return Coordinates of the point in the view's coordinate system.
     */
    private fun transformCoordBitmapToTouch(bx: Float, by: Float): PointF {
        mMatrix!!.getValues(matrixFloatArray)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = matrixFloatArray!![Matrix.MTRANS_X] + imageWidth * px
        val finalY = matrixFloatArray!![Matrix.MTRANS_Y] + imageHeight * py
        return PointF(finalX, finalY)
    }

    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the image. The values for the translation
     * are interpolated by the Scroller.
     * @author Ortiz
     */
    private inner class Fling internal constructor(velocityX: Int, velocityY: Int) : Runnable {
        var scroller: Scroller?
        var currX: Int
        var currY: Int
        fun cancelFling() {
            if (scroller != null) {
                setState(NONE)
                scroller!!.forceFinished(true)
            }
        }

        override fun run() {
            if (scroller!!.isFinished) {
                scroller = null
                return
            }
            if (scroller!!.computeScrollOffset()) {
                val newX = scroller!!.currX
                val newY = scroller!!.currY
                val transX = newX - currX
                val transY = newY - currY
                currX = newX
                currY = newY
                mMatrix!!.postTranslate(transX.toFloat(), transY.toFloat())
                fixTrans()
//                fixScaleTrans() // **
                imageMatrix = mMatrix
//                compatPostOnAnimation(this)
            }
        }

        init {
            setState(FLING)
            scroller = Scroller(mContext)
            mMatrix!!.getValues(matrixFloatArray)
            val startX = matrixFloatArray!![Matrix.MTRANS_X].toInt()
            val startY = matrixFloatArray!![Matrix.MTRANS_Y].toInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (imageWidth > viewWidth) {
                maxX = imageWidth.toInt()
                minX = maxX * -1
            } else {
                maxX = viewWidth
                minX = maxX * -1
            }
            if (imageHeight > viewHeight) {
                maxY = imageHeight.toInt()
                minY = maxY * -1
            } else {
                maxY = viewHeight
                minY = maxY * -1
            }
            scroller!!.fling(startX, startY, velocityX, velocityY, minX,
                    maxX, minY, maxY)
            currX = startX
            currY = startY
        }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private fun compatPostOnAnimation(runnable: Runnable?) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable)
        } else {
            postDelayed(runnable, 1000 / 60.toLong())
        }
    }


    override fun onDraw(canvas: Canvas) {
        Log.d("LIFE CYCLE", "onDraw")
        super.onDraw(canvas)
        
        // ??????, ?????? ?????? ????????? ????????? ??????
        imageCroppingViewWidth = this.width
        imageCroppingViewHeight = this.height

        x0 = (imageCroppingViewWidth - frameWidth).toFloat() / 2
        y0 = (imageCroppingViewHeight - frameHeight).toFloat() / 2


        /*val x1 = x0 + croppedImageWidth
        val y1 = y0
        val y2 = y1 + croppedImageHeight
        val x3 = x0
        val y3 = y0 + croppedImageHeight*/

        /*val path = PathParser.createPathFromPathData(pathData)
        val resizedPath = PathDataUtils.resizePath(path, croppedImageWidth.toFloat(), croppedImageHeight.toFloat())

        paint.color = -0x56000000   // ?????? ?????? ?????? ????????? ?????????
        canvas.drawRect(0f, 0f, imageCroppingViewWidth.toFloat(), imageCroppingViewHeight.toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
//        paint.color = Color.TRANSPARENT
        paint.color = Color.parseColor("#558392")   // ?????? ?????? ?????? ????????? ?????????
        canvas.translate(x0, y0)    // ????????? ????????? paint ?????? ?????? ??????
        canvas.drawPath(resizedPath, paint)

        paint.color = -0x56000000   // ?????? ?????? ?????? ????????? ?????????*/


        /*canvas.translate(-x0, -y0)  // paint ?????? ?????? ?????? ????????????
        //left
        canvas.drawRect(0f, y0, x3, y3, paint)
        //top
        canvas.drawRect(0f, 0f, imageCroppingViewWidth.toFloat(), y0, paint)
        //right
        canvas.drawRect(x1, y1, imageCroppingViewWidth.toFloat(), y2, paint)
        //bottom
        canvas.drawRect(0f, y3, imageCroppingViewWidth.toFloat(), imageCroppingViewHeight.toFloat(), paint)*/
    }

    val croppedImage: Bitmap
        get() {
            isDrawingCacheEnabled = true
            val bitMap = drawingCache
            val croppedBitmap = Bitmap.createBitmap(bitMap,
                    (x0).toInt(),
                    (y0).toInt(),
                    (frameWidth).toInt(),
                    (frameHeight).toInt())
            bitMap.recycle()
            isDrawingCacheEnabled = false
            return croppedBitmap
        }

    val croppedImageBytes: ByteArray?
        get() = SelectAndSaveImageUtils.getByteByBitmap(croppedImage)


}