package com.hschoi.collect

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hschoi.collect.database.AlbumDatabase
import com.hschoi.collect.database.entity.AlbumEntity
import kotlinx.android.synthetic.main.activity_popup.*
import java.io.File


class PopUpDialogActivity: AppCompatActivity() {
    companion object{
        enum class DialogType {
            PREMIUM_INFO, DELETE_CHECK
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)

        val type = intent.getSerializableExtra("type")
        tv_title.text = intent.getStringExtra("title")
        tv_description.text = intent.getStringExtra("description")
        val albumId = intent.getLongExtra("albumId", -1)

        when(type){
            DialogType.DELETE_CHECK->{
                // 취소
                tv_left.setOnClickListener {
                    finish()
                }
                // 삭제
                tv_right.setOnClickListener {
                    deleteAlbum(albumId)
                    finish()
                }
            }
            DialogType.PREMIUM_INFO->{

            }
        }

    }

    private fun deleteAlbum(id: Long){

        // 앨범 커버 이미지 삭제
        for(album in MainActivity.albumList) {
               if (album.id == id) {
                   MainActivity.albumList.remove(album)

                   val deleteThread = DeleteAlbum(applicationContext, id)
                   deleteThread.start()
                   Thread.sleep(100)

                   // 이미지 파일 삭제
                   applicationContext.deleteFile(album.imageFileName)
                   break
               }
        }

        // 앨범 내 컨텐츠 이미지 모두 삭제
        val files = applicationContext.filesDir.listFiles()
        for(file in files){
            if(file.nameWithoutExtension.startsWith("contents_${id}_")){
                file.delete()
            }
            else if(file.nameWithoutExtension.startsWith("contents_cover_${id}_")){
                file.delete()
            }
        }



        // 모든 앨범 삭제될 경우, 더미 앨범 객체 삭제, 디폴트 앨범 추가 카드 보이게 설정
        if(MainActivity.albumList.size==1){
            MainActivity.albumList.removeAt(0)
            MainActivity.defaultAddAlbum.visibility = View.VISIBLE
        }

        // 리사이클러뷰 업데이트하기
        MainActivity.homeRecyclerAdapter.notifyDataSetChanged()
        MainActivity.addContentsRecyclerAdapter.notifyDataSetChanged()
    }

}


class DeleteAlbum(val context : Context, private val albumId : Long) : Thread() {
    override fun run(){
        AlbumDatabase.getInstance(context)!!
                .albumDao()
                .deleteAlbum(albumId)
    }
}