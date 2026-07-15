package com.pixelhub.app.data.local.dao

import androidx.room.*
import com.pixelhub.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getFavoritesByGroup(groupId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE groupId IS NULL ORDER BY createdAt DESC")
    fun getUngroupedFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: String): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id IN (:ids)")
    suspend fun deleteFavoritesByIds(ids: List<String>)

    @Query("UPDATE favorites SET groupId = :groupId WHERE id IN (:ids)")
    suspend fun moveToGroup(ids: List<String>, groupId: String?)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}
