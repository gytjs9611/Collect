package com.hschoi.collect

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hschoi.collect.adapter.PhotoViewPagerAdapter
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumItemEntity
import com.hschoi.collect.util.ColorUtils
import com.hschoi.collect.util.DateUtils.Companion.getDayOfWeekFromDate
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.activity_album_feed_contents.*

class AlbumFeedContentsActivity : AppCompatActivity(){

    companion object {
        lateinit var activity : AlbumFeedContentsActivity

        private const val DOT_SIZE = 4f/716f
        private const val DOT_TOP_MARGIN = 8f/716f
    }

    private var contentsId : Long = -1
    private lateinit var mImageList : ArrayList<String>

    private lateinit var mAlbumItemEntity: AlbumItemEntity

    private var albumColor = -1

    private lateinit var viewPagerAdapter : PhotoViewPagerAdapter


    inner class GetAlbumItemEntity(private val context: Context, private val contentsId: Long):Thread(){
        override fun run() {
            mAlbumItemEntity = AlbumDatabase.getInstance(context)!!
                    .albumItemDao()
                    .getAlbumItemEntity(contentsId)
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
        cl_icon_left.setOnClickListener {
            finish()
        }

        // 케밥 메뉴 버튼
        cl_icon_right.setOnClickListener {
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
        val statusBarColor = ColorUtils.getStatusBarColor(this, albumColor)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        // UI 출력
        cl_feed_contents_back.setBackgroundColor(ColorUtils.getAlbumColor(this,albumColor))
        tv_album_feed_contents_title.text = mAlbumItemEntity.contentsTitle

        var dateString = mAlbumItemEntity.contentsDate
        dateString = dateString.substringBefore('/')
        dateString += "."+getDayOfWeekFromDate(this, dateString)
        tv_album_feed_contents_date.text = dateString

        tv_album_feed_contents_string.text = mAlbumItemEntity.contentsSentence
        tv_album_feed_contents_string.movementMethod = ScrollingMovementMethod()    // 스크롤 가능

        // 이미지
        mImageList = ArrayList()
        val tempList = mAlbumItemEntity.contentsImageName.split("|")
        for(item in tempList){
            mImageList.add(item)
            Log.d("test","item=$item")
        }
        // view pager
        viewPagerAdapter = PhotoViewPagerAdapter(mImageList)
        vp_images.adapter = viewPagerAdapter

        // page indicator
        setPageIndicator()
    }


    private fun setPageIndicator(){
//        val dotSize = LayoutParamsUtils.getItemHeightByPercent(this, DOT_SIZE)
        val dotTopMargin = LayoutParamsUtils.getItemHeightByPercent(this, DOT_TOP_MARGIN)

        LayoutParamsUtils.setItemMarginTop(indicator_feed_contents, dotTopMargin)


        indicator_feed_contents.dotColor = getColor(R.color.page_indicator_dot_white)
        indicator_feed_contents.selectedDotColor = getColor(R.color.page_indicator_dot_white_selected)
        indicator_feed_contents.attachToPager(vp_images)
    }


}
