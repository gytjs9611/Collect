package com.hschoi.collect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.PathParser
import androidx.recyclerview.widget.GridLayoutManager
import com.hschoi.collect.adapter.CoverImageAdapter
import com.hschoi.collect.customview.ImageCroppingView
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.database.entity.AlbumItemEntity
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.PathDataUtils
import kotlinx.android.synthetic.main.activity_add_contents_cover.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.layout_button_frame0
import kotlinx.android.synthetic.main.layout_create_new_album_frame.layout_button_frame1
import kotlinx.android.synthetic.main.layout_create_new_album_frame.layout_button_frame2
import kotlinx.android.synthetic.main.layout_create_new_album_frame.layout_button_frame3
import kotlinx.android.synthetic.main.layout_create_new_album_frame.layout_button_frame4
import kotlinx.android.synthetic.main.layout_frame_buttons.*
import kotlinx.android.synthetic.main.layout_frame_select_button.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddContentsCoverActivity : AppCompatActivity() {
    companion object{
        private const val IMAGE_CROPPING_VIEW_HEIGHT_RATIO = 272f/716f

        private const val FRAME_WIDTH_RATIO = 114f/234f
        private const val FRAME_HEIGHT_PERCENT = 234f/716f    // λλΉ * ratio = λμ΄

        private const val FRAME4_WIDTH_RATIO = 114f/230f
        private const val FRAME4_HEIGHT_PERCENT = 230f/716f    // λλΉ * ratio = λμ΄

        lateinit var imageCroppingView : ImageCroppingView
    }

    private lateinit var mAlbumEntity: AlbumEntity
    private lateinit var mAlbumItemEntity: AlbumItemEntity

    private var mAlbumId: Long = -1
    private var mContentsId: Long = -1

    private var mAlbumTitle : String? = null
    private var mFrameType = BitmapCropUtils.FRAME_TYPE_0
    private var mAlbumColor = -1


    private var mContentsDate : String? = null
    private var mContentsTitle : String? = null
    private var mContentsSentence : String? = null

    private var viewWidth = 0
    private var viewHeight = 0
    private var frameWidth = 0
    private var frameHeight = 0

    private var originCoverExists = false

    private lateinit var adapter: CoverImageAdapter

    private lateinit var selectedFrameButton : View
//    private var selectedFrameType = BitmapCropUtils.FRAME_TYPE_0

    private var isOpened = false


    private var measureReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action==ImageCroppingView.ACTION_MEASURE){
                if(AddContentsActivity.isModify){   // μ»¨νμΈ  μμ 
                    if(originCoverExists){
                        loadSavedImageState()
                        drawFrameBack(mFrameType)
                    }
                }
                else{   // μ»¨νμΈ  μμ±
                    drawFrameBack(mFrameType)
                }
            }
        }
    }


    inner class GetAlbumItemEntity(private val context: Context, private val contentsId:Long):Thread(){
        override fun run() {
            mAlbumItemEntity = AlbumDatabase.getInstance(context)!!
                    .albumItemDao()
                    .getAlbumItemEntity(contentsId)
        }
    }

    inner class GetAlbumEntity(private val context: Context, private val albumId:Long):Thread(){
        override fun run() {
            mAlbumEntity = AlbumDatabase.getInstance(context)!!
                    .albumDao()
                    .getAlbumEntity(albumId)
        }
    }

    inner class UpdateAlbumItem(private val context: Context, private val albumItemEntity:AlbumItemEntity):Thread(){
        override fun run() {
            AlbumDatabase.getInstance(context)!!
                    .albumItemDao()
                    .updateContents(albumItemEntity)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contents_cover)
        imageCroppingView = icv_cover_image_source

        selectedFrameButton = layout_button_frame0   // μ‘ν°λΉν° μ€ν μ νλ μ μ€νμΌ μ΄κΈ°κ°
        setFrameButtonOnClickListener()
        initFrameButtons()


        mContentsId = intent.getLongExtra("contentsId", -1)
        mAlbumId = intent.getLongExtra("albumId", -1)

        GetAlbumEntity(this, mAlbumId).start()
        Thread.sleep(100)


        mAlbumTitle = mAlbumEntity.albumTitle
        mFrameType = mAlbumEntity.frameType
        mAlbumColor = mAlbumEntity.albumColor



        // μ»¨νμΈ  μΆκ° μ‘ν°λΉν°μμ μ λ¬λ λ°μ΄ν° λ°μ
        if(AddContentsActivity.isModify){   // μ»¨νμΈ  μμ μΌ κ²½μ°
            GetAlbumItemEntity(this, mContentsId).start()
            Thread.sleep(100)

            mFrameType = mAlbumItemEntity.frameType
        }

        selectedFrameButton = getFrameButton(mFrameType)
        setFrameButtonFocused(selectedFrameButton)


        val filter = IntentFilter()
        filter.addAction(ImageCroppingView.ACTION_MEASURE)
        registerReceiver(measureReceiver, filter)

        mContentsDate = intent.getStringExtra("contentsDate")
        mContentsTitle = intent.getStringExtra("contentsTitle")
        mContentsSentence = intent.getStringExtra("contentsSentence")


        // μνλ° μμ μ€μ 
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black_status)

        // νμ΄νλ° μ€μ 
        layout_top_menu_set_cover.apply {
            setBackgroundColor(getColor(R.color.black))
            tv_album_name_title.text = getString(R.string.set_cover_image)
            iv_icon_left.setImageDrawable(getDrawable(R.drawable.ic_back_white))
            iv_icon_right.setImageDrawable(getDrawable(R.drawable.ic_confirm_white))
        }

        // add button λλ¬μ μ΄λ―Έμ§ λ°μμ΄(imagePath)
        // setImageUri or .... μ΄μ©ν΄μ μμ€ μ΄λ―Έμ§ μ€μ ν΄μ€
        // viewWidth, viewHeight μ€μ ν΄μ€
        // frameWidth, frameHeight μ€μ ν΄μ€

        viewWidth = LayoutParamsUtils.getScreenWidth(this)
        viewHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, IMAGE_CROPPING_VIEW_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(cl_cover_image, viewWidth, viewHeight)
        LayoutParamsUtils.setItemSize(icv_cover_image_source, viewWidth, viewHeight)

        frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
        frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        // CroppingViewμ νλ μ μ λ³΄ μ λ¬
        icv_cover_image_source.setFrameStyle(mFrameType, frameWidth, frameHeight)
        drawFrameBack(mFrameType)



        adapter = CoverImageAdapter(AddContentsActivity.imageList)


        if(AddContentsActivity.isModify){
            var coverIdx = 0
            for((index, item) in AddContentsActivity.imageList.withIndex()){
                if(item.substringAfter("temp_")==AddContentsActivity.originCoverImage){
                    coverIdx = index
                    originCoverExists = true
                    break
                }
            }
            adapter.selectedPosition = coverIdx


            val bitmap = BitmapFactory.decodeFile("${filesDir}/${AddContentsActivity.imageList[coverIdx]}")
            icv_cover_image_source.setImageBitmap(bitmap)
            // zoom, x, y λΆλ¬μμ μ μ₯νμ λ μνλ‘ μ μ©μν€κΈ° (broadcast μμ μ)
        }
        else{
            val bitmap = BitmapFactory.decodeFile("${filesDir}/${AddContentsActivity.imageList[0]}")
            icv_cover_image_source.setImageBitmap(bitmap)
        }

        rv_contents_image.adapter = adapter
        rv_contents_image.layoutManager = GridLayoutManager(this, 3)

        
        // λ€λ‘κ°κΈ° λ²νΌ
        layout_top_menu_set_cover.cl_icon_left.setOnClickListener {
            finish()
        }

        // μ μ₯ λ²νΌ
        layout_top_menu_set_cover.cl_icon_right.setOnClickListener {
            // μ΅μ’μ μΌλ‘ μΆκ°ν μ΄λ―Έμ§ μ μ₯
            AddContentsActivity.isSaved = true


            // λ΄λΆμ μ₯μμ μ»¨νμΈ  μ»€λ²μ΄λ―Έμ§ μ μ₯
            var coverImageName: String
            if(AddContentsActivity.isModify){
                coverImageName = mAlbumItemEntity.coverImageName
            }
            else{
                coverImageName = "contents_cover_${mAlbumId}_${mContentsTitle}.png"
                var coverCnt = 1
                var coverFile = File("${applicationContext.filesDir}/$coverImageName")
                val coverName = coverFile.nameWithoutExtension
                while(coverFile.exists()){
                    coverImageName = "${coverName}_${coverCnt}.png"
                    coverFile = File("${applicationContext.filesDir}/$coverImageName")
                    coverCnt++
                }
            }

            val fos2 = openFileOutput(coverImageName, Context.MODE_PRIVATE)
            icv_cover_image_source.croppedImage
                    .compress(Bitmap.CompressFormat.JPEG, 50, fos2)
            fos2.close()



            // ** λ°μ΄ν°λ² μ΄μ€μ μ»¨νμΈ  μ λ³΄ μ μ₯
            val time = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())

            var contentsImages = ""
            val size = AddContentsActivity.imageList.size
            for((index, name) in AddContentsActivity.imageList.withIndex()){
                contentsImages+=name.substringAfter("temp_")
                if(index!=size-2) contentsImages+="|"
                else break
            }

            val zoom: Float = icv_cover_image_source.currentZoom
            val x: Float = icv_cover_image_source.getMatrixTransX()
            val y: Float = icv_cover_image_source.getMatrixTransY()


            if(AddContentsActivity.isModify){
                val selectedIdx = adapter.selectedPosition

                mAlbumItemEntity.coverImageName = coverImageName
                mAlbumItemEntity.contentsImageName = contentsImages
                mAlbumItemEntity.coverImageIndex = selectedIdx
                mAlbumItemEntity.contentsSentence = mContentsSentence!!
                mAlbumItemEntity.contentsDate = "$mContentsDate/$time"
                mAlbumItemEntity.contentsTitle = mContentsTitle!!
                mAlbumItemEntity.frameType = mFrameType
                mAlbumItemEntity.zoom = zoom
                mAlbumItemEntity.x = x
                mAlbumItemEntity.y = y

                UpdateAlbumItem(this, mAlbumItemEntity).start()
                Thread.sleep(100)

                GetAlbumItemEntity(this, mContentsId).start()
                Thread.sleep(100)

            }
            else{
                val selectedIndex = adapter.selectedPosition
                val albumItemEntity = AlbumItemEntity(mAlbumId, mAlbumTitle!!, mContentsTitle!!,
                        "$mContentsDate/$time", mContentsSentence!!,
                                    coverImageName, selectedIndex, contentsImages, mFrameType,
                                    zoom, x, y)
                val addContents = AddContents(applicationContext, albumItemEntity)
                addContents.start()
                Thread.sleep(100)
            }

            AlbumFeedActivity.activity.loadAlbumFeed()


            // ν νλ¨ λ©λ΄λ°μ μ»¨νμΈ  μΆκ° λ²νΌ λλ¬μ μΆκ°νλ κ²½μ°
            if(MainActivity.isAddFromHome){
                val intent = Intent(this, AlbumFeedActivity::class.java)
                intent.putExtra("albumId", mAlbumId)
                intent.putExtra("albumTitle", mAlbumTitle)
                intent.putExtra("frameType", mFrameType)
                intent.putExtra("color", mAlbumColor)

                startActivity(intent)
                MainActivity.isAddFromHome = false
            }


            // μ‘ν°λΉν° μ’λ£
            // μ΄μ  μ‘ν°λΉμλ μ»¨νμΈ  μΆκ° μ‘ν°λΉν°λ ν¨κ» μ’λ£μν΄
            val addContentsActivity = AddContentsActivity.activity as AddContentsActivity
            AddContentsActivity.isModify = false
            addContentsActivity.finish()

            finish()


        }


        // νλ μ λ³΄κΈ° λ²νΌ
        cl_contents_frame_title.setOnClickListener {
            if(isOpened){   // λ«κΈ°
                iv_frame_show_icon.setImageDrawable(getDrawable(R.drawable.ic_down))
                layout_contents_frame.visibility = View.GONE
                v_frame_title_bottom.visibility = View.VISIBLE
                isOpened = false
            }
            else{   // μ΄κΈ°
                iv_frame_show_icon.setImageDrawable(getDrawable(R.drawable.ic_up))
                layout_contents_frame.visibility = View.VISIBLE
                v_frame_title_bottom.visibility = View.GONE
                isOpened = true
            }
        }

    }


    inner class AddContents(val context: Context, private val entity: AlbumItemEntity): Thread(){
        override fun run(){
            mContentsId = AlbumDatabase
                    .getInstance(context)!!
                    .albumItemDao()
                    .insertContents(entity)
        }
    }



    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        unregisterReceiver(measureReceiver)
        super.onDestroy()
    }

    // μ΄κ±° μ μΆκ°νκ±°μ§..?
    /*override fun onWindowFocusChanged(hasFocus: Boolean) {
        // onCreate μμ ν  κ²½μ°, width, height κ°μ΄ 0μΌλ‘ λμ€κΈ° λλ¬Έμ onWindowFocusChanged μμ νΈμΆν΄μ€
        if(!isAlreadyDone){ // μ΅μ΄ 1νλ§ μ€ν
            drawFrameBack(frameType)
            isAlreadyDone = true
        }
        Log.d("TAG", "onWindowFocusChanged νΈμΆλ¨")
    }*/

    private fun initFrameButtons(){
        val icon0 = getDrawable(R.drawable.ic_frame0_button)
        val icon1 = getDrawable(R.drawable.ic_frame1_button)
        val icon2 = getDrawable(R.drawable.ic_frame2_button)
        val icon3 = getDrawable(R.drawable.ic_frame3_button)
        val icon4 = getDrawable(R.drawable.ic_frame4_button)

        val strokeColor = getColor(R.color.white)
        icon0?.setTint(strokeColor)
        icon1?.setTint(strokeColor)
        icon2?.setTint(strokeColor)
        icon3?.setTint(strokeColor)
        icon4?.setTint(strokeColor)

        layout_button_frame0.iv_frame_icon.setImageDrawable(icon0)
        layout_button_frame1.iv_frame_icon.setImageDrawable(icon1)
        layout_button_frame2.iv_frame_icon.setImageDrawable(icon2)
        layout_button_frame3.iv_frame_icon.setImageDrawable(icon3)
        layout_button_frame4.iv_frame_icon.setImageDrawable(icon4)
    }


    private fun setFrameButtonOnClickListener(){
        // νλ μ λ²νΌ
        layout_button_frame0.setOnClickListener{
            mFrameType = BitmapCropUtils.FRAME_TYPE_0
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_0)
            setFrameButtonFocused(layout_button_frame0)
        }
        layout_button_frame1.setOnClickListener {
            mFrameType = BitmapCropUtils.FRAME_TYPE_1
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_1)
            setFrameButtonFocused(layout_button_frame1)
        }
        layout_button_frame2.setOnClickListener{
            mFrameType = BitmapCropUtils.FRAME_TYPE_2
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_2)
            setFrameButtonFocused(layout_button_frame2)
        }
        layout_button_frame3.setOnClickListener{
            mFrameType = BitmapCropUtils.FRAME_TYPE_3
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_3)
            setFrameButtonFocused(layout_button_frame3)
        }
        layout_button_frame4.setOnClickListener{
            mFrameType = BitmapCropUtils.FRAME_TYPE_4
            drawFrameBack(BitmapCropUtils.FRAME_TYPE_4)
            setFrameButtonFocused(layout_button_frame4)
        }
    }

    private fun getFrameButton(type: Int): View{
        return when(type){
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
    }

    private fun setFrameButtonFocused(button : View){
        selectedFrameButton.iv_frame_icon_back.setImageDrawable(null)
        button.iv_frame_icon_back.setImageDrawable(getDrawable(R.drawable.ic_button_contents_frame_back))
        selectedFrameButton = button
    }

    private fun loadSavedImageState(){

        icv_cover_image_source.mMatrix!!.postScale(mAlbumItemEntity.zoom, mAlbumItemEntity.zoom)
        icv_cover_image_source.currentZoom = mAlbumItemEntity.zoom

        val savedX = mAlbumItemEntity.x
        val savedY = mAlbumItemEntity.y

        val initX = icv_cover_image_source.getMatrixTransX()
        val initY = icv_cover_image_source.getMatrixTransY()
        icv_cover_image_source.mMatrix!!.postTranslate(savedX-initX, savedY-initY)

        icv_cover_image_source.imageMatrix = icv_cover_image_source.mMatrix
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
        iv_cover_image_frame_stroke.setImageDrawable(frameDrawable)
    }

    /*private fun drawFrameBack(frameType : Int){
        // νλ μ νμμ λ°λΌ νλ μ μ¬μ΄μ¦ λ¬λ¦¬ μ€μ  (νλ μ4 μΈμλ μ¬μ΄μ¦ λͺ¨λ λμΌν¨)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_cover_image_frame_stroke, frameWidth, frameHeight)

        // νλ μ λͺ¨μ κ°μ Έμ μλ§μ ν¬κΈ°λ‘ λ³κ²½
        val pathData : String = BitmapCropUtils.getPathData(applicationContext, frameType)
        val path : Path = PathParser.createPathFromPathData(pathData)
        val resizedPath : Path = PathDataUtils.resizePath(path, frameWidth.toFloat(), frameHeight.toFloat())

        // νλ μ μμμ 
        val x0 = (viewWidth - frameWidth).toFloat() / 2
        val y0 = (viewHeight - frameHeight).toFloat() / 2

        // κ·Έλ¦¬κΈ° κ΄λ ¨ κ°μ²΄ μμ±
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()


        // ν¬λ‘­ λ°μ€ μΈλΆ μμ­ μ±μ°κΈ°
        paint.color = getColor(R.color.image_crop_background)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // paint λͺ¨λ λ³κ²½ν΄μ κ²ΉμΉλ μμ­ (νλ μ μμ­) paint μ κ±°
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.translate(x0, y0)    // νλ μ μ΄λ―Έμ§ paint μμ μ’ν μ΄λ
        canvas.drawPath(resizedPath, paint)

        // μΊλ²μ€ μμμ μμν λΉνΈλ§΅ νλ μ μ΄λ―Έμ§λ₯Ό μ΄λ―Έμ§λ·°μ μ€μ 
        iv_cover_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // νλ μ νλλ¦¬ μ€μ 
        setFrameStroke(frameType)

        // CroppingViewμ νλ μ μ λ³΄ μ λ¬
        icv_cover_image_source.setFrameStyle(frameType, frameWidth, frameHeight)
    }*/

    private fun drawFrameBack(frameType : Int){
        // νλ μ νμμ λ°λΌ νλ μ μ¬μ΄μ¦ λ¬λ¦¬ μ€μ  (νλ μ4 μΈμλ μ¬μ΄μ¦ λͺ¨λ λμΌν¨)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, CreateNewAlbumActivity.FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, CreateNewAlbumActivity.FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, CreateNewAlbumActivity.FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, CreateNewAlbumActivity.FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_cover_image_frame_stroke, frameWidth, frameHeight)

        // νλ μ λͺ¨μ κ°μ Έμ μλ§μ ν¬κΈ°λ‘ λ³κ²½
        val pathData : String = BitmapCropUtils.getPathData(applicationContext, frameType)
        val path : Path = PathParser.createPathFromPathData(pathData)
        val resizedPath : Path = PathDataUtils.resizePath(path, frameWidth.toFloat(), frameHeight.toFloat())

        // νλ μ μμμ 
        val x0 = (viewWidth - frameWidth).toFloat() / 2
        val y0 = (viewHeight - frameHeight).toFloat() / 2

        // κ·Έλ¦¬κΈ° κ΄λ ¨ κ°μ²΄ μμ±
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        Log.d("VIEW", "CREATE ALBUM BACK viewWidth= $viewWidth, height=$viewHeight")


        // ν¬λ‘­ λ°μ€ μΈλΆ μμ­ μ±μ°κΈ°
        paint.color = getColor(R.color.image_crop_background)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // paint λͺ¨λ λ³κ²½ν΄μ κ²ΉμΉλ μμ­ (νλ μ μμ­) paint μ κ±°
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.translate(x0, y0)    // νλ μ μ΄λ―Έμ§ paint μμ μ’ν μ΄λ
        canvas.drawPath(resizedPath, paint)

        // μΊλ²μ€ μμμ μμν λΉνΈλ§΅ νλ μ μ΄λ―Έμ§λ₯Ό μ΄λ―Έμ§λ·°μ μ€μ 
        iv_cover_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // νλ μ νλλ¦¬ μ€μ 
        setFrameStroke(frameType)

        // CroppingViewμ νλ μ μ λ³΄ μ λ¬
        imageCroppingView.setFrameStyle(frameType, frameWidth, frameHeight)

         // 4λ² νλ μ -> λ€λ₯Έ νλ μ or λ€λ₯Έ νλ μ -> 4λ² νλ μμΌλ‘ λ³κ²½ν  λ minScale μ¬μ€μ 
         if(selectedFrameButton!=layout_button_frame4 && frameType==BitmapCropUtils.FRAME_TYPE_4
                 || selectedFrameButton==layout_button_frame4 && frameType!=BitmapCropUtils.FRAME_TYPE_4){
             imageCroppingView.setScale()
         }

         // μ΄μ μ μ νλ νλ μ 4λ²μ΄κ³ , μ€μΌμΌ μ΅λλ‘ μ€μΈ μνμΌ λ
         // λ€λ₯Έ νλ μμ λμ΄κ° λ λκΈ° λλ¬Έμ μ΄λ―Έμ§ κ·Έλ§νΌ νλν΄μ€
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

}
