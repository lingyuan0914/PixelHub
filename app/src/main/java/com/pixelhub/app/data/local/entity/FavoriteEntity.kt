package com.pixelhub.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
    val tags: String, // JSON serialized list
    val source: String,
    val sourceUrl: String,
    val rating: String = "safe",
    val groupId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
