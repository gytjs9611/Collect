package com.hschoi.collect

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.layout_bottom_menu_bar.view.*
import kotlinx.android.synthetic.main.layout_setting_item.view.*
import kotlinx.android.synthetic.main.layout_setting_title.view.*
import kotlinx.android.synthetic.main.layout_top_menu_bar.view.*

class SettingActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initView()

        setClickListeners()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    private fun setClickListeners(){
        layout_bottom_menu_bar.iv_menu_home.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // 한 줄 소개 수정
        setting_home_sentence.setOnClickListener {
            val homeSentenceBottomSheet = SettingHomeSentenceBottomSheet(this)
            homeSentenceBottomSheet.show(supportFragmentManager, null)
        }

    }

    private fun initView(){
        // 홈 화면 설정
        setting_home_change_album_order.tv_item_title.text = getString(R.string.setting_change_album_order)

        // 구매
        setting_premium_title.tv_setting_menu_title.text = getString(R.string.setting_premium_title)
        setting_premium.tv_item_title.text = getString(R.string.setting_premium_item)

        // 피드백
        setting_feedback_title.tv_setting_menu_title.text = getString(R.string.setting_feedback_title)
        setting_review.tv_item_title.text = getString(R.string.setting_feedback_review)
        setting_question_feedback.tv_item_title.text = getString(R.string.setting_feedback_question)

        // 정보
        setting_info_title.tv_setting_menu_title.text = getString(R.string.setting_info_title)
        setting_info_version.tv_item_title.text = getString(R.string.setting_info_version)
        setting_info_open_source.tv_item_title.text = getString(R.string.setting_info_open_source)

        setting_info_version.tv_sub_item.text = "v 1.0"
        setting_info_version.tv_version_info.text = "최신 버전"

        layout_bottom_menu_bar.iv_menu_setting.imageTintList = ColorStateList.valueOf(getColor(R.color.black))
    }


}