package com.hschoi.collect.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "album_item",
        foreignKeys = [ForeignKey(
                            entity = AlbumEntity::class,
                            parentColumns=arrayOf("id"),
                            childColumns=arrayOf("albumId"),
                            onDelete = ForeignKey.CASCADE
                        )]
        )
data class AlbumItemEntity(
        var albumId : Long,
        var albumTitle : String,
        var contentsTitle : String,
        var contentsDate : String,
        var contentsSentence : String,
        var coverImageName : String,
        var coverImageIndex : Int,
        var contentsImageName : String,
        var frameType : Int,
        var zoom : Float,
        var x : Float,
        var y : Float
){
    @PrimaryKey(autoGenerate = true)
    var contentsId : Long = 0
}