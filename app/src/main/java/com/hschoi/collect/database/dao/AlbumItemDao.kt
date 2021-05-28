package com.hschoi.collect.database.dao

import androidx.room.*
import com.hschoi.collect.database.entity.AlbumItemEntity

@Dao
interface AlbumItemDao {
    /*@Query("SELECT * FROM album_item")
    fun getAllContents() : List<AlbumItemEntity>*/

    @Query("SELECT * FROM album_item WHERE albumId = :albumId")
    fun getAllAlbumItems(albumId : Long) : List<AlbumItemEntity>

    @Query("SELECT * FROM album_item WHERE albumId = :albumId ORDER BY contentsDate ASC")
    fun getAlbumFeedItemInfoASC(albumId : Long) : List<AlbumItemEntity>

    @Query("SELECT * FROM album_item WHERE albumId = :albumId ORDER BY contentsDate DESC")
    fun getAlbumFeedItemInfoDESC(albumId : Long) : List<AlbumItemEntity>


    @Query("SELECT COUNT(*) FROM album_item WHERE albumId = :albumId")
    fun getAlbumItemCnt(albumId : Long) : Int

    @Insert
    fun insertContents(entity: AlbumItemEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateContents(entity: AlbumItemEntity)


    @Query("DELETE FROM album_item WHERE contentsId=:id")
    fun deleteContents(id: Long)

    @Query("SELECT * FROM album_item WHERE contentsId = :contentsId")
    fun getAlbumItemEntity(contentsId: Long): AlbumItemEntity
}