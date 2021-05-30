package com.hschoi.collect


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.hschoi.collect.customview.ImageCroppingView
import com.hschoi.collect.customview.ColorItem
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.BitmapUtils
import com.hschoi.collect.util.LayoutParamsUtils
import com.hschoi.collect.util.PathDataUtils
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_add_contents.*
import kotlinx.android.synthetic.main.activity_create_new_album.*
import kotlinx.android.synthetic.main.layout_create_new_album_color.*
import kotlinx.android.synthetic.main.layout_create_new_album_color.view.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.*
import kotlinx.android.synthetic.main.layout_create_new_album_frame.view.*
import kotlinx.android.synthetic.main.layout_create_new_album_title.view.*
import kotlinx.android.synthetic.main.layout_frame_select_button.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.tv_album_name_title
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class CreateNewAlbumActivity : AppCompatActivity() {

    companion object{
        private const val IMAGE_CROPPING_VIEW_HEIGHT_RATIO = 272f/716f

        private const val ADD_IMAGE_ICON_HEIGHT_PERCENT = 26f/716f
        private const val ADD_IMAGE_SWITCH_ICON_HEIGHT_PERCENT = 38f/716f

        private const val FRAME_WIDTH_RATIO = 114f/234f
        private const val FRAME_HEIGHT_PERCENT = 234f/716f    // 너비 * ratio = 높이
        
        private const val FRAME4_WIDTH_RATIO = 114f/230f
        private const val FRAME4_HEIGHT_PERCENT = 230f/716f    // 너비 * ratio = 높이

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



//        갤러리 접근
        const val REQ_STORAGE_PERMISSION = 100
        const val REQ_GALLERY = 101
    }


    private lateinit var topMenuBarLayout : View
    private lateinit var topMenuBarTitle : TextView
    private lateinit var topMenuBarBackButton : ImageView
    private lateinit var topMenuBarOkButton : ImageView


    private var albumId : Long = -1

    private var selectedFrameType = BitmapCropUtils.FRAME_TYPE_0
    private var viewWidth = 0
    private var viewHeight = 0
    private var frameWidth = 0
    private var frameHeight = 0

    private lateinit var selectedFrameButton : View
    private lateinit var selectedColorButton : ColorItem
    private var selectedColor = -1

    private var isImageSelected = false

    private lateinit var imageCroppingView : ImageCroppingView

    // test
    private var tempUri : Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_album)


        imageCroppingView = ImageCroppingView(applicationContext)
        cl_image_cropping_view.addView(imageCroppingView)

        // imageCroppingView 가 맨 뒤로 가도록 조정
        iv_image_cropping_view_frame_back.bringToFront()
        iv_add_icon_default.bringToFront()
        iv_frame_stroke.bringToFront()


        initTopMenuBar()
        setOnClickListeners()
        selectedFrameButton = layout_button_frame0   // 액티비티 실행 시 프레임 스타일 초기값
        selectedColorButton = color_item_pink       // 액티비티 실행 시 컬러 초기값

        selectedColor = resources.getColor(R.color.album_color_pink)
        
        setFrameButtonOnClickListener()
        setColorButtonOnClickListener()

        // add button 눌러서 이미지 받아옴(imagePath)
        // setImageUri or .... 이용해서 소스 이미지 설정해줌
        // viewWidth, viewHeight 설정해줌
        // frameWidth, frameHeight 설정해줌

        viewWidth = LayoutParamsUtils.getScreenWidth(this)
        viewHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, IMAGE_CROPPING_VIEW_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(cl_image_cropping_view, viewWidth, viewHeight)
        LayoutParamsUtils.setItemSize(imageCroppingView, viewWidth, viewHeight)

        frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
        frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        // CroppingView에 프레임 정보 전달
        imageCroppingView.setFrameStyle(BitmapCropUtils.FRAME_TYPE_0, frameWidth, frameHeight)

        drawFrameBack(BitmapCropUtils.FRAME_TYPE_0)





        // add icon size
        val addIconSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ADD_IMAGE_ICON_HEIGHT_PERCENT)
        iv_add_icon_default.updateLayoutParams {
            width = addIconSize
            height = addIconSize
        }

        // 이미지 추가 버튼 누르면 갤러리 실행
        iv_add_icon_default.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // First time
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
                } else {
                    // Not first time
                    Toast.makeText(this, "기기 설정에서 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Permission has already been granted
                openImagePicker()
            }
        }


        // 제목
        val titleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_FONT_SIZE_PERCENT)
        val titleEditTextFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_EDIT_TEXT_FONT_SIZE_PERCENT)
        layout_album_title.tv_album_name_title.setTextSize(Dimension.DP, titleFontSize.toFloat())
        layout_album_title.et_album_name.setTextSize(Dimension.DP, titleEditTextFontSize.toFloat())
        initTitleLayoutMargin()

        // 프레임
        initFrameButtons()
        initFrameLayoutMargin()

        // 컬러
        val colorItemSize = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COLOR_ITEM_SIZE_PERCENT)
        initColorButtons()
        setColorButtonSize(colorItemSize)
        initColorLayoutMargin()


        // 취소 버튼
        layout_top_menu_create_album.iv_icon_left.setOnClickListener {
            finish()
        }

        // 저장 버튼
        layout_top_menu_create_album.iv_icon_right.setOnClickListener {
            val title = layout_album_title.et_album_name.text.toString()

            if(!isImageSelected){
                Toast.makeText(this, R.string.toast_add_image, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(title==""){
                Toast.makeText(this, R.string.toast_enter_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // 내부저장소에 커버이미지 저장
            var fos : FileOutputStream
            var cnt = 1
            var fileName = "album_cover_${title}.png"
            var originFileName = "album_cover_origin_${title}.png"

            var file = File("${applicationContext.filesDir}/$fileName")
            var originFile = File("${applicationContext.filesDir}/$originFileName")

            val name = file.nameWithoutExtension
            val originName = originFile.nameWithoutExtension

            while(file.exists()){
                fileName = "${name}_${cnt}.png"
                file = File("${applicationContext.filesDir}/$fileName")
                cnt++
            }

            if(cnt-1>0)
                originFileName = "${originName}_${cnt-1}.png"

            try{
                // cover
                fos = openFileOutput(fileName, Context.MODE_PRIVATE)
                imageCroppingView.croppedImage
                        .compress(Bitmap.CompressFormat.JPEG, 50, fos)
                fos.write(imageCroppingView.croppedImageBytes)
                fos.close()

                // origin
                fos = openFileOutput(originFileName, Context.MODE_PRIVATE)
                var bitmap = BitmapUtils.uriToBitmap(this, tempUri)

                // 회전값 존재하면 똑바로 보이도록 조정
               val exifDegree = BitmapUtils.getExifDegree(applicationContext, tempUri!!)
                if(exifDegree!=0){
                    bitmap = BitmapUtils.rotate(bitmap, exifDegree.toFloat()) ?: return@setOnClickListener
                }

                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                fos.close()

            } catch (e :Exception){
            }


            // 앨범 객체 저장
            val zoom: Float = imageCroppingView.currentZoom
            val x: Float = imageCroppingView.getMatrixTransX()
            val y: Float = imageCroppingView.getMatrixTransY()

            val albumEntity = AlbumEntity(false, title, selectedFrameType, selectedColor,
                    fileName, originFileName, zoom, x, y)

            val addAlbum = AddAlbum(applicationContext, albumEntity)
            addAlbum.start()
            Thread.sleep(100)

            val setAlbumOrder = SetAlbumOrder(applicationContext, albumId)
            setAlbumOrder.start()
            Thread.sleep(100)



            // 메인 앨범 리스트에 객체 추가
            val album = Albums(albumId, title, selectedColor, fileName, selectedFrameType)

            MainActivity.albumList.add(MainActivity.albumList.size-1, album)

            MainActivity.homeRecyclerAdapter.notifyDataSetChanged()
            MainActivity.addContentsRecyclerAdapter.notifyDataSetChanged()

            MainActivity.homeRecyclerView.smoothScrollToPosition(MainActivity.albumList.size-2)

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


    inner class AddAlbum(val context : Context, private val entity : AlbumEntity) : Thread() {
        override fun run() {
            albumId = AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .insertAlbum(entity)
        }
    }

    inner class SetAlbumOrder(val context: Context, private val order: Long): Thread(){
        override fun run() {
            AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .setOrder(albumId, order)
        }
    }


    override fun onResume() {
        super.onResume()

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQ_STORAGE_PERMISSION->{
                Log.d("GALLERY", "permission result=${grantResults[0]}, granted=${PackageManager.PERMISSION_GRANTED}")

                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d("GALLERY", "permission granted")
                    // 동의했을 경우 갤러리 실행
                    openImagePicker()
                }
                else{
                    // 거부했을 경우
                    // 토스트나 안내 띄워야 함
                }
            }
        }
    }


    private fun openImagePicker(){
        TedImagePicker.with(this)
            .start { uri ->
                val w = LayoutParamsUtils.getScreenWidth(applicationContext)
                val h = LayoutParamsUtils.getItemHeightByPercent(applicationContext, 0.479f)

                imageCroppingView.initView(applicationContext)  // 크롭 이미지뷰 초기화
                imageCroppingView.setImageURI(uri, w, h)

                // 이미지 처음으로 선택되면 이미지 추가 버튼 형태 변경하기
                if(!isImageSelected){
                    iv_add_icon_default.setImageResource(R.drawable.ic_switch_img)
                    val addSwitchIconSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ADD_IMAGE_SWITCH_ICON_HEIGHT_PERCENT)
                    LayoutParamsUtils.setItemSize(iv_add_icon_default, addSwitchIconSize, addSwitchIconSize)
                    isImageSelected = true
                }
                tempUri = uri
            }

    }






    private fun setOnClickListeners() {
        topMenuBarBackButton.setOnClickListener {
            Log.d("TAG", "BackButton Clicked")
        }
        topMenuBarOkButton.setOnClickListener {
            Log.d("TAG", "OkButton Clicked")
//            iv_result.setImageBitmap(BitmapCropUtils.getCroppedBitmap(this, icv_source.croppedImage, BitmapCropUtils.FRAME_TYPE_1))


        }
        
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

    private fun setColorButtonOnClickListener(){
        color_item_pink.setOnClickListener {
            setColorButtonFocused(color_item_pink)
            selectedColor = resources.getColor(R.color.album_color_pink)
        }
        color_item_yellow.setOnClickListener {
            setColorButtonFocused(color_item_yellow)
            selectedColor = resources.getColor(R.color.album_color_yellow)
        }
        color_item_red.setOnClickListener {
            setColorButtonFocused(color_item_red)
            selectedColor = resources.getColor(R.color.album_color_red)
        }
        color_item_brown.setOnClickListener {
            setColorButtonFocused(color_item_brown)
            selectedColor = resources.getColor(R.color.album_color_brown)

        }
        color_item_light_purple.setOnClickListener {
            setColorButtonFocused(color_item_light_purple)
            selectedColor = resources.getColor(R.color.album_color_light_purple)

        }
        color_item_purple.setOnClickListener {
            setColorButtonFocused(color_item_purple)
            selectedColor = resources.getColor(R.color.album_color_purple)

        }
        color_item_green.setOnClickListener {
            setColorButtonFocused(color_item_green)
            selectedColor = resources.getColor(R.color.album_color_green)

        }
        color_item_mint.setOnClickListener {
            setColorButtonFocused(color_item_mint)
            selectedColor = resources.getColor(R.color.album_color_mint)

        }
        color_item_blue.setOnClickListener {
            setColorButtonFocused(color_item_blue)
            selectedColor = resources.getColor(R.color.album_color_blue)

        }
        color_item_navy.setOnClickListener {
            setColorButtonFocused(color_item_navy)
            selectedColor = resources.getColor(R.color.album_color_navy)

        }
        color_item_gray.setOnClickListener {
            setColorButtonFocused(color_item_gray)
            selectedColor = resources.getColor(R.color.album_color_gray)

        }
        color_item_black.setOnClickListener {
            setColorButtonFocused(color_item_black)
            selectedColor = resources.getColor(R.color.album_color_black)

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
        // 프레임 타입에 따라 프레임 사이즈 달리 설정 (프레임4 외에는 사이즈 모두 동일함)
        if(frameType!=BitmapCropUtils.FRAME_TYPE_4){
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME_WIDTH_RATIO)
        }
        else{
            frameHeight = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME4_HEIGHT_PERCENT)
            frameWidth = LayoutParamsUtils.getItemSizeByRatio(frameHeight, FRAME4_WIDTH_RATIO)
        }
        LayoutParamsUtils.setItemSize(iv_frame_stroke, frameWidth, frameHeight)

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
        iv_image_cropping_view_frame_back.setImageBitmap(bitmap)

        // 프레임 테두리 설정
        setFrameStroke(frameType)

        // CroppingView에 프레임 정보 전달
        imageCroppingView.setFrameStyle(frameType, frameWidth, frameHeight)

        if(isImageSelected){
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


    private fun initTopMenuBar(){
        topMenuBarLayout = layout_top_menu_create_album
        topMenuBarTitle = topMenuBarLayout.tv_album_name_title
        topMenuBarBackButton = topMenuBarLayout.iv_icon_left
        topMenuBarOkButton = topMenuBarLayout.iv_icon_right

        topMenuBarTitle.apply{
            text = getString(R.string.create_new_album_top_title)
            setTextColor(getColor(R.color.black))
        }
        topMenuBarLayout.background = getDrawable(R.color.white)
        topMenuBarBackButton.setImageDrawable(getDrawable(R.drawable.ic_back_black))
        topMenuBarOkButton.setImageDrawable(getDrawable(R.drawable.ic_confirm))
    }

    private fun initTitleLayoutMargin(){
        val commonMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COMMON_MARGIN_SIZE_PERCENT)  // 좌,우,하
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_TOP_MARGIN_PERCENT)
        val etTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ET_TOP_MARGIN_PERCENT)

        LayoutParamsUtils.setItemMarginTop(layout_album_title, layoutTopMargin) // 레이아웃 상단 여백
        LayoutParamsUtils.setItemMarginStart(layout_album_title, commonMargin)   // 레이아웃 좌측 공통 여백
        LayoutParamsUtils.setItemMarginEnd(layout_album_title, commonMargin)  // 레이아웃 우측 공통 여백
        LayoutParamsUtils.setItemMarginTop(layout_album_title.et_album_name, etTopMargin)    // EditText 상단 여백
    }

    private fun initFrameLayoutMargin(){
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_TOP_MARGIN_PERCENT)
        val itemTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_ITEM_TOP_MARGIN_PERCENT)
        val subtitleStartMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, FRAME_SUBTITLE_START_MARGIN_PERCENT)

        LayoutParamsUtils.setItemMarginTop(layout_album_frame, layoutTopMargin) // 레이아웃 상단 여백
        LayoutParamsUtils.setItemMarginBottom(layout_album_frame.tv_album_frame_title, itemTopMargin)   // 프레임 아이템 상단 여백
        LayoutParamsUtils.setItemMarginStart(layout_album_frame.tv_album_frame_subtitle, subtitleStartMargin)  // 타이틀 하단 여백
    }


    private fun initColorLayoutMargin(){
        val layoutTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, COLOR_TOP_MARGIN_PERCENT)
        val layoutBottomMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COMMON_MARGIN_SIZE_PERCENT)
        val itemTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, COLOR_ITEM_TOP_MARGIN_PERCENT)
        val colorItemSize = LayoutParamsUtils.getItemWidthByPercent(applicationContext, COLOR_ITEM_SIZE_PERCENT)
        val itemVerticalMargin = LayoutParamsUtils.getItemSizeByRatio(colorItemSize, COLOR_ITEM_BOTTOM_MARGIN_RATIO)

        LayoutParamsUtils.setItemMarginTop(layout_album_color, layoutTopMargin) // 레이아웃 상단 여백
        LayoutParamsUtils.setItemMarginBottom(layout_album_color, layoutBottomMargin)   // 레이아웃 하단 여백
        LayoutParamsUtils.setItemMarginTop(layout_album_color.cl_colors, itemTopMargin)   // 아이템 상단 여백
        LayoutParamsUtils.setItemMarginTop(color_item_green, itemVerticalMargin)    // 아이템간 높이 간격
    }

    private fun initFrameButtons(){
        val titleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, TITLE_FONT_SIZE_PERCENT)
        val frameSubtitleFontSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_SUBTITLE_FONT_SIZE_PERCENT)
        val frameTitleBottomMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, FRAME_ITEM_TOP_MARGIN_PERCENT)
        tv_album_frame_title.setTextSize(Dimension.DP, titleFontSize.toFloat())
        tv_album_frame_subtitle.setTextSize(Dimension.DP, frameSubtitleFontSize.toFloat())
        LayoutParamsUtils.setItemMarginTop(layout_album_frame.cl_frame_icons, frameTitleBottomMargin)


        tv_album_color_title.setTextSize(Dimension.DP, titleFontSize.toFloat())

        layout_button_frame0.iv_frame_icon_back.setImageDrawable(getDrawable(R.drawable.ic_button_frame_back))

        layout_button_frame1.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame1_button))
        layout_button_frame2.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame2_button))
        layout_button_frame3.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame3_button))
        layout_button_frame4.iv_frame_icon.setImageDrawable(getDrawable(R.drawable.ic_frame4_button))
    }

    private fun initColorButtons(){
        // 첫번째 줄
        color_item_pink.setColor(resources.getColor(R.color.album_color_pink))
        color_item_pink.setFocused()    // 초기 선택 값

        color_item_yellow.setColor(resources.getColor(R.color.album_color_yellow))
        color_item_red.setColor(resources.getColor(R.color.album_color_red))
        color_item_brown.setColor(resources.getColor(R.color.album_color_brown))
        color_item_light_purple.setColor(resources.getColor(R.color.album_color_light_purple))
        color_item_purple.setColor(resources.getColor(R.color.album_color_purple))

        // 두번째 줄
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
        LayoutParamsUtils.setItemSize(color_item_red, size, size)
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
