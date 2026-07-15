package com.pixelhub.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pixelhub.app.data.local.dao.FavoriteDao
import com.pixelhub.app.data.local.dao.GroupDao
import com.pixelhub.app.data.local.entity.FavoriteEntity
import com.pixelhub.app.data.local.entity.GroupEntity

@Database(
    entities = [FavoriteEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun groupDao(): GroupDao
}
