package com.hschoi.collect.database.entity

import androidx.room.*

@Entity(tableName = "album")
data class AlbumEntity(
    var isAsc: Boolean,
    var albumTitle : String,
    var frameType : Int,
    var albumColor : Int,
    var coverImageFileName : String
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var albumOrder: Long = 0
}