package com.hschoi.collect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.DateUtils.Companion.getDayOfWeekString
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.activity_add_contents_free.*
import kotlinx.android.synthetic.main.layout_add_contents_image.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import java.util.*


class AddContentsActivity : AppCompatActivity() {

    companion object {
        private const val ADD_IMAGE_BACK_WIDTH = 340f/360f
        private const val ADD_IMAGE_BACK_HEIGHT_RATIO = 262f/340f
        private const val ADD_IMAGE_BACK_MARGIN = 10f/360f

        private const val ADD_ICON_SIZE = 52f/716f

        private const val IMAGE_WIDTH = 328f/360f
        private const val IMAGE_HEIGHT_RATIO = 262f/328f
        private const val IMAGE_TOP_MARGIN_RATIO = 14f/16f
        private const val IMAGE_SIDE_MARGIN = 16f/360f

        private const val REQ_STORAGE_PERMISSION = 100
        private const val REQ_GALLERY = 101

//        var contentImageList = ArrayList<String>()

        var addContentsActivity: Activity? = null
    }

    private var albumId: Long = -1
    private var albumTitle = ""
    private var frameType = -1

    private var albumColor = -1

    private var isImageSelected = false

    private var contentImageUri : Uri? = null

    private var mYear = 0
    private var mMonth = 0
    private var mDayOfMonth = 0
    private var mDayOfWeek = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contents_free)

        addContentsActivity = this@AddContentsActivity


        // 레이아웃 초기화
        initLayoutStyle()



        // 버튼 클릭 리스너 등록
        setButtonClickListeners()

        
    }



    private fun initLayoutStyle(){
        frameType = intent.getIntExtra("frameType", BitmapCropUtils.FRAME_TYPE_0)

        // 상단바 아이콘
        layout_top_menu_add_contents.iv_icon_left.setImageDrawable(getDrawable(R.drawable.ic_close))
        layout_top_menu_add_contents.iv_icon_right.setImageDrawable(getDrawable(R.drawable.ic_next))

        // 상단바 타이틀, 배경색
        albumId = intent.getLongExtra("albumId", -1)
        albumTitle = intent.getStringExtra("albumTitle")
        layout_top_menu_add_contents.tv_album_name_title.text = albumTitle
        albumColor = intent.getIntExtra("color", getColor(R.color.album_color_pink))
        layout_top_menu_add_contents.setBackgroundColor(albumColor)

        // 상태바 색상 배경색에 따라 설정
        val statusBarColor = getStatusBarColor(albumColor)
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


        // 날짜 오늘 날짜로 초기화
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDayOfMonth = c[Calendar.DAY_OF_MONTH]
        mDayOfWeek = c[Calendar.DAY_OF_WEEK]

        tv_contents_date.text =
                "$mYear.${mMonth+1}.$mDayOfMonth.${getDayOfWeekString(applicationContext, mDayOfWeek)}"
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
            intent.putExtra("albumId", albumId)
            intent.putExtra("albumTitle", albumTitle)
            intent.putExtra("frameType", frameType)
            intent.putExtra("color", albumColor)
            intent.putExtra("contentImageUri", contentImageUri)
            val contentsDate = tv_contents_date.text.toString().substringBeforeLast(".")
            intent.putExtra("contentsDate", contentsDate)
            intent.putExtra("contentsTitle", et_contents_title.text.toString())
            intent.putExtra("contentsSentence", et_contents_sentences.text.toString())
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

    // 카드뷰에 선택한 이미지 삽입
    private fun addImageCardView(uri : Uri){
        cl_contents_add_image_back.visibility = View.GONE

        // 이미지 카드뷰 객체 생성 및 초기화
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val imageCardView
                = inflater.inflate(
                R.layout.layout_add_contents_image, cl_cover_image, false
        ) as CardView

        cl_cover_image.addView(imageCardView)
        val imageWidth = LayoutParamsUtils.getItemWidthByPercent(applicationContext, IMAGE_WIDTH)
        val imageHeight = LayoutParamsUtils.getItemSizeByRatio(imageWidth, IMAGE_HEIGHT_RATIO)
        LayoutParamsUtils.setItemSize(imageCardView, imageWidth, imageHeight)

        imageCardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToTop = cl_cover_image.id
            startToStart = cl_cover_image.id
            endToEnd = cl_cover_image.id
        }

        // 이미지 카드뷰 상단 여백 설정
        val imageSideMargin = LayoutParamsUtils.getItemWidthByPercent(applicationContext, IMAGE_SIDE_MARGIN)
        val imageTopMargin = LayoutParamsUtils.getItemSizeByRatio(imageSideMargin, IMAGE_TOP_MARGIN_RATIO)
        LayoutParamsUtils.setItemMarginTop(imageCardView, imageTopMargin)

        // 이미지 설정
//        imageCardView.iv_content_image.setImageURI(uri)
        Glide.with(this).load(uri).into(imageCardView.iv_content_image)

        // 삭제 버튼
        imageCardView.iv_delete_button.setOnClickListener {
            cl_contents_add_image_back.visibility = View.VISIBLE
            cl_cover_image.removeView(imageCardView)
            contentImageUri = null
            isImageSelected = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQ_GALLERY ->{
                // 갤러리로부터 사진 가져옴
                if(data!=null && resultCode==RESULT_OK){
                    contentImageUri = data.data
//                    val bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri)
                    addImageCardView(contentImageUri!!)
                    cl_contents_add_image_back.visibility = View.GONE
                    isImageSelected = true


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