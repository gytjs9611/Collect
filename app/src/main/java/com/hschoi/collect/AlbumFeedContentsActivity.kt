package com.hschoi.collect

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumItemEntity
import com.hschoi.collect.util.DateUtils.Companion.getDayOfWeekFromDate
import kotlinx.android.synthetic.main.activity_album_feed_contents.*
import java.text.SimpleDateFormat
import java.util.*

class AlbumFeedContentsActivity : AppCompatActivity(){

    companion object {
        lateinit var activity : AlbumFeedContentsActivity
    }

    private var contentsId : Long = -1
    /*private var albumId : Long = -1
    private lateinit var albumTitle : String
    private lateinit var contentsImage : String
    private lateinit var contentsCoverImage : String
    private lateinit var contentsTitle : String
    private lateinit var contentsDate : String
    private lateinit var contentsSentence : String
    private var frameType = 0*/

    private lateinit var mAlbumItemEntity: AlbumItemEntity

    private var albumColor = -1


    inner class GetAlbumItemEntity(private val context: Context, private val contentsId: Long):Thread(){
        override fun run() {
            mAlbumItemEntity = AlbumDatabase.getInstance(context)!!
                    .albumItemDao()
                    .getAlbumEntity(contentsId)
        }
    }

    inner class GetAlbumColor(private val context: Context, private val albumId: Long):Thread(){
        override fun run() {
            albumColor = AlbumDatabase.getInstance(context)!!
                    .albumDao()
                    .getAlbumColor(albumId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_feed_contents)

        activity = this

        // 앨범명, 컨텐츠 제목, 날짜, 컨텐츠 내용
        contentsId = intent.getLongExtra("contentsId", -1)


        // 버튼
        iv_album_feed_contents_back_button.setOnClickListener {
            finish()
        }

        // 케밥 메뉴 버튼
        iv_album_feed_contents_more_button.setOnClickListener {
            val bottomMenuSheet = ContentsBottomMenuSheet(applicationContext, contentsId)
            bottomMenuSheet.show(supportFragmentManager, bottomMenuSheet.tag)

        }

    }

    override fun onResume() {
        super.onResume()


        GetAlbumItemEntity(this, contentsId).start()
        Thread.sleep(100)

        GetAlbumColor(this, mAlbumItemEntity.albumId).start()
        Thread.sleep(100)



        // 상태바 색상 배경색에 따라 설정
        val statusBarColor = getStatusBarColor(albumColor)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        // UI 출력
        cl_feed_contents_back.setBackgroundColor(albumColor)
        tv_album_feed_contents_title.text = mAlbumItemEntity.contentsTitle

        var dateString = mAlbumItemEntity.contentsDate
        dateString = dateString.substringBeforeLast('/')
        dateString += "."+getDayOfWeekFromDate(this, dateString)
        tv_album_feed_contents_date.text = dateString

        tv_album_feed_contents_string.text = mAlbumItemEntity.contentsSentence
        tv_album_feed_contents_string.movementMethod = ScrollingMovementMethod()    // 스크롤 가능

        // 이미지
        val fis = openFileInput(mAlbumItemEntity.contentsImageName)
        val bitmap = BitmapFactory.decodeStream(fis)
        Glide.with(this).load(bitmap).into(iv_feed_contents_image)
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
