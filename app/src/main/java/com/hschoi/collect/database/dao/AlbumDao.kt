package com.hschoi.collect.database.dao

import android.graphics.Matrix
import androidx.room.*
import com.hschoi.collect.database.entity.AlbumEntity

@Dao
interface AlbumDao {
    @Query("SELECT * FROM album")
    fun getAllAlbums() : List<AlbumEntity>

    @Query("SELECT COUNT(*) FROM album")
    fun getAlbumCnt() : Int

    @Query("SELECT * FROM album WHERE id = :id")
    fun getAlbum(id : Long) : AlbumEntity


    @Query("UPDATE album SET albumOrder=:order WHERE id=:id")
    fun setOrder(id: Long, order: Long)


    @Query("SELECT isAsc FROM album WHERE id=:id")
    fun getASC(id: Long): Boolean

    @Query("UPDATE album SET isAsc=:flag WHERE id=:id")
    fun setASC(id: Long, flag: Boolean)

    @Insert
    fun insertAlbum(entity: AlbumEntity) : Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateAlbum(entity: AlbumEntity)

    @Query("DELETE FROM album WHERE id=:id")
    fun deleteAlbum(id: Long)




}