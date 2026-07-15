package com.pixelhub.app.domain.model

data class ImageItem(
    val id: String,
    val url: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
    val tags: List<String>,
    val source: String,
    val sourceUrl: String,
    val rating: String = "safe" // safe, questionable, explicit
)
