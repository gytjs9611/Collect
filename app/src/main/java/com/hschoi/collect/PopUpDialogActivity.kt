package com.hschoi.collect

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hschoi.collect.database.AlbumDatabase
import kotlinx.android.synthetic.main.activity_popup.*


class PopUpDialogActivity: AppCompatActivity() {
    companion object{
        enum class DialogType {
            PREMIUM_INFO, ALBUM_DELETE_CHECK, CONTENTS_DELETE_CHECK
        }
    }

    private var mAlbumId = -1
    private lateinit var mContentsCoverImageName : String
    private lateinit var mContentsImageName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)

        val type = intent.getSerializableExtra("type")
        val albumId = intent.getLongExtra("albumId", -1)


        when(type){
            DialogType.ALBUM_DELETE_CHECK->{
                tv_title.text = getString(R.string.delete_album)
                tv_description.text = getString(R.string.delete_album_description)
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
            DialogType.CONTENTS_DELETE_CHECK->{
                tv_title.text = getString(R.string.delete_contents)
                tv_description.text = getString(R.string.delete_contents_description)
                // 취소
                tv_left.setOnClickListener {
                    finish()
                }
                // 삭제
                tv_right.setOnClickListener {
                    val contentsId = intent.getLongExtra("contentsId", -1)
                    val contentsCoverImage = intent.getStringExtra("contentsCoverImage")
                    val contentsImage = intent.getStringExtra("contentsImage")
                    deleteContents(contentsId, contentsCoverImage, contentsImage)
                    finish()
                }
            }
            DialogType.PREMIUM_INFO->{

            }
        }

    }


    private fun deleteContents(contentsId: Long, cover: String, image: String){
        val deleteThread = DeleteContents(applicationContext, contentsId)
        deleteThread.start()
        Thread.sleep(100)

        // 이미지 파일 삭제
        val files = applicationContext.filesDir.listFiles()
        for(file in files){
            if(file.nameWithoutExtension+".png" == image){
                file.delete()
            }
            else if(file.nameWithoutExtension+".png" == cover){
                file.delete()
            }
        }
    }



    private fun deleteAlbum(id: Long){

        // 앨범 커버 이미지 삭제
        for(album in MainActivity.albumList) {
               if (album.id == id) {
                   MainActivity.albumList.remove(album)
                   Log.d("home", "size=${MainActivity.albumList.size}")

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

class DeleteContents(val context: Context, private val contentsId: Long):Thread(){
    override fun run() {
        AlbumDatabase.getInstance(context)!!
            .albumItemDao()
            .deleteContents(contentsId)
    }
}