package com.hschoi.collect.database.entity

import android.graphics.Matrix
import androidx.room.*

@Entity(tableName = "album")
data class AlbumEntity(
    var isAsc: Boolean,
    var albumTitle : String,
    var frameType : Int,
    var albumColor : Int,
    var coverImageFileName : String,
    var coverImageOriginFileName : String,
    var zoom : Float,
    var x : Float,
    var y : Float
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var albumOrder: Long = 0
}