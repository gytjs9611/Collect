package com.hschoi.collect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hschoi.collect.adapter.AddContentsRecyclerAdapter
import com.hschoi.collect.adapter.HomeRecyclerAdapter
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.util.UnitConversion
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_bottom_menu_bar.view.*

class MainActivity : AppCompatActivity() {
    companion object{
        const val CARD_ELEVATION = 40f
        const val CARD_RADIUS = 8f
        const val CARD_DIMENSION_RATIO = 210f/343f
        const val CARD_HEIGHT_PERCENT = 343f/716f

        private const val FINISH_INTERVAL_TIME = 1000
        lateinit var defaultAddAlbum : View
        lateinit var albumList : ArrayList<Albums>

        lateinit var homeRecyclerView : RecyclerView
        lateinit var homeRecyclerAdapter : HomeRecyclerAdapter
        lateinit var addContentsRecyclerAdapter: AddContentsRecyclerAdapter

        var isAddFromHome = false

        lateinit var sharedPref : SharedPreferences
        const val SHARED_PREF_KEY = "com.hschoi.collect.SHARED_PREF_KEY"
        const val HOME_SENTENCE_KEY = "com.hschoi.collect.HOME_SENTENCE_KEY"
    }


    private var backPressedTime : Long = 0
    private var collapseDrag = false
    private var isHiddenCalled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)


        albumList = ArrayList()

        homeRecyclerView = rv_album_card
        homeRecyclerAdapter = HomeRecyclerAdapter(albumList)
        addContentsRecyclerAdapter = AddContentsRecyclerAdapter(albumList)


        // 리사이클러뷰 초기화
        rv_album_card.adapter = homeRecyclerAdapter
        rv_album_card.layoutManager = GridLayoutManager(applicationContext, 1, RecyclerView.HORIZONTAL, false)

        val decoration = HomeRecyclerDecoration(applicationContext)
        rv_album_card.addItemDecoration(decoration)


        // 하단 슬라이딩뷰 내의 리사이클러뷰 초기화
        rv_album_list.adapter = addContentsRecyclerAdapter
        rv_album_list.layoutManager = GridLayoutManager(applicationContext, 1, RecyclerView.VERTICAL, false)
        val decoration2 = AddContentsRecyclerDecoration(applicationContext)
        rv_album_list.addItemDecoration(decoration2)


        // 데이터베이스로부터 데이터 받아와 albumList에 적용
        val getAlbums = GetAlbum(applicationContext)
        getAlbums.start()
        Thread.sleep(100)



        // 하단 메뉴 관련
        list_panel.panelHeight = UnitConversion.dpToPx(applicationContext, 170f).toInt()
        list_panel.panelState = PanelState.HIDDEN
        list_panel.isTouchEnabled = true


        cl_fake_fade.setOnClickListener {
            list_panel.panelState = PanelState.HIDDEN
            cl_fake_fade.setBackgroundColor(Color.TRANSPARENT)
            cl_fake_fade.isClickable = false
        }
        cl_fake_fade.isClickable = false    // setonclicklistener 설정해주면 자동으로 clickable true로 설정되므로 바로 뒤에 false 로 설정해줌


        list_panel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                Log.d("slide", "slide offset = $slideOffset")

            }

            override fun onPanelStateChanged(panel: View, previousState: PanelState,
                                             newState: PanelState) {
                Log.d("slide", "cd=${collapseDrag}, prev=$previousState, newState=$newState")
                if(newState==PanelState.HIDDEN){
                    cl_fake_fade.setBackgroundColor(Color.TRANSPARENT)
                    cl_fake_fade.isClickable = false
                }

                if(previousState==PanelState.HIDDEN){
                    rv_album_list.smoothScrollToPosition(0)
                }


            }
        })



//        list_panel.setFadeOnClickListener {
//            list_panel.panelState = PanelState.HIDDEN
//        }

        layout_bottom_menu_bar.iv_menu_home.imageTintList = ColorStateList.valueOf(getColor(R.color.black))
        

        // 홈 버튼 누르면 가장 첫번째 아이템으로 스크롤 이동
       layout_bottom_menu_bar.cl_menu_home.setOnClickListener {
            rv_album_card.smoothScrollToPosition(0)
        }


        // 하단 컨텐츠 추가 메뉴 클릭 이벤트 설정
        layout_bottom_menu_bar.cl_menu_add.setOnClickListener {
            // 앨범 리스트 버튼 좌라락 바텀 메뉴
            list_panel.isTouchEnabled = true
            list_panel.panelState = PanelState.COLLAPSED
            cl_fake_fade.setBackgroundColor(getColor(R.color.fade))
            cl_fake_fade.isClickable = true
        }

        // 하단 세팅 메뉴 클릭 이벤트 설정
        layout_bottom_menu_bar.cl_menu_setting.setOnClickListener {
            // setting activity
            startActivity(Intent(this, SettingActivity::class.java))
            overridePendingTransition(0, 0)
        }

        rv_album_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                Log.d("scroll", "newState=$newState")
                if (rv_album_list.canScrollVertically(-1)) {
                    Log.i("scroll", "Top of list")
                } else if (!rv_album_list.canScrollVertically(1)) {
                    Log.i("scroll", "End of list")
                } else {
                    Log.i("scroll", "idle")
                }
            }
        })



   }

    override fun onResume() {
        super.onResume()

        var savedSubtitle = sharedPref.getString(HOME_SENTENCE_KEY, null)
        if(savedSubtitle==null){
            val defaultSentence = getString(R.string.app_subtitle)
            sharedPref.edit().putString(HOME_SENTENCE_KEY, defaultSentence).commit()
            savedSubtitle = defaultSentence
        }
        tv_app_subtitle.text = savedSubtitle


    }

    override fun onPause() {
        super.onPause()
        list_panel.panelState = PanelState.HIDDEN

        cl_fake_fade.setBackgroundColor(Color.TRANSPARENT)
        cl_fake_fade.isClickable = false

    }


    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if(list_panel.panelState == PanelState.COLLAPSED){
            list_panel.panelState = PanelState.HIDDEN
            cl_fake_fade.setBackgroundColor(Color.TRANSPARENT)
            cl_fake_fade.isClickable = false
        }
        else if(list_panel.panelState == PanelState.EXPANDED){
            list_panel.panelState = PanelState.COLLAPSED
        }
        else{
            if(intervalTime in 0..FINISH_INTERVAL_TIME){
                super.onBackPressed()
            }
            else{
                backPressedTime = tempTime
                Toast.makeText(this, getString(R.string.backpressed_finish), Toast.LENGTH_SHORT).show()
            }
        }


    }

}


class GetAlbum(val context : Context) : Thread() {
    override fun run(){
        val items = AlbumDatabase
            .getInstance(context)!!
            .albumDao()
            .getAllAlbums()

        for(album in items){
            val id = album.id
            val title = album.albumTitle
            val frameType = album.frameType
            val albumColor = album.albumColor
            val coverImagePath = album.coverImageFileName
            val albumsItem = Albums(id, title, albumColor, coverImagePath, frameType)
            MainActivity.albumList.add(albumsItem)
        }
        MainActivity.albumList.add(Albums(-1, "", -1, "", -1))   // dummy

    }
}
