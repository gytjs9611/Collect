package com.hschoi.collect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.util.UnitConversion
import kotlinx.android.synthetic.main.fragment_album_bottom_sheet.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumBottomSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumBottomMenuSheet(context : Context, albumId : Long) : BottomSheetDialogFragment() {
    private val mContext = context
    private val mAlbumId = albumId

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_album_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앨범 수정 버튼
        tv_album_edit.setOnClickListener {

        }

        // 앨범 삭제 버튼
        tv_album_delete.setOnClickListener {
            val intent = Intent(mContext, PopUpDialogActivity::class.java)
            intent.putExtra("type", PopUpDialogActivity.Companion.DialogType.DELETE_CHECK)
            intent.putExtra("albumId", mAlbumId)
            intent.putExtra("title", mContext.getString(R.string.delete_album))
            intent.putExtra("description", mContext.getString(R.string.delte_album_description))
            startActivity(intent)
            dismiss()


        }

    }

}
