package com.hschoi.collect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_contents_bottom_sheet.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumBottomSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContentsBottomMenuSheet(context : Context, contentsId: Long,
                            contentsCoverImage: String, contentsImage: String) : BottomSheetDialogFragment() {
    private val mContext = context
    private val mContentsId = contentsId
    private val mCover = contentsCoverImage
    private val mContentsImage = contentsImage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_contents_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 컨텐츠 수정 버튼
        tv_contents_edit.setOnClickListener {

        }

        // 컨텐츠 공유 버튼
        tv_contents_share.setOnClickListener {

        }

        // 컨텐 삭제 버튼
        tv_contents_delete.setOnClickListener {
            val intent = Intent(mContext, PopUpDialogActivity::class.java)
            intent.putExtra("type", PopUpDialogActivity.Companion.DialogType.CONTENTS_DELETE_CHECK)
            intent.putExtra("contentsId", mContentsId)
            intent.putExtra("contentsCoverImage", mCover)
            intent.putExtra("contentsImage", mContentsImage)
            startActivity(intent)
            dismiss()
        }

    }

}
