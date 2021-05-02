package com.hschoi.collect.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hschoi.collect.database.dao.AlbumDao
import com.hschoi.collect.database.dao.AlbumItemDao
import com.hschoi.collect.database.entity.AlbumEntity
import com.hschoi.collect.database.entity.AlbumItemEntity

@Database(entities= [AlbumEntity::class, AlbumItemEntity::class], version = 1, exportSchema = false)
abstract class AlbumDatabase : RoomDatabase() {
    abstract fun albumDao() : AlbumDao
    abstract fun albumItemDao() : AlbumItemDao

    companion object {
        private var instance: AlbumDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AlbumDatabase? {
            if (instance == null) {
                synchronized(AlbumDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AlbumDatabase::class.java,
                        "database-album"
                    )
                        .build()
                }
            }
            return instance
        }
    }
}