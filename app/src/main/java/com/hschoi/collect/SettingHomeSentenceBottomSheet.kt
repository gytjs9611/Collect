package com.hschoi.collect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_setting_home_sentence_sheet.*
import kotlinx.android.synthetic.main.layout_common_title.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumBottomSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingHomeSentenceBottomSheet(context : Context) : BottomSheetDialogFragment() {
    private val mContext = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_setting_home_sentence_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedSubtitle = MainActivity.sharedPref.getString(MainActivity.HOME_SENTENCE_KEY, null)
        l_home_sentence_edit.et_title.setText(savedSubtitle)


        l_home_sentence_edit.et_title.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                l_home_sentence_edit.cv_string_length.visibility = View.VISIBLE
                l_home_sentence_edit.v_title_underline.setBackgroundColor(mContext.getColor(R.color.edit_text_underline_focus))
            }
            else{
                l_home_sentence_edit.cv_string_length.visibility = View.INVISIBLE
                l_home_sentence_edit.v_title_underline.setBackgroundColor(mContext.getColor(R.color.edit_text_underline_unfocus))
            }
        }


        l_home_sentence_edit.et_title.requestFocus()


        // 제목 입력된 글자 수 표시
        l_home_sentence_edit.et_title.addTextChangedListener {
            l_home_sentence_edit.tv_length.text = "${it?.length}/${resources.getInteger(R.integer.title_max_length)}"
        }

        iv_close.setOnClickListener {
            dismiss()
        }

    }

    override fun onStop() {
        val editor = MainActivity.sharedPref.edit()
        editor.putString(MainActivity.HOME_SENTENCE_KEY, l_home_sentence_edit.et_title.text.toString())
        editor.commit()
        super.onStop()
    }


}
