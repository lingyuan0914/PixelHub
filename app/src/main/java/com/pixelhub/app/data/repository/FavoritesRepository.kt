package com.pixelhub.app.data.repository

import com.google.gson.Gson
import com.pixelhub.app.data.local.dao.FavoriteDao
import com.pixelhub.app.data.local.dao.GroupDao
import com.pixelhub.app.data.local.entity.FavoriteEntity
import com.pixelhub.app.data.local.entity.GroupEntity
import com.pixelhub.app.domain.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val groupDao: GroupDao,
    private val gson: Gson
) {
    fun getAllFavorites(): Flow<List<ImageItem>> =
        favoriteDao.getAllFavorites().map { it.map { e -> e.toDomain() } }

    fun getFavoritesByGroup(groupId: String): Flow<List<ImageItem>> =
        favoriteDao.getFavoritesByGroup(groupId).map { it.map { e -> e.toDomain() } }

    fun getUngroupedFavorites(): Flow<List<ImageItem>> =
        favoriteDao.getUngroupedFavorites().map { it.map { e -> e.toDomain() } }

    suspend fun isFavorite(id: String) = favoriteDao.isFavorite(id)

    suspend fun toggleFavorite(image: ImageItem) {
        if (favoriteDao.getFavoriteById(image.id) != null) {
            favoriteDao.deleteFavorite(image.toEntity())
        } else {
            favoriteDao.insertFavorite(image.toEntity())
        }
    }

    suspend fun removeFromFavorites(id: String) {
        favoriteDao.getFavoriteById(id)?.let { favoriteDao.deleteFavorite(it) }
    }

    suspend fun batchRemoveFromFavorites(ids: List<String>) {
        favoriteDao.deleteFavoritesByIds(ids)
    }

    suspend fun batchMoveToGroup(ids: List<String>, groupId: String?) {
        favoriteDao.moveToGroup(ids, groupId)
    }

    fun getAllGroups() = groupDao.getAllGroups()

    suspend fun createGroup(name: String) {
        groupDao.insertGroup(GroupEntity(UUID.randomUUID().toString(), name))
    }

    suspend fun deleteGroup(group: GroupEntity) {
        // Move favorites from this group to ungrouped before deleting
        val favoritesInGroup = favoriteDao.getFavoritesByGroup(group.id).first()
        if (favoritesInGroup.isNotEmpty()) {
            favoriteDao.moveToGroup(favoritesInGroup.map { it.id }, null)
        }
        groupDao.deleteGroup(group)
    }

    suspend fun exportToJson(): String {
        val favorites = favoriteDao.getAllFavorites().first()
        val groups = groupDao.getAllGroups().first()
        return gson.toJson(mapOf("favorites" to favorites, "groups" to groups))
    }

    suspend fun importFromJson(json: String) {
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(json, type)
        val groups: List<GroupEntity> = gson.fromJson(gson.toJson(map["groups"]), object : com.google.gson.reflect.TypeToken<List<GroupEntity>>() {}.type)
        val favorites: List<FavoriteEntity> = gson.fromJson(gson.toJson(map["favorites"]), object : com.google.gson.reflect.TypeToken<List<FavoriteEntity>>() {}.type)
        groups.forEach { groupDao.insertGroup(it) }
        favorites.forEach { favoriteDao.insertFavorite(it) }
    }

    private fun FavoriteEntity.toDomain() = ImageItem(
        id, url, thumbnailUrl, width, height, author,
        try { gson.fromJson(tags, Array<String>::class.java).toList() } catch (_: Exception) { emptyList() },
        source, sourceUrl, rating
    )

    private fun ImageItem.toEntity() = FavoriteEntity(
        id, url, thumbnailUrl, width, height, author, gson.toJson(tags), source, sourceUrl, rating
    )
}
