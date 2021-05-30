package com.hschoi.collect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.adapter.ContentsImageRecyclerAdapter
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.database.entity.AlbumItemEntity
import com.hschoi.collect.util.BitmapUtils
import com.hschoi.collect.util.DateUtils.Companion.getDayOfWeekString
import com.hschoi.collect.util.LayoutParamsUtils
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_add_contents.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


class AddContentsActivity : AppCompatActivity() {

    companion object {
        private lateinit var mContext: Context
        private var MAX_IMAGE_CNT = 10

        private const val ADD_IMAGE_BACK_WIDTH = 340f/360f
        const val ADD_IMAGE_BACK_HEIGHT_RATIO = 262f/340f
        private const val ADD_IMAGE_BACK_MARGIN = 10f/360f

        private const val ADD_ICON_SIZE = 52f/716f

        const val IMAGE_WIDTH = 328f/360f
        const val IMAGE_HEIGHT = 262f/716f
        const val IMAGE_TOP_MARGIN_RATIO = 14f/16f
        const val IMAGE_SIDE_MARGIN = 16f/360f
        const val IMAGE_ADD_BUTTON_SIDE_MARGIN = 28f/360f

        const val ADD_BUTTON_WIDTH_RATIO = 56f/262f

        const val DATE_TOP_MARGIN = 22f/716f

        private const val DOT_TOP_MARGIN = 8f/716f


        const val REQ_STORAGE_PERMISSION = 100

        var addContentsActivity: Activity? = null

        lateinit var originImageList : ArrayList<String>
        lateinit var imageList : ArrayList<String>
        lateinit var originCoverImage: String

        private lateinit var imageRecyclerView: RecyclerView
        private lateinit var imageRecyclerAdapter : ContentsImageRecyclerAdapter


        private lateinit var mAlbumItemEntity : AlbumItemEntity
        private lateinit var mAlbumEntity : AlbumEntity

        var isModify = false
        var isSaved = false

        lateinit var defaultAddView : ConstraintLayout

        var addContentsAttacher = AddImageAttacher()

        fun hideKeyboard(){
            val activity = mContext as Activity
            val focusView = activity.currentFocus
            if(focusView!=null){
                val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }

        fun openImagePicker(){
            val itemCnt = if(imageList.size==0) 0 else imageList.size-1
            val remainCnt = MAX_IMAGE_CNT - itemCnt

            val maxInfo = String.format(mContext.getString(R.string.max_image_cnt), MAX_IMAGE_CNT)
            TedImagePicker.with(mContext)
                .max(remainCnt, maxInfo)
                .startMultiImage { uriList ->

                    val viewWidth = LayoutParamsUtils.getScreenWidth(mContext)
                    val viewHeight = LayoutParamsUtils
                        .getItemHeightByPercent(mContext, 0.479f)

                    for(item in uriList){
                        var fileName = "contents_image_${mAlbumEntity.id}_.png"
                        var file = File("${mContext.filesDir}/$fileName")
                        var fileTemp = File("${mContext.filesDir}/temp_$fileName")
                        val name = file.nameWithoutExtension
                        var cnt = 1

                        while(file.exists() || fileTemp.exists()){
                            fileName = "${name}${cnt}.png"
                            file = File("${mContext.filesDir}/$fileName")
                            fileTemp = File("${mContext.filesDir}/temp_$fileName")
                            cnt++
                        }

                        if(cnt-1>0)
                            fileName = "${name}${cnt-1}.png"

                        fileName = "temp_${fileName}"

                        val fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE)
                        var bitmap: Bitmap = BitmapUtils
                            .getResizedBitmap(mContext, item,
                                viewWidth, viewHeight)?:return@startMultiImage

                        // 회전값 존재하면 똑바로 보이도록 조정
                        val exifDegree = BitmapUtils.getExifDegree(mContext, item!!)
                        if(exifDegree!=0){
                            bitmap = BitmapUtils.rotate(bitmap, exifDegree.toFloat()) ?: return@startMultiImage
                        }
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                        fos.close()

                        defaultAddView.visibility = View.INVISIBLE

                        if(imageList.size==0){
                            imageList.add(fileName)
                            imageList.add("")
                        }
                        else{
                            imageList.add(imageList.size-1,fileName)
                        }

                        imageRecyclerView.smoothScrollToPosition(imageList.size-1)
                        imageRecyclerAdapter.notifyDataSetChanged()
                    }
                }

        }
    }



    private var albumId: Long = -1
    private var contentsId: Long = -1


//    private var isImageSelected = false

    private var contentImageUri : Uri? = null

    private lateinit var mCalendar : Calendar
    private var mYear = 0
    private var mMonth = 0
    private var mDayOfMonth = 0
    private var mDayOfWeek = 0


    inner class GetAlbumItemEntity(private val context: Context, private val contentsId: Long):Thread(){
        override fun run() {
            mAlbumItemEntity = AlbumDatabase.getInstance(context)!!
                    .albumItemDao()
                    .getAlbumItemEntity(contentsId)
        }
    }

    inner class GetAlbumEntity(val context: Context, private val albumId: Long): Thread(){
        override fun run(){
            mAlbumEntity = AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .getAlbumEntity(albumId)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contents)


        mContext = this
        MAX_IMAGE_CNT = 10  // free, premium 차이두기

        addContentsActivity = this@AddContentsActivity
        originImageList = ArrayList()
        imageList = ArrayList()
        defaultAddView = cl_contents_add_image_back

        // 이미지 아이템 리사이클러뷰 초기화
        imageRecyclerView = rv_image_list
        imageRecyclerAdapter = ContentsImageRecyclerAdapter(imageList)
        imageRecyclerView.adapter = imageRecyclerAdapter
        imageRecyclerView.layoutManager = GridLayoutManager(applicationContext, 1, RecyclerView.HORIZONTAL, false)

        val decoration = ContentsImageRecyclerDecoration(applicationContext)
        imageRecyclerView.addItemDecoration(decoration)

        // 데이터 불러옴
        loadData()

        if(imageList.size==0){
            defaultAddView.bringToFront()
        }
        else{
            imageRecyclerView.smoothScrollToPosition(imageList.size-1)
            defaultAddView.visibility = View.INVISIBLE
        }


        // 레이아웃 초기화
        initLayoutStyle()

        // 버튼 클릭 리스너 등록
        setButtonClickListeners()

        // page indicator
        setPageIndicator()


    }



    override fun onDestroy() {
        if(!isSaved){   //  저장되지 않는 경우에 이미지 파일 모두 삭제
            for(fileName in imageList){
                deleteFile(fileName)
            }
        }
        else{
            // 원래 저장되어있던 이미지 모두 삭제
            for(fileName in originImageList){
                deleteFile(fileName)
            }

            // 새로 업데이트된 파일(temp_어쩌고.png)이름 앞 temp_ 제거
            for(fileName in imageList){
                val prevFile = File("${applicationContext.filesDir}/$fileName")
                val newFile = File("${applicationContext.filesDir}/${fileName.substringAfter("temp_")}")
                prevFile.renameTo(newFile)
            }

            isSaved = false
        }
        super.onDestroy()
    }

    private fun setPageIndicator(){
        val dotTopMargin = LayoutParamsUtils.getItemHeightByPercent(this, DOT_TOP_MARGIN)

        LayoutParamsUtils.setItemMarginTop(indicator_add_contents, dotTopMargin)

        indicator_add_contents.dotColor = getColor(R.color.page_indicator_dot_black)
        indicator_add_contents.selectedDotColor = getColor(R.color.page_indicator_dot_black_selected)

        indicator_add_contents.attachToPager(rv_image_list, addContentsAttacher)
//        indicator_add_contents.attachToRecyclerView(rv_image_list)

    }



    private fun loadData(){
        albumId = intent.getLongExtra("albumId", -1)

        GetAlbumEntity(this, albumId).start()
        Thread.sleep(100)


        // 앨범 수정일 경우 데이터 로드
        contentsId = intent.getLongExtra("contentsId", -1)
        isModify = contentsId>=0
        if(isModify){  // 컨텐츠 수정일 경우
            val getAlbumItemEntity = GetAlbumItemEntity(this, contentsId)
            getAlbumItemEntity.start()
            Thread.sleep(100)

            et_contents_title.setText(mAlbumItemEntity.contentsTitle)

            // 날짜 설정
            val dateString = mAlbumItemEntity.contentsDate
            val dateFormat = SimpleDateFormat("yyyy.MM.dd")
            val date = dateFormat.parse(dateString)

            mCalendar = Calendar.getInstance()
            mCalendar.time = date


            // 컨텐츠 내용
            et_contents_sentences.setText(mAlbumItemEntity.contentsSentence)


            // 이미지
            val savedList = mAlbumItemEntity.contentsImageName.split("|")
            for(name in savedList){
                originImageList.add(name)
                imageList.add("temp_${name}")
            }
            originImageList.add("")
            imageList.add("")   // add dummy (for add button)
            imageRecyclerAdapter.notifyDataSetChanged()

            originCoverImage = originImageList[mAlbumItemEntity.coverImageIndex]

            // imageList 파일 생성
            for(i in 0 until imageList.size){
                val originFile = File("${applicationContext.filesDir}/${originImageList[i]}")
                val destFile = File("${applicationContext.filesDir}/${imageList[i]}")
                Files.copy(originFile.toPath(), destFile.toPath())
            }
        }
        else{   // 컨텐츠 생성일 경우
            // 날짜 오늘 날짜로 초기화
            mCalendar = Calendar.getInstance()
        }

        mYear = mCalendar[Calendar.YEAR]
        mMonth = mCalendar[Calendar.MONTH]
        mDayOfMonth = mCalendar[Calendar.DAY_OF_MONTH]
        mDayOfWeek = mCalendar[Calendar.DAY_OF_WEEK]

        tv_contents_date.text =
                "$mYear.${mMonth+1}.$mDayOfMonth.${getDayOfWeekString(applicationContext, mDayOfWeek)}"

    }


    private fun initLayoutStyle(){
        // 상단바 아이콘
        layout_top_menu_add_contents.iv_icon_left.setImageDrawable(getDrawable(R.drawable.ic_close))
        layout_top_menu_add_contents.iv_icon_right.setImageDrawable(getDrawable(R.drawable.ic_next))

        // 상단바 타이틀, 배경색
        layout_top_menu_add_contents.tv_album_name_title.text = mAlbumEntity.albumTitle
        layout_top_menu_add_contents.setBackgroundColor(mAlbumEntity.albumColor)

        // 상태바 색상 배경색에 따라 설정
        val statusBarColor = getStatusBarColor(mAlbumEntity.albumColor)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        // 이미지 추가 버튼 영역 너비, 높이
        val addImageBackWidth = LayoutParamsUtils.getItemWidthByPercent(applicationContext, ADD_IMAGE_BACK_WIDTH)
        val addImageBackHeight = LayoutParamsUtils.getItemSizeByRatio(addImageBackWidth, ADD_IMAGE_BACK_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(cl_contents_add_image_back, addImageBackWidth, addImageBackHeight)

        // 상단 여백
        val addImageBackMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, ADD_IMAGE_BACK_MARGIN)
        LayoutParamsUtils.setItemMarginTop(cl_contents_add_image_back, addImageBackMargin)

        // 가운데 ADD 버튼 크기 설정
        val addImageSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ADD_ICON_SIZE)
        LayoutParamsUtils.setItemSize(iv_add_contents_image, addImageSize, addImageSize)

        val dateTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, DATE_TOP_MARGIN)
        LayoutParamsUtils.setItemMarginTop(cl_contents_date, dateTopMargin)

        // 이미지 아이템 여백
        val imageLeftMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, IMAGE_SIDE_MARGIN)
        val imageTopMargin = LayoutParamsUtils.getItemSizeByRatio(imageLeftMargin, IMAGE_TOP_MARGIN_RATIO)
        LayoutParamsUtils.setItemMarginTop(rv_image_list, imageTopMargin)

    }


    private fun setButtonClickListeners(){
        // CLOSE 버튼
        layout_top_menu_add_contents.iv_icon_left.setOnClickListener {
            finish()
        }

        // NEXT 버튼
        layout_top_menu_add_contents.iv_icon_right.setOnClickListener {
            if(imageList.isEmpty()){
                // show toast
                Toast.makeText(this, "이미지를 추가해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // start activity (add contents cover activity)
            val intent = Intent(this, AddContentsCoverActivity::class.java)
            if(isModify) {
                intent.putExtra("contentsId", contentsId)
                intent.putExtra("contentsCoverImage", mAlbumItemEntity.coverImageName)
            }
            val contentsDate = tv_contents_date.text.toString().substringBeforeLast(".")
            intent.putExtra("contentsDate", contentsDate)
            intent.putExtra("contentsTitle", et_contents_title.text.toString())
            intent.putExtra("contentsSentence", et_contents_sentences.text.toString())
            intent.putExtra("albumId", albumId)
            startActivity(intent)
        }
        
        // 이미지 추가 버튼
        iv_add_contents_image.setOnClickListener {
            // 이미지 갤러리로부터 받아오기
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // First time
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION
                    )
                } else {
                    // Not first time
                    Toast.makeText(this, "기기 설정에서 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Permission has already been granted
                openImagePicker()
            }
        }

        // 날짜 선택 버튼
        cl_contents_date.setOnClickListener {
            hideKeyboard()

            // 달력 호출해서 날짜 yyyy.mm.dd.요일 형식으로 받아오기
            val datePickerFragment = DatePickerFragment(mYear, mMonth, mDayOfMonth)
            datePickerFragment.show(supportFragmentManager, "datePicker")
        }

    }



    fun processDatePickerResult(year: Int, month: Int, dayOfMonth: Int, dayOfWeek: Int){
        mYear = year
        mMonth = month
        mDayOfMonth = dayOfMonth

        tv_contents_date.text =
                "$year.${month+1}.$dayOfMonth.${getDayOfWeekString(applicationContext, dayOfWeek)}"
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQ_STORAGE_PERMISSION->{
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


    override fun onResume() {
        super.onResume()
    }



    private fun getStatusBarColor(albumColor : Int) : Int{
        val statusBarColor = when(albumColor){
            getColor(R.color.album_color_pink)->R.color.album_feed_status_bar_pink
            getColor(R.color.album_color_yellow)->R.color.album_feed_status_bar_yellow
            getColor(R.color.album_color_red)->R.color.album_feed_status_bar_red
            getColor(R.color.album_color_brown)->R.color.album_feed_status_bar_brown
            getColor(R.color.album_color_light_purple)->R.color.album_feed_status_bar_light_purple
            getColor(R.color.album_color_purple)->R.color.album_feed_status_bar_purple
            getColor(R.color.album_color_green)->R.color.album_feed_status_bar_green
            getColor(R.color.album_color_mint)->R.color.album_feed_status_bar_mint
            getColor(R.color.album_color_blue)->R.color.album_feed_status_bar_blue
            getColor(R.color.album_color_navy)->R.color.album_feed_status_bar_navy
            getColor(R.color.album_color_gray)->R.color.album_feed_status_bar_gray
            getColor(R.color.album_color_black)->R.color.album_feed_status_bar_black
            else->R.color.album_feed_status_bar_pink
        }
        return getColor(statusBarColor)
    }

}