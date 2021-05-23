package com.hschoi.collect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import kotlinx.android.synthetic.main.activity_add_contents.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddContentsActivity : AppCompatActivity() {

    companion object {
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

        const val REQ_STORAGE_PERMISSION = 100
        const val REQ_GALLERY = 101

        var addContentsActivity: Activity? = null

        lateinit var imageList : ArrayList<String>
        var isModify = false
        var isSaved = false

        lateinit var defaultAddView : ConstraintLayout
    }

    private lateinit var mAlbumItemEntity : AlbumItemEntity
    private lateinit var mAlbumEntity : AlbumEntity

    private var albumId: Long = -1
    private var contentsId: Long = -1

    private lateinit var imageRecyclerAdapter : ContentsImageRecyclerAdapter

    private var isImageSelected = false

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

        addContentsActivity = this@AddContentsActivity
        imageList = ArrayList()
        defaultAddView = cl_contents_add_image_back


        // 이미지 아이템 리사이클러뷰 초기화
        imageRecyclerAdapter = ContentsImageRecyclerAdapter(imageList)
        rv_image_list.adapter = imageRecyclerAdapter
        rv_image_list.layoutManager = GridLayoutManager(applicationContext, 1, RecyclerView.HORIZONTAL, false)

        val decoration = ContentsImageRecyclerDecoration(applicationContext)
        rv_image_list.addItemDecoration(decoration)

        // 데이터 불러옴
        loadData()

        if(imageList.size==0){
            defaultAddView.bringToFront()
        }
        else{
            rv_image_list.smoothScrollToPosition(imageList.size-1)
            defaultAddView.visibility = View.INVISIBLE
        }


        // 레이아웃 초기화
        initLayoutStyle()

        // 버튼 클릭 리스너 등록
        setButtonClickListeners()

        
    }

    override fun onDestroy() {
        if(!isSaved && !isModify){   //  저장되지 않는 경우에 이미지 파일 모두 삭제
            for(fileName in imageList){
                deleteFile(fileName)
            }
        }
        else{
            isSaved = false
        }
        super.onDestroy()
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
                imageList.add(name)
            }
            imageList.add("")   // add dummy (for add button)

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
            if(!isImageSelected){
                // show toast
                return@setOnClickListener
            }
            // start activity (add contents cover activity)
            val intent = Intent(this, AddContentsCoverActivity::class.java)
            if(isModify){
                intent.putExtra("contentsId", contentsId)
                intent.putExtra("contentsImage", mAlbumItemEntity.contentsImageName)
                intent.putExtra("contentsCoverImage", mAlbumItemEntity.coverImageName)
            }
            else{
                intent.putExtra("contentImageUri", contentImageUri)
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
            selectGallery()
        }

        // 날짜 선택 버튼
        cl_contents_date.setOnClickListener {
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQ_GALLERY ->{
                // 갤러리로부터 사진 가져옴
                if(data!=null && resultCode==RESULT_OK){
                    val contentImageUri = data.data


                    val viewWidth = LayoutParamsUtils.getScreenWidth(applicationContext)
                    val viewHeight = LayoutParamsUtils
                            .getItemHeightByPercent(applicationContext, 0.479f)

                    var fileName = "contents_image_${mAlbumEntity.id}.png"
                    var file = File("${applicationContext.filesDir}/$fileName")
                    val name = file.nameWithoutExtension
                    var cnt = 1

                    while(file.exists()){
                        fileName = "${name}_${cnt}.png"
                        Log.d("file", "new=$fileName")
                        file = File("${applicationContext.filesDir}/$fileName")
                        cnt++
                    }

                    if(cnt-1>0)
                        fileName = "${name}_${cnt-1}.png"


                    val fos = openFileOutput(fileName, Context.MODE_PRIVATE)
                    var bitmap: Bitmap = BitmapUtils
                            .getResizedBitmap(applicationContext, contentImageUri,
                                    viewWidth, viewHeight)?:return

                    // 회전값 존재하면 똑바로 보이도록 조정
                    val exifDegree = BitmapUtils.getExifDegree(applicationContext, contentImageUri!!)
                    if(exifDegree!=0){
                        bitmap = BitmapUtils.rotate(bitmap, exifDegree.toFloat()) ?: return
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                    fos.close()

                    defaultAddView.visibility = View.INVISIBLE
                    isImageSelected = true

                    if(imageList.size==0){
                        imageList.add(fileName)
                        imageList.add("")
                    }
                    else{
                        imageList.add(imageList.size-1,fileName)
                    }

                    rv_image_list.smoothScrollToPosition(imageList.size-1)
                    imageRecyclerAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQ_STORAGE_PERMISSION->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d("GALLERY", "permission granted")
                    // 동의했을 경우 갤러리 실행
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    intent.type = "image/*"
                    startActivityForResult(intent, REQ_GALLERY)
                }
                else{
                    // 거부했을 경우
                    // 토스트나 안내 띄워야 함
                }
            }
        }
    }



    private fun selectGallery() {
//        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
            Log.d("GALLERY", "권한요청 $readPermission, denied=${PackageManager.PERMISSION_DENIED}")

        } else { // 권한 있음
            Log.d("GALLERY", "권한있음 갤러리 실행되야함")

            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            startActivityForResult(intent, REQ_GALLERY)
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