package com.hschoi.collect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hschoi.collect.customview.AlbumFeedItem
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.util.BitmapCropUtils
import com.hschoi.collect.util.DateUtils.Companion.getDayOfWeekFromDate
import com.hschoi.collect.util.LayoutParamsUtils
import kotlinx.android.synthetic.main.activity_album_feed.*
import kotlinx.android.synthetic.main.item_album_feed.view.*
import kotlinx.android.synthetic.main.layout_bottom_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.tv_album_name_title
import kotlin.math.roundToInt

class AlbumFeedActivity : AppCompatActivity() {
    companion object{
        private const val ITEM_HEIGHT_PERCENT = 246f/716f         // 246/716
        private const val ITEM_WIDTH_PERCENT = 147f/360f            // 147/360
        private const val ITEM_HEIGHT_DIMENSION_RATIO = 147f/246f   // 147/246
        private const val ITEM_WIDTH_DIMENSION_RATIO = 246f/147f    // 246/147

        private const val ITEM_BOTTOM_MARGIN_PERCENT = 34f/716f        // 34/716
        private const val ITEM_LEFT_TOP_MARGIN_PERCENT = 18f/716f
        private const val ITEM_RIGHT_TOP_MARGIN_PERCENT = 141f/716f

        private const val TITLE_TEXT_SIZE_RATIO = 14f/126f
        private const val TITLE_TEXT_VIEW_SIZE_RATIO = 21f/126f

        private const val INIT_TEXT_SIZE_PERCENT = 13f/716f

        var isDataChanged = false
    }

    private var albumItemList = ArrayList<FeedItemInfo>()
    private var albumItemCnt = 0

    private var albumId :Long = -1
    private lateinit var albumTitle : String
    private var frameType = BitmapCropUtils.FRAME_TYPE_0
    private var color = -1

    private var itemWidth = 0
    private var itemHeight = 0
    private var textSize = 0
    private var textViewSize = 0

    private var isASC = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_feed)

        isDataChanged = false   // 데이터 업데이트 되었는지 판단하는 변수 false 로 초기

        itemWidth = LayoutParamsUtils.getItemWidthByPercent(applicationContext, ITEM_WIDTH_PERCENT)
        itemHeight = LayoutParamsUtils.getItemSizeByRatio(itemWidth, ITEM_WIDTH_DIMENSION_RATIO)
        textSize = LayoutParamsUtils.getItemSizeByRatio(itemWidth, TITLE_TEXT_SIZE_RATIO)
        textViewSize = LayoutParamsUtils.getItemSizeByRatio(itemWidth, TITLE_TEXT_VIEW_SIZE_RATIO)

        Log.d("TAG", "itemWidth = ${itemWidth}, ratio=${TITLE_TEXT_SIZE_RATIO}, textsize = $textSize")


        // add items to scroll view
        // 저장되어있는 앨범 데이터 불러와서 뷰에 추가시킴
        // 불러올 때 이용하는 데이터 : 앨범명, 앨범커버 색, 앨범자켓 이미지 (mAlbumTitle과 일치하는 앨범의 정보 가져옴)

        albumId = intent.getLongExtra("albumId", -1)
        albumTitle = intent.getStringExtra("albumTitle")
        frameType = intent.getIntExtra("frameType", BitmapCropUtils.FRAME_TYPE_0)
        color = intent.getIntExtra("color", getColor(R.color.album_color_pink))
        val statusBarColor = getStatusBarColor(color)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        // set margin at the top of the items
        val leftTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ITEM_LEFT_TOP_MARGIN_PERCENT)
        val rightTopMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ITEM_RIGHT_TOP_MARGIN_PERCENT)
        LayoutParamsUtils.setItemSize(sp_left_top_margin, 1, leftTopMargin)
        LayoutParamsUtils.setItemSize(sp_right_top_margin, 1, rightTopMargin)


        layout_bottom_menu_bar.iv_menu_add.setOnClickListener {
            val intent = Intent(this, AddContentsActivity::class.java)
            intent.putExtra("albumId", albumId)
            intent.putExtra("albumTitle", albumTitle)
            intent.putExtra("color", color)
            intent.putExtra("frameType", frameType)
            startActivity(intent)
        }

        loadAlbumFeed()

    }

    override fun onResume() {
        super.onResume()

        if(isDataChanged){
            loadAlbumFeed()
            isDataChanged = false
        }

    }

    private fun loadAlbumFeed(){
        val getItemCnt = GetItemCnt(applicationContext, albumId)
        getItemCnt.start()
        Thread.sleep(100)

        val getAsc = GetAscOption(applicationContext, albumId)
        getAsc.start()
        Thread.sleep(100)

        cl_album_feed.setBackgroundColor(color)
        initTitleBar(color, albumTitle)
        initBottomMenuBar(color)

        if(albumItemCnt==0){
            tv_init.visibility = View.VISIBLE
//        val initTextSize = LayoutParamsUtils.getItemHeightByPercent(applicationContext, INIT_TEXT_SIZE_PERCENT)
//        tv_init.textSize = initTextSize.toFloat()
        }
        else{
            tv_init.visibility = View.INVISIBLE
        }

        if(isASC){
            layout_top_menu_album_feed.iv_icon_right
                .setImageDrawable(getDrawable(R.drawable.ic_sort_asc))
            showItemASC()
        }
        else{
            layout_top_menu_album_feed.iv_icon_right
                .setImageDrawable(getDrawable(R.drawable.ic_sort_desc))
            showItemDESC()
        }

    }

    private fun showItemASC(){
        albumItemList = ArrayList<FeedItemInfo>()
        val getAlbumFeedItemInfo = GetAlbumFeedItemInfoASC(applicationContext, albumId)
        getAlbumFeedItemInfo.start()
        Thread.sleep(100)

        setFeedItems()
    }

    private fun showItemDESC(){
        albumItemList = ArrayList<FeedItemInfo>()
        val getAlbumFeedItemInfo = GetAlbumFeedItemInfoDESC(applicationContext, albumId)
        getAlbumFeedItemInfo.start()
        Thread.sleep(100)

        setFeedItems()
    }

    private fun setFeedItems(){
        ll_left_side.removeAllViewsInLayout()
        ll_left_side.addView(sp_left_top_margin)

        ll_right_side.removeAllViewsInLayout()
        ll_right_side.addView(sp_right_top_margin)

        for((i, data) in albumItemList.withIndex()){
            val contentsId = data.contentsId
            val coverImageName = data.coverImageName
            val contentsImageName = data.contentsImageName
            val contentsTitle = data.contentsTitle
            val contentsDate = data.contentsDate    // yyyy.M.d/time
            val contentsSentence = data.contentsSentence
            val item = AlbumFeedItem(applicationContext, coverImageName, frameType, contentsTitle)
            item.setTitleTextSize(textSize.toFloat(), textViewSize, (itemHeight*0.6).roundToInt())

            addItemToAlbumFeed(item, i)

            item.setOnClickListener {
                val intent = Intent(this, AlbumFeedContentsActivity::class.java)
                intent.putExtra("contentsId", contentsId)
                intent.putExtra("albumId", albumId)
                intent.putExtra("albumTitle", albumTitle)
                intent.putExtra("color", color)
                intent.putExtra("contentsImage", contentsImageName)
                intent.putExtra("contentsCoverImage", coverImageName)
                intent.putExtra("contentsTitle", contentsTitle)
                val date = contentsDate.substringBefore("/")    // yyyy.M.d
                val dayOfWeek = getDayOfWeekFromDate(applicationContext, date)  // E
                intent.putExtra("contentsDate", "$date.$dayOfWeek")
                intent.putExtra("contentsSentence", contentsSentence)
                intent.putExtra("frameType", frameType)
                startActivity(intent)
            }
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

    private fun initTitleBar(color : Int, title : String){
        layout_top_menu_album_feed.setBackgroundColor(color)
        layout_top_menu_album_feed.tv_album_name_title.text = title

        layout_top_menu_album_feed.iv_icon_left.setOnClickListener {
            finish()
        }

        layout_top_menu_album_feed.iv_icon_right
            .setImageDrawable(getDrawable(R.drawable.ic_sort_asc))

        layout_top_menu_album_feed.iv_icon_right.setOnClickListener {
            isASC = !isASC
            val setAscOption = SetAscOption(applicationContext, albumId, isASC)
            setAscOption.start()
            Thread.sleep(100)

            if(isASC){
                layout_top_menu_album_feed.iv_icon_right
                    .setImageDrawable(getDrawable(R.drawable.ic_sort_asc))
                showItemASC()
            }
            else{
                layout_top_menu_album_feed.iv_icon_right
                    .setImageDrawable(getDrawable(R.drawable.ic_sort_desc))
                showItemDESC()
            }
        }
    }

    private fun initBottomMenuBar(color : Int){
        layout_bottom_menu_bar.setBackgroundColor(color)
        layout_bottom_menu_bar.v_bottom_menu.setBackgroundColor(Color.TRANSPARENT)
        layout_bottom_menu_bar.iv_menu_home.setImageDrawable(getDrawable(R.drawable.ic_home_white))
        layout_bottom_menu_bar.iv_menu_add.setImageDrawable(getDrawable(R.drawable.ic_contents_add_white))
        layout_bottom_menu_bar.iv_menu_setting.setImageDrawable(getDrawable(R.drawable.ic_setting_white))
//        cl_album_feed.layout_top_menu_album_feed.cl_bottom_menu_bar.setBackgroundColor(color) // 이렇게 하면 안됨

        layout_bottom_menu_bar.iv_menu_home.setOnClickListener {
            finish()
        }

    }

    private fun addItemToAlbumFeed(item : AlbumFeedItem, index : Int){
        if(index%2==0){
            ll_left_side.addView(item)
        }
        else{
            ll_right_side.addView(item)
        }
        LayoutParamsUtils.setItemSize(item,itemWidth, itemHeight)
        if(frameType==4){
            val ivImageWidth = LayoutParamsUtils.getItemSizeByRatio(itemWidth, 114f/147f)
            val ivImageHeight = LayoutParamsUtils.getItemSizeByRatio(ivImageWidth, 230f/114f)
            LayoutParamsUtils.setItemSize(item.iv_image, ivImageWidth, ivImageHeight)
            Log.d("FEED", "width=$itemWidth, height=$itemHeight, ($ivImageWidth, $ivImageHeight)")
        }
        val bottomMargin = LayoutParamsUtils.getItemHeightByPercent(applicationContext, ITEM_BOTTOM_MARGIN_PERCENT)
        LayoutParamsUtils.setItemMarginBottom(item, bottomMargin)
    }

    inner class GetItemCnt(val context : Context, private val albumId: Long) : Thread() {
        override fun run(){
            val itemCnt = AlbumDatabase
                    .getInstance(context)!!
                    .albumItemDao()
                    .getAlbumItemCnt(albumId)

            albumItemCnt = itemCnt
        }
    }

    inner class GetAscOption(val context: Context, private val albumId: Long) : Thread(){
        override fun run(){
            val asc = AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .getASC(albumId)

            isASC = asc
        }
    }

    inner class SetAscOption(val context: Context, private val albumId: Long, private val flag: Boolean) : Thread(){
        override fun run(){
            AlbumDatabase
                    .getInstance(context)!!
                    .albumDao()
                    .setASC(albumId, flag)
        }
    }

    inner class GetAlbumFeedItemInfoASC(val context : Context, private val albumId : Long) : Thread() {
        override fun run() {
            val albumFeedItems = AlbumDatabase
                    .getInstance(context)!!
                    .albumItemDao()
                    .getAlbumFeedItemInfoASC(albumId)


            for(item in albumFeedItems){
                val feedItem = FeedItemInfo(item.contentsId, item.coverImageName, item.contentsImageName,
                        item.contentsTitle, item.contentsDate, item.contentsSentence)
                albumItemList.add(feedItem)
            }
        }
    }

    inner class GetAlbumFeedItemInfoDESC(val context : Context, private val albumId : Long) : Thread() {
        override fun run() {
            val albumFeedItems = AlbumDatabase
                .getInstance(context)!!
                .albumItemDao()
                .getAlbumFeedItemInfoDESC(albumId)


            for(item in albumFeedItems){
                val feedItem = FeedItemInfo(item.contentsId, item.coverImageName, item.contentsImageName,
                    item.contentsTitle, item.contentsDate, item.contentsSentence)
                albumItemList.add(feedItem)
            }
        }
    }

}

data class FeedItemInfo(val contentsId : Long, val coverImageName : String, val contentsImageName : String,
                        val contentsTitle : String, val contentsDate : String,
                        val contentsSentence : String)



