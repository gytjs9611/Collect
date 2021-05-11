package com.hschoi.collect.database.dao

import androidx.room.*
import com.hschoi.collect.FeedItemInfo
import com.hschoi.collect.database.entity.AlbumItemEntity

@Dao
interface AlbumItemDao {
    /*@Query("SELECT * FROM album_item")
    fun getAllContents() : List<AlbumItemEntity>*/

    @Query("SELECT * FROM album_item WHERE albumId = :albumId")
    fun getAllAlbumItems(albumId : Long) : List<AlbumItemEntity>

    @Query("SELECT contentsId, coverImageName, contentsImageName, contentsTitle, contentsDate, contentsSentence FROM album_item WHERE albumId = :albumId ORDER BY contentsDate ASC")
    fun getAlbumFeedItemInfoASC(albumId : Long) : List<FeedItemInfo>

    @Query("SELECT contentsId, coverImageName, contentsImageName, contentsTitle, contentsDate, contentsSentence FROM album_item WHERE albumId = :albumId ORDER BY contentsDate DESC")
    fun getAlbumFeedItemInfoDESC(albumId : Long) : List<FeedItemInfo>


    @Query("SELECT COUNT(*) FROM album_item WHERE albumId = :albumId")
    fun getAlbumItemCnt(albumId : Long) : Int

    @Insert
    fun insertContents(entity: AlbumItemEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateContents(entity: AlbumItemEntity)


    @Query("DELETE FROM album_item WHERE contentsId=:id")
    fun deleteContents(id: Long)

}