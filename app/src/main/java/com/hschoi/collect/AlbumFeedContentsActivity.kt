package com.hschoi.collect

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.activity_album_feed_contents.*

class AlbumFeedContentsActivity : AppCompatActivity(){

    private lateinit var albumTitle : String
    private var contentsId : Long = -1
    private lateinit var contentsImage : String
    private lateinit var contentsCoverImage : String
    private lateinit var contentsTitle : String
    private lateinit var contentsDate : String
    private lateinit var contentsSentence : String

    private var color = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_feed_contents)


        // 앨범명, 컨텐츠 제목, 날짜, 컨텐츠 내용
        albumTitle = intent.getStringExtra("albumTitle")
        contentsId = intent.getLongExtra("contentsId", -1)
        contentsTitle = intent.getStringExtra("contentsTitle")
        color = intent.getIntExtra("color", getColor(R.color.album_color_pink))
        contentsDate = intent.getStringExtra("contentsDate")
        contentsSentence = intent.getStringExtra("contentsSentence")
        contentsImage = intent.getStringExtra("contentsImage")
        contentsCoverImage = intent.getStringExtra("contentsCoverImage")




        // 상태바 색상 배경색에 따라 설정
        val statusBarColor = getStatusBarColor(color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        // UI 출력
        cl_feed_contents_back.setBackgroundColor(color)
        tv_album_feed_contents_title.text = contentsTitle
        tv_album_feed_contents_date.text = contentsDate
        tv_album_feed_contents_string.text = contentsSentence
        tv_album_feed_contents_string.movementMethod = ScrollingMovementMethod()    // 스크롤 가능

        // 이미지
        val fis = openFileInput(contentsImage)
        val bitmap = BitmapFactory.decodeStream(fis)
        Glide.with(this).load(bitmap).into(iv_feed_contents_image)

        // 버튼
        iv_album_feed_contents_back_button.setOnClickListener {
            finish()
        }

        // 케밥 메뉴 버튼
        iv_album_feed_contents_more_button.setOnClickListener {
            val bottomMenuSheet = ContentsBottomMenuSheet(applicationContext, contentsId,
                                            contentsCoverImage, contentsImage)
            bottomMenuSheet.show(supportFragmentManager, bottomMenuSheet.tag)
        }

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
