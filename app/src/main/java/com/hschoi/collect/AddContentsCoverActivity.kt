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
import androidx.annotation.Dimension
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
import kotlinx.android.synthetic.main.activity_create_new_album.*
import kotlinx.android.synthetic.main.layout_create_new_album_color.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.view.*
import kotlinx.android.synthetic.main.layout_frame_select_button.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddContentsCoverActivity : AppCompatActivity() {
    companion object{
        private const val IMAGE_CROPPING_VIEW_HEIGHT_RATIO = 272f/716f

        private const val FRAME_WIDTH_RATIO = 114f/234f
        private const val FRAME_HEIGHT_PERCENT = 234f/716f    // 너비 * ratio = 높이

        private const val FRAME4_WIDTH_RATIO = 114f/230f
        private const val FRAME4_HEIGHT_PERCENT = 230f/716f    // 너비 * ratio = 높이

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
    private var selectedFrameType = BitmapCropUtils.FRAME_TYPE_0


    private var measureReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action==ImageCroppingView.ACTION_MEASURE && AddContentsActivity.isModify){
                if(originCoverExists){
                    loadSavedImageState()
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

        selectedFrameButton = layout_button_frame0   // 액티비티 실행 시 프레임 스타일 초기값
        setFrameButtonOnClickListener()
        initFrameButtons()


        mContentsId = intent.getLongExtra("contentsId", -1)
        mAlbumId = intent.getLongExtra("albumId", -1)

        GetAlbumEntity(this, mAlbumId).start()
        Thread.sleep(100)


        mAlbumTitle = mAlbumEntity.albumTitle
//        mFrameType = mAlbumEntity.frameType
        mAlbumColor = mAlbumEntity.albumColor

        // 컨텐츠 추가 액티비티에서 전달된 데이터 받음
        if(AddContentsActivity.isModify){   // 컨텐츠 수정일 경우
            GetAlbumItemEntity(this, mContentsId).start()
            Thread.sleep(100)
        }

        val filter = IntentFilter()
        filter.addAction(ImageCroppingView.ACTION_MEASURE)
        registerReceiver(measureReceiver, filter)

        mContentsDate = intent.getStringExtra("contentsDate")
        mContentsTitle = intent.getStringExtra("contentsTitle")
        mContentsSentence = intent.getStringExtra("contentsSentence")


        // 상태바 색상 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black_status)

        // 타이틀바 설정
        layout_top_menu_set_cover.apply {
            setBackgroundColor(getColor(R.color.black))
            tv_album_name_title.text = getString(R.string.set_cover_image)
            iv_icon_left.setImageDrawable(getDrawable(R.drawable.ic_back_white))
            iv_icon_right.setImageDrawable(getDrawable(R.drawable.ic_confirm_white))
        }

        // add button 눌러서 이미지 받아옴(imagePath)
        // setImageUri or .... 이용해서 소스 이미지 설정해줌
        // viewWidth, viewHeight 설정해줌
        // frameWidth, frameHeight 설정해줌

        viewWidth = LayoutParamsUtils.getScreenWidth(this)
        viewHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, IMAGE_CROPPING_VIEW_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(cl_cover_image, viewWidth, viewHeight)
        LayoutParamsUtils.setItemSize(icv_cover_image_source, viewWidth, viewHeight)

        frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
        frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        // CroppingView에 프레임 정보 전달
        icv_cover_image_source.setFrameStyle(BitmapCropUtils.FRAME_TYPE_0, frameWidth, frameHeight)
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
            // zoom, x, y 불러와서 저장했을 때 상태로 적용시키기 (broadcast 수신시)
        }
        else{
            val bitmap = BitmapFactory.decodeFile("${filesDir}/${AddContentsActivity.imageList[0]}")
            icv_cover_image_source.setImageBitmap(bitmap)
        }

        rv_contents_image.adapter = adapter
        rv_contents_image.layoutManager = GridLayoutManager(this, 3)

        
        // 뒤로가기 버튼
        layout_top_menu_set_cover.cl_icon_left.setOnClickListener {
            finish()
        }

        // 저장 버튼
        layout_top_menu_set_cover.cl_icon_right.setOnClickListener {
            // 최종적으로 추가한 이미지 저장
            AddContentsActivity.isSaved = true


            // 내부저장소에 컨텐츠 커버이미지 저장
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



            // ** 데이터베이스에 컨텐츠 정보 저장
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


            // 홈 하단 메뉴바의 컨텐츠 추가 버튼 눌러서 추가하는 경우
            if(MainActivity.isAddFromHome){
                val intent = Intent(this, AlbumFeedActivity::class.java)
                intent.putExtra("albumId", mAlbumId)
                intent.putExtra("albumTitle", mAlbumTitle)
                intent.putExtra("frameType", mFrameType)
                intent.putExtra("color", mAlbumColor)

                startActivity(intent)
                MainActivity.isAddFromHome = false
            }


            // 액티비티 종료
            // 이전 액티비였던 컨텐츠 추가 액티비티도 함께 종료시킴
            val addContentsActivity = AddContentsActivity.addContentsActivity as AddContentsActivity
            AddContentsActivity.isModify = false
            addContentsActivity.finish()

            finish()


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

    // 이거 왜 추가한거지..?
    /*override fun onWindowFocusChanged(hasFocus: Boolean) {
        // onCreate 에서 할 경우, width, height 값이 0으로 나오기 때문에 onWindowFocusChanged 에서 호출해줌
        if(!isAlreadyDone){ // 최초 1회만 실행
            drawFrameBack(frameType)
            isAlreadyDone = true
        }
        Log.d("TAG", "onWindowFocusChanged 호출됨")
    }*/

    private fun initFrameButtons(){
        layout_button_frame0.iv_frame_icon_back.setImageDrawable(getDrawable(R.drawable.ic_button_frame_back))
        layout_button_frame1.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame1_button))
        layout_button_frame2.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame2_button))
        layout_button_frame3.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame3_button))
        layout_button_frame4.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame4_button))
    }


    private fun setFrameButtonOnClickListener(){
        // 프레임 버튼
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
    private fun setFrameButtonFocused(button : View){
        selectedFrameButton.iv_frame_icon_back.setImageDrawable(null)
        button.iv_frame_icon_back.setImageDrawable(getDrawable(R.drawable.ic_button_frame_back))
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
        // 프레임 타입에 따라 프레임 사이즈 달리 설정 (프레임4 외에는 사이즈 모두 동일함)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_cover_image_frame_stroke, frameWidth, frameHeight)

        // 프레임 모양 가져와 알맞은 크기로 변경
        val pathData : String = BitmapCropUtils.getPathData(applicationContext, frameType)
        val path : Path = PathParser.createPathFromPathData(pathData)
        val resizedPath : Path = PathDataUtils.resizePath(path, frameWidth.toFloat(), frameHeight.toFloat())

        // 프레임 시작점
        val x0 = (viewWidth - frameWidth).toFloat() / 2
        val y0 = (viewHeight - frameHeight).toFloat() / 2

        // 그리기 관련 객체 생성
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()


        // 크롭 박스 외부 영역 채우기
        paint.color = getColor(R.color.image_crop_background)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // paint 모드 변경해서 겹치는 영역 (프레임 영역) paint 제거
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.translate(x0, y0)    // 프레임 이미지 paint 시작 좌표 이동
        canvas.drawPath(resizedPath, paint)

        // 캔버스 위에서 작업한 비트맵 프레임 이미지를 이미지뷰에 설정
        iv_cover_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // 프레임 테두리 설정
        setFrameStroke(frameType)

        // CroppingView에 프레임 정보 전달
        icv_cover_image_source.setFrameStyle(frameType, frameWidth, frameHeight)
    }*/

    private fun drawFrameBack(frameType : Int){
        // 프레임 타입에 따라 프레임 사이즈 달리 설정 (프레임4 외에는 사이즈 모두 동일함)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, CreateNewAlbumActivity.FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, CreateNewAlbumActivity.FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, CreateNewAlbumActivity.FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, CreateNewAlbumActivity.FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_cover_image_frame_stroke, frameWidth, frameHeight)

        // 프레임 모양 가져와 알맞은 크기로 변경
        val pathData : String = BitmapCropUtils.getPathData(applicationContext, frameType)
        val path : Path = PathParser.createPathFromPathData(pathData)
        val resizedPath : Path = PathDataUtils.resizePath(path, frameWidth.toFloat(), frameHeight.toFloat())

        // 프레임 시작점
        val x0 = (viewWidth - frameWidth).toFloat() / 2
        val y0 = (viewHeight - frameHeight).toFloat() / 2

        // 그리기 관련 객체 생성
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        Log.d("VIEW", "CREATE ALBUM BACK viewWidth= $viewWidth, height=$viewHeight")


        // 크롭 박스 외부 영역 채우기
        paint.color = getColor(R.color.image_crop_background)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // paint 모드 변경해서 겹치는 영역 (프레임 영역) paint 제거
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.translate(x0, y0)    // 프레임 이미지 paint 시작 좌표 이동
        canvas.drawPath(resizedPath, paint)

        // 캔버스 위에서 작업한 비트맵 프레임 이미지를 이미지뷰에 설정
        iv_cover_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // 프레임 테두리 설정
        setFrameStroke(frameType)

        // CroppingView에 프레임 정보 전달
        imageCroppingView.setFrameStyle(frameType, frameWidth, frameHeight)

         // 4번 프레임 -> 다른 프레임 or 다른 프레임 -> 4번 프레임으로 변경할 때 minScale 재설정
         if(frameType==BitmapCropUtils.FRAME_TYPE_4 || selectedFrameButton==layout_button_frame4){
             imageCroppingView.setScale()
         }

         // 이전에 선택된 프레임 4번이고, 스케일 최대로 줄인 상태일 때
         // 다른 프레임은 높이가 더 높기 때문에 이미지 그만큼 확대해줌
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
