package com.hschoi.collect


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import com.hschoi.collect.customview.ImageCroppingView
import com.hschoi.collect.customview.ColorItem
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.util.*
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_add_contents.*
import kotlinx.android.synthetic.main.activity_create_new_album.cl_image_cropping_view
import kotlinx.android.synthetic.main.activity_create_new_album.iv_add_icon_default
import kotlinx.android.synthetic.main.activity_create_new_album.iv_frame_stroke
import kotlinx.android.synthetic.main.activity_create_new_album.iv_image_cropping_view_frame_back
import kotlinx.android.synthetic.main.activity_create_new_album.layout_album_color
import kotlinx.android.synthetic.main.activity_create_new_album.layout_album_frame
import kotlinx.android.synthetic.main.activity_create_new_album.layout_album_title
import kotlinx.android.synthetic.main.activity_create_new_album.layout_top_menu_create_album
import kotlinx.android.synthetic.main.layout_common_title.view.*
import kotlinx.android.synthetic.main.layout_create_new_album_color.*
import kotlinx.android.synthetic.main.layout_create_new_album_color.view.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.view.*
import kotlinx.android.synthetic.main.layout_create_new_album_title.view.*
import kotlinx.android.synthetic.main.layout_frame_select_button.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.tv_album_name_title
import java.io.FileOutputStream
import java.lang.Exception

class ModifyAlbumActivity : AppCompatActivity() {

    companion object{
        private const val IMAGE_CROPPING_VIEW_HEIGHT_RATIO = 272f/716f

        private const val ADD_IMAGE_ICON_HEIGHT_PERCENT = 26f/716f
        private const val ADD_IMAGE_SWITCH_ICON_HEIGHT_PERCENT = 38f/716f

        private const val FRAME_WIDTH_RATIO = 114f/234f
        private const val FRAME_HEIGHT_PERCENT = 234f/716f    // ?????? * ratio = ??????

        private const val FRAME4_WIDTH_RATIO = 114f/230f
        private const val FRAME4_HEIGHT_PERCENT = 230f/716f    // ?????? * ratio = ??????

        private const val COMMON_MARGIN_SIZE_PERCENT = 16f/360f
        private const val COMMON_TOP_MARGIN_SIZE_PERCENT = 22f/716f

        private const val TITLE_FONT_SIZE_PERCENT = 16f/716f
        private const val TITLE_EDIT_TEXT_FONT_SIZE_PERCENT = 18f/716f
        private const val FRAME_SUBTITLE_FONT_SIZE_PERCENT = 12f/716f


        private const val TITLE_TOP_MARGIN_PERCENT = 12f/716f
        private const val ET_TOP_MARGIN_PERCENT = 3f/716f

        private const val FRAME_TOP_MARGIN_PERCENT = 23f/716f
        private const val FRAME_SUBTITLE_START_MARGIN_PERCENT = 8f/360f
        private const val FRAME_ITEM_TOP_MARGIN_PERCENT = 13f/716f

        private const val COLOR_TOP_MARGIN_PERCENT = 22f/716f
        private const val COLOR_ITEM_TOP_MARGIN_PERCENT = 11f/716f
        private const val COLOR_ITEM_SIZE_PERCENT = 38f/360f
        private const val COLOR_ITEM_BOTTOM_MARGIN_RATIO = 12f/38f



        //        ????????? ??????
        private const val REQ_STORAGE_PERMISSION = 100
        private const val REQ_GALLERY = 101

        lateinit var activity : ModifyAlbumActivity
    }


    private lateinit var topMenuBarLayout : View
    private lateinit var topMenuBarTitle : TextView
    private lateinit var topMenuBarBackButton : ImageView
    private lateinit var topMenuBarOkButton : ImageView


    private var albumId : Long = -1
    private lateinit var mAlbumEntity : AlbumEntity

    private var selectedFrameType = BitmapCropUtils.FRAME_TYPE_0
    private var viewWidth = 0
    private var viewHeight = 0
    private var frameWidth = 0
    private var frameHeight = 0

    private lateinit var selectedFrameButton : View
    private lateinit var selectedColorButton : ColorItem
    private var selectedColor = -1


    private lateinit var imageCroppingView : ImageCroppingView

    private var tempUri : Uri? = null
    private var isInitFinished = false

    private var measureReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action==ImageCroppingView.ACTION_MEASURE){
                loadSavedImageState()
                drawFrameBack(selectedFrameType)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_album)

        activity = this@ModifyAlbumActivity

        albumId = intent.getLongExtra("albumId", -1)

        // ????????? ?????? ??? ?????? onMeasure ??? ???????????? ??? ???????????? ?????????????????? ????????? ??????
        val filter = IntentFilter()
        filter.addAction(ImageCroppingView.ACTION_MEASURE)
        registerReceiver(measureReceiver, filter)


        imageCroppingView = ImageCroppingView(applicationContext)
        cl_image_cropping_view.addView(imageCroppingView)

        // imageCroppingView ??? ??? ?????? ????????? ??????
        iv_image_cropping_view_frame_back.bringToFront()
        iv_add_icon_default.bringToFront()
        iv_frame_stroke.bringToFront()


        loadAlbumInfo()  // ????????? ????????? ????????? ?????? ??????, ???????????? ?????????
        isInitFinished = true

        setFrameButtonOnClickListener() // ????????? ?????? ?????? ?????????
        setColorButtonOnClickListener() // ?????? ?????? ?????? ?????????

        initTopMenuBar()    // ????????? ?????? ??????, ??? ??????


        //  ????????? ?????? ??? ?????? ???????????? ??????
        viewWidth = LayoutParamsUtils.getScreenWidth(this)
        viewHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, IMAGE_CROPPING_VIEW_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(cl_image_cropping_view, viewWidth, viewHeight)
        LayoutParamsUtils.setItemSize(imageCroppingView, viewWidth, viewHeight)

        frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
        frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)

        // CroppingView??? ????????? ?????? ??????
        imageCroppingView.setFrameStyle(selectedFrameType, frameWidth, frameHeight)


        // icv ??? ????????? ????????? ??????
        val fis = openFileInput(mAlbumEntity.coverImageOriginFileName)
        val savedBitmap = BitmapFactory.decodeStream(fis)

        imageCroppingView.initView(applicationContext)  // ?????? ???????????? ?????????
        imageCroppingView.setImageBitmap(savedBitmap)



        // add icon size
        val addIconSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ADD_IMAGE_SWITCH_ICON_HEIGHT_PERCENT)
        iv_add_icon_default.updateLayoutParams {
            width = addIconSize
            height = addIconSize
        }

        // ????????? ?????? ?????? ????????? ????????? ??????
        iv_add_icon_default.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // First time
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQ_STORAGE_PERMISSION
                    )
                } else {
                    // Not first time
                    Toast.makeText(this, "?????? ???????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Permission has already been granted
                openImagePicker()
            }
        }


        // ??????
        val titleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_FONT_SIZE_PERCENT)
        val titleEditTextFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_EDIT_TEXT_FONT_SIZE_PERCENT)
        layout_album_title.tv_album_name_title.setTextSize(Dimension.DP, titleFontSize.toFloat())
        layout_album_title.layout_title.et_title.setTextSize(Dimension.DP, titleEditTextFontSize.toFloat())
        initTitleLayoutMargin()

        // ?????????
        initFrameButtons()
        initFrameLayoutMargin()

        // ??????
        val colorItemSize = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COLOR_ITEM_SIZE_PERCENT)
        initColorButtons()
        setColorButtonSize(colorItemSize)
        initColorLayoutMargin()


        // ?????? ??????
        layout_top_menu_create_album.cl_icon_left.setOnClickListener {
            backButtonEvent()
        }

        // ?????? ??????
        layout_top_menu_create_album.cl_icon_right.setOnClickListener {
            val title = layout_album_title.layout_title.et_title.text.toString()

            if(title==""){
                Toast.makeText(this, R.string.toast_enter_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ?????????????????? ??????????????? ??????
            var fos : FileOutputStream
            try{
                // cover
                fos = openFileOutput(mAlbumEntity.coverImageFileName, Context.MODE_PRIVATE)
                imageCroppingView.croppedImage
                        .compress(Bitmap.CompressFormat.JPEG, 50, fos)
                fos.write(imageCroppingView.croppedImageBytes)
                fos.close()

                // origin
                if(tempUri!=null){  // ????????? ???????????? ????????????
                    fos = openFileOutput(mAlbumEntity.coverImageOriginFileName, Context.MODE_PRIVATE)
                    var bitmap = BitmapUtils.uriToBitmap(this, tempUri)

                    // ????????? ???????????? ????????? ???????????? ??????
                    val exifDegree = BitmapUtils.getExifDegree(applicationContext, tempUri!!)
                    if(exifDegree!=0){
                        bitmap = BitmapUtils.rotate(bitmap, exifDegree.toFloat()) ?: return@setOnClickListener
                    }

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                    fos.close()
                }

            } catch (e :Exception){
            }


            // ?????? ?????? ??????
            mAlbumEntity.albumTitle = title
            mAlbumEntity.albumColor = selectedColor
            mAlbumEntity.frameType = selectedFrameType
            mAlbumEntity.zoom = imageCroppingView.currentZoom
            mAlbumEntity.x = imageCroppingView.getMatrixTransX()
            mAlbumEntity.y = imageCroppingView.getMatrixTransY()


            val modifyAlbum = ModifyAlbum(applicationContext, mAlbumEntity)
            modifyAlbum.start()
            Thread.sleep(100)

            Log.d("DB", "id=$albumId")


            // ?????? ?????? ????????? ?????? ??????
            for((index, album) in MainActivity.albumList.withIndex()){

                if(album.id==albumId){
                    val temp = Albums(albumId, title, mAlbumEntity.albumColor,
                            mAlbumEntity.coverImageFileName, mAlbumEntity.frameType, mAlbumEntity.albumOrder)
                    MainActivity.albumList[index] = temp
                    break
                }
            }


            MainActivity.homeRecyclerAdapter.notifyDataSetChanged()
            MainActivity.addContentsRecyclerAdapter.notifyDataSetChanged()

            finish()
        }


        layout_title.et_title.et_title.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                layout_title.cv_string_length.visibility = View.VISIBLE
                layout_title.v_title_underline.setBackgroundColor(getColor(R.color.edit_text_underline_focus))
            }
            else{
                layout_title.cv_string_length.visibility = View.INVISIBLE
                layout_title.v_title_underline.setBackgroundColor(getColor(R.color.edit_text_underline_unfocus))
            }
        }


        // ?????? ????????? ?????? ??? ??????
        layout_title.et_title.addTextChangedListener {
            layout_title.tv_length.text = "${it?.length}/${resources.getInteger(R.integer.title_max_length)}"
        }


    }

    override fun onBackPressed() {
        if(isInitFinished){
            backButtonEvent()
        }
        else{
            finish()
        }
    }

    private fun backButtonEvent(){

        val isZoomModified = mAlbumEntity.zoom != imageCroppingView.currentZoom
        val isXModified = mAlbumEntity.x != imageCroppingView.getMatrixTransX()
        val isYModified = mAlbumEntity.y != imageCroppingView.getMatrixTransY()
        val isImageModified = (tempUri!=null || isZoomModified || isXModified || isYModified)

        val isTitleModified = layout_title.et_title.text.toString() != mAlbumEntity.albumTitle
        val isFrameModified = selectedFrameType != mAlbumEntity.frameType
        val isColorModified = selectedColor != mAlbumEntity.albumColor

        if(isImageModified || isTitleModified || isFrameModified || isColorModified){
            val intent = Intent(this, PopUpDialogActivity::class.java)
            intent.putExtra(PopUpDialogActivity.TYPE, PopUpDialogActivity.Companion.DialogType.ALBUM_MODIFY_NOT_SAVE_CHECK)
            intent.putExtra(PopUpDialogActivity.IS_NEW_ALBUM, false)
            startActivity(intent)
        }
        else{
            finish()
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        unregisterReceiver(measureReceiver)
        super.onDestroy()
    }

    private fun openImagePicker(){
        TedImagePicker.with(this)
            .start { uri ->
                val w = LayoutParamsUtils.getScreenWidth(applicationContext)
                val h = LayoutParamsUtils.getItemHeightByPercent(applicationContext, 0.479f)

                imageCroppingView.initView(applicationContext)  // ?????? ???????????? ?????????
                imageCroppingView.setImageURI(uri, w, h)

                // ????????? ???????????? ???????????? ????????? ?????? ?????? ?????? ????????????

                tempUri = uri
            }

    }

    private fun loadSavedImageState(){

        imageCroppingView.mMatrix!!.postScale(mAlbumEntity.zoom, mAlbumEntity.zoom)
        imageCroppingView.currentZoom = mAlbumEntity.zoom

        val savedX = mAlbumEntity.x
        val savedY = mAlbumEntity.y

        val initX = imageCroppingView.getMatrixTransX()
        val initY = imageCroppingView.getMatrixTransY()
        imageCroppingView.mMatrix!!.postTranslate(savedX-initX, savedY-initY)

        imageCroppingView.imageMatrix = imageCroppingView.mMatrix
    }

    private fun loadAlbumInfo(){
        // ????????? ?????? ?????? ????????????
        val albumId = intent.getLongExtra("albumId", -1)
        val getAlbumEntity = GetAlbumEntity(this, albumId)
        getAlbumEntity.start()
        Thread.sleep(100)

        // title
        layout_album_title.layout_title.et_title.setText(mAlbumEntity.albumTitle)

        // frame
        selectedFrameType = mAlbumEntity.frameType
        selectedFrameButton = when(mAlbumEntity.frameType){
            BitmapCropUtils.FRAME_TYPE_0->{
                layout_button_frame0
            }
            BitmapCropUtils.FRAME_TYPE_1->{
                layout_button_frame1
            }
            BitmapCropUtils.FRAME_TYPE_2->{
                layout_button_frame2
            }
            BitmapCropUtils.FRAME_TYPE_3->{
                layout_button_frame3
            }
            BitmapCropUtils.FRAME_TYPE_4->{
                layout_button_frame4
            }
            else->{
                layout_button_frame0
            }
        }
        setFrameButtonFocused(selectedFrameButton)

        // color
        selectedColor = mAlbumEntity.albumColor
        selectedColorButton = when(selectedColor){
            ColorUtils.PINK->{
                color_item_pink
            }
            ColorUtils.YELLOW->{
                color_item_yellow
            }
            ColorUtils.ORANGE->{
                color_item_orange
            }
            ColorUtils.BROWN->{
                color_item_brown
            }
            ColorUtils.LIGHT_PURPLE->{
                color_item_light_purple
            }
            ColorUtils.PURPLE->{
                color_item_purple
            }
            ColorUtils.GREEN->{
                color_item_green
            }
            ColorUtils.MINT->{
                color_item_mint
            }
            ColorUtils.BLUE->{
                color_item_blue
            }
            ColorUtils.NAVY->{
                color_item_navy
            }
            ColorUtils.GRAY->{
                color_item_gray
            }
            ColorUtils.BLACK->{
                color_item_black
            }
            else->{
                color_item_pink
            }
        }
        setColorButtonFocused(selectedColorButton)


    }

    inner class GetAlbumEntity(val context: Context, private val albumId : Long): Thread(){
        override fun run() {
            mAlbumEntity = AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .getAlbumEntity(albumId)
        }
    }


    inner class ModifyAlbum(val context : Context, private val entity : AlbumEntity) : Thread() {
        override fun run() {
            AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .updateAlbum(entity)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQ_GALLERY->{
                if(data!=null && resultCode==RESULT_OK){
                    val uri = data.data

                    val w = LayoutParamsUtils.getScreenWidth(applicationContext)
                    val h = LayoutParamsUtils
                            .getItemHeightByPercent(applicationContext, 0.479f)

                    imageCroppingView.initView(applicationContext)  // ?????? ???????????? ?????????
                    imageCroppingView.setImageURI(uri, w, h)

                    // test
                    tempUri = uri
                }
            }
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQ_STORAGE_PERMISSION->{
                Log.d("GALLERY", "permission result=${grantResults[0]}, granted=${PackageManager.PERMISSION_GRANTED}")

                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d("GALLERY", "permission granted")
                    // ???????????? ?????? ????????? ??????
                    openImagePicker()
                }
                else{
                    // ???????????? ??????
                    // ???????????? ?????? ????????? ???
                }
            }
        }
    }



    private fun setFrameButtonOnClickListener(){
        // ????????? ??????
        layout_button_frame0.setOnClickListener{
            selectedFrameType = BitmapCropUtils.FRAME_TYPE_0
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_0)
            setFrameButtonFocused(layout_button_frame0)
        }
        layout_button_frame1.setOnClickListener {
            selectedFrameType = BitmapCropUtils.FRAME_TYPE_1
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_1)
            setFrameButtonFocused(layout_button_frame1)
        }
        layout_button_frame2.setOnClickListener{
            selectedFrameType = BitmapCropUtils.FRAME_TYPE_2
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_2)
            setFrameButtonFocused(layout_button_frame2)
        }
        layout_button_frame3.setOnClickListener{
            selectedFrameType = BitmapCropUtils.FRAME_TYPE_3
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_3)
            setFrameButtonFocused(layout_button_frame3)
        }
        layout_button_frame4.setOnClickListener{
            selectedFrameType = BitmapCropUtils.FRAME_TYPE_4
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_4)
            setFrameButtonFocused(layout_button_frame4)
        }
    }

    private fun setColorButtonOnClickListener(){
        color_item_pink.setOnClickListener {
            setColorButtonFocused(color_item_pink)
            selectedColor = ColorUtils.PINK
        }
        color_item_yellow.setOnClickListener {
            setColorButtonFocused(color_item_yellow)
            selectedColor = ColorUtils.YELLOW
        }
        color_item_orange.setOnClickListener {
            setColorButtonFocused(color_item_orange)
            selectedColor = ColorUtils.ORANGE
        }
        color_item_brown.setOnClickListener {
            setColorButtonFocused(color_item_brown)
            selectedColor = ColorUtils.BROWN

        }
        color_item_light_purple.setOnClickListener {
            setColorButtonFocused(color_item_light_purple)
            selectedColor = ColorUtils.LIGHT_PURPLE

        }
        color_item_purple.setOnClickListener {
            setColorButtonFocused(color_item_purple)
            selectedColor = ColorUtils.PURPLE

        }
        color_item_green.setOnClickListener {
            setColorButtonFocused(color_item_green)
            selectedColor = ColorUtils.GREEN

        }
        color_item_mint.setOnClickListener {
            setColorButtonFocused(color_item_mint)
            selectedColor = ColorUtils.MINT

        }
        color_item_blue.setOnClickListener {
            setColorButtonFocused(color_item_blue)
            selectedColor = ColorUtils.BLUE

        }
        color_item_navy.setOnClickListener {
            setColorButtonFocused(color_item_navy)
            selectedColor = ColorUtils.NAVY

        }
        color_item_gray.setOnClickListener {
            setColorButtonFocused(color_item_gray)
            selectedColor = ColorUtils.GRAY

        }
        color_item_black.setOnClickListener {
            setColorButtonFocused(color_item_black)
            selectedColor = ColorUtils.BLACK

        }
    }


    private fun setFrameButtonFocused(button : View){
        selectedFrameButton.iv_frame_icon_back.setImageDrawable(null)
        button.iv_frame_icon_back.setImageDrawable(getDrawable(R.drawable.ic_button_frame_back))
        selectedFrameButton = button
    }

    private fun setColorButtonFocused(button : ColorItem){
        selectedColorButton.setUnfocused()
        button.setFocused()
        selectedColorButton = button
    }



    private fun setFrameStroke(frameType: Int){
        val frameDrawable = when(frameType){
            BitmapCropUtils.FRAME_TYPE_0->{
                resources.getDrawable(R.drawable.ic_frame0_stroke)
            }
            BitmapCropUtils.FRAME_TYPE_1->{
                resources.getDrawable(R.drawable.ic_frame1_stroke)
            }
            BitmapCropUtils.FRAME_TYPE_2->{
                resources.getDrawable(R.drawable.ic_frame2_stroke)
            }
            BitmapCropUtils.FRAME_TYPE_3->{
                resources.getDrawable(R.drawable.ic_frame3_stroke)
            }
            BitmapCropUtils.FRAME_TYPE_4->{
                resources.getDrawable(R.drawable.ic_frame4_stroke)
            }
            else->{
                resources.getDrawable(R.drawable.ic_frame0_stroke)
            }
        }
        iv_frame_stroke.setImageDrawable(frameDrawable)
    }

    private fun drawFrameBack(frameType : Int){
        // ????????? ????????? ?????? ????????? ????????? ?????? ?????? (?????????4 ????????? ????????? ?????? ?????????)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_frame_stroke, frameWidth, frameHeight)

        // ????????? ?????? ????????? ????????? ????????? ??????
        val pathData : String = BitmapCropUtils.getPathData(applicationContext, frameType)
        val path : Path = PathParser.createPathFromPathData(pathData)
        val resizedPath : Path = PathDataUtils.resizePath(path, frameWidth.toFloat(), frameHeight.toFloat())

        // ????????? ?????????
        val x0 = (viewWidth - frameWidth).toFloat() / 2
        val y0 = (viewHeight - frameHeight).toFloat() / 2

        // ????????? ?????? ?????? ??????
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        Log.d("VIEW", "CREATE ALBUM BACK viewWidth= $viewWidth, height=$viewHeight")


        // ?????? ?????? ?????? ?????? ?????????
        paint.color = getColor(R.color.image_crop_background)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // paint ?????? ???????????? ????????? ?????? (????????? ??????) paint ??????
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.translate(x0, y0)    // ????????? ????????? paint ?????? ?????? ??????
        canvas.drawPath(resizedPath, paint)

        // ????????? ????????? ????????? ????????? ????????? ???????????? ??????????????? ??????
        iv_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // ????????? ????????? ??????
        setFrameStroke(frameType)

        // CroppingView??? ????????? ?????? ??????
        imageCroppingView.setFrameStyle(frameType, frameWidth, frameHeight)

        // 4??? ????????? -> ?????? ????????? or ?????? ????????? -> 4??? ??????????????? ????????? ??? minScale ?????????
        if(frameType==BitmapCropUtils.FRAME_TYPE_4 || selectedFrameButton==layout_button_frame4){
            imageCroppingView.setScale()
        }
        // ????????? ????????? ????????? 4?????????, ????????? ????????? ?????? ????????? ???
        // ?????? ???????????? ????????? ??? ?????? ????????? ????????? ????????? ????????????
        if(selectedFrameButton==layout_button_frame4 && imageCroppingView.currentZoom<imageCroppingView.minScale){
            imageCroppingView.setZoomMinScale()
            Log.d("MAIN", "setZoomMinScale")
        }
        //
        val imageBottomY = imageCroppingView.getMatrixTransY()+imageCroppingView.getSrcImageHeight()
        val frameBottomY = imageCroppingView.y0 + frameHeight
        val imageTopY = imageCroppingView.getMatrixTransY()
        val frameTopY = imageCroppingView.y0
        if(selectedFrameButton==layout_button_frame4 && imageBottomY<=frameBottomY){
            Log.d("MOVE", "bottom $frameBottomY-$imageBottomY=${frameBottomY-imageBottomY}")
            imageCroppingView.moveImage(0f, frameBottomY-imageBottomY)
        }
        else if(selectedFrameButton==layout_button_frame4 && imageTopY>=frameTopY){
            Log.d("MOVE", "top -($imageTopY-$frameTopY)=${frameTopY-imageTopY}")
            imageCroppingView.moveImage(0f, -(imageTopY-frameTopY))
        }




    }


    private fun initTopMenuBar(){
        topMenuBarLayout = layout_top_menu_create_album
        topMenuBarTitle = topMenuBarLayout.tv_album_name_title
        topMenuBarBackButton = topMenuBarLayout.iv_icon_left
        topMenuBarOkButton = topMenuBarLayout.iv_icon_right

        topMenuBarTitle.apply{
            text = getString(R.string.modify_album)
            setTextColor(getColor(R.color.black))
        }
        topMenuBarLayout.background = getDrawable(R.color.white)
        topMenuBarBackButton.setImageDrawable(getDrawable(R.drawable.ic_back_black))
        topMenuBarOkButton.setImageDrawable(getDrawable(R.drawable.ic_confirm))
    }

    private fun initTitleLayoutMargin(){
        val commonMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COMMON_MARGIN_SIZE_PERCENT)  // ???,???,???
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_TOP_MARGIN_PERCENT)
        val etTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ET_TOP_MARGIN_PERCENT)

        LayoutParamsUtils.setItemMarginTop(layout_album_title, layoutTopMargin) // ???????????? ?????? ??????
        LayoutParamsUtils.setItemMarginStart(layout_album_title, commonMargin)   // ???????????? ?????? ?????? ??????
        LayoutParamsUtils.setItemMarginEnd(layout_album_title, commonMargin)  // ???????????? ?????? ?????? ??????
        LayoutParamsUtils.setItemMarginTop(layout_album_title.layout_title.et_title, etTopMargin)    // EditText ?????? ??????
    }

    private fun initFrameLayoutMargin(){
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_TOP_MARGIN_PERCENT)
        val itemTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_ITEM_TOP_MARGIN_PERCENT)
        val subtitleStartMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, FRAME_SUBTITLE_START_MARGIN_PERCENT)

        LayoutParamsUtils.setItemMarginTop(layout_album_frame, layoutTopMargin) // ???????????? ?????? ??????
        LayoutParamsUtils.setItemMarginBottom(layout_album_frame.tv_album_frame_title, itemTopMargin)   // ????????? ????????? ?????? ??????
        LayoutParamsUtils.setItemMarginStart(layout_album_frame.tv_album_frame_subtitle, subtitleStartMargin)  // ????????? ?????? ??????
    }


    private fun initColorLayoutMargin(){
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, COLOR_TOP_MARGIN_PERCENT)
        val layoutBottomMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COMMON_MARGIN_SIZE_PERCENT)
        val itemTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, COLOR_ITEM_TOP_MARGIN_PERCENT)
        val colorItemSize = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COLOR_ITEM_SIZE_PERCENT)
        val itemVerticalMargin = LayoutParamsUtils.getItemSizeByRatio(colorItemSize, COLOR_ITEM_BOTTOM_MARGIN_RATIO)

        LayoutParamsUtils.setItemMarginTop(layout_album_color, layoutTopMargin) // ???????????? ?????? ??????
        LayoutParamsUtils.setItemMarginBottom(layout_album_color, layoutBottomMargin)   // ???????????? ?????? ??????
        LayoutParamsUtils.setItemMarginTop(layout_album_color.cl_colors, itemTopMargin)   // ????????? ?????? ??????
        LayoutParamsUtils.setItemMarginTop(color_item_green, itemVerticalMargin)    // ???????????? ?????? ??????
    }

    private fun initFrameButtons(){
        val titleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_FONT_SIZE_PERCENT)
        val frameSubtitleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_SUBTITLE_FONT_SIZE_PERCENT)
        val frameTitleBottomMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_ITEM_TOP_MARGIN_PERCENT)
        tv_album_frame_title.setTextSize(Dimension.DP, titleFontSize.toFloat())
        tv_album_frame_subtitle.setTextSize(Dimension.DP, frameSubtitleFontSize.toFloat())
        LayoutParamsUtils.setItemMarginTop(layout_album_frame.cl_frame_icons, frameTitleBottomMargin)

        tv_album_color_title.setTextSize(Dimension.DP, titleFontSize.toFloat())

        layout_button_frame1.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame1_button))
        layout_button_frame2.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame2_button))
        layout_button_frame3.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame3_button))
        layout_button_frame4.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame4_button))
    }

    private fun initColorButtons(){
        // ????????? ???
        color_item_pink.setColor(resources.getColor(R.color.album_color_pink))
        color_item_yellow.setColor(resources.getColor(R.color.album_color_yellow))
        color_item_orange.setColor(resources.getColor(R.color.album_color_orange))
        color_item_brown.setColor(resources.getColor(R.color.album_color_brown))
        color_item_light_purple.setColor(resources.getColor(R.color.album_color_light_purple))
        color_item_purple.setColor(resources.getColor(R.color.album_color_purple))

        // ????????? ???
        color_item_green.setColor(resources.getColor(R.color.album_color_green))
        color_item_mint.setColor(resources.getColor(R.color.album_color_mint))
        color_item_blue.setColor(resources.getColor(R.color.album_color_blue))
        color_item_navy.setColor(resources.getColor(R.color.album_color_navy))
        color_item_gray.setColor(resources.getColor(R.color.album_color_gray))
        color_item_black.setColor(resources.getColor(R.color.black))
    }

    private fun setColorButtonSize(size : Int){
        LayoutParamsUtils.setItemSize(color_item_pink, size, size)
        LayoutParamsUtils.setItemSize(color_item_yellow, size, size)
        LayoutParamsUtils.setItemSize(color_item_orange, size, size)
        LayoutParamsUtils.setItemSize(color_item_brown, size, size)
        LayoutParamsUtils.setItemSize(color_item_light_purple, size, size)
        LayoutParamsUtils.setItemSize(color_item_purple, size, size)
        LayoutParamsUtils.setItemSize(color_item_green, size, size)
        LayoutParamsUtils.setItemSize(color_item_mint, size, size)
        LayoutParamsUtils.setItemSize(color_item_blue, size, size)
        LayoutParamsUtils.setItemSize(color_item_navy, size, size)
        LayoutParamsUtils.setItemSize(color_item_gray, size, size)
        LayoutParamsUtils.setItemSize(color_item_black, size, size)
    }

}
