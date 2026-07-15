package com.pixelhub.app.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoliconResponse(
    val error: String,
    val data: List<LoliconData>
)

data class LoliconData(
    val pid: Int,
    val uid: Int,
    val title: String,
    val author: String,
    val r18: Boolean,
    val tags: List<String>,
    val ext: String,
    val uploadDate: Long,
    val width: Int,
    val height: Int,
    val urls: LoliconUrls?
)

data class LoliconUrls(
    val original: String?,
    val regular: String?,
    val small: String?,
    val thumb: String?,
    val mini: String?
)

data class YpppResponse(
    @SerializedName("acgurl")
    val acgUrl: String,
    val width: Int,
    val height: Int
)

data class ApiSourceConfig(
    val id: String,
    val name: String,
    val url: String,
    val type: String,
    val enabled: Boolean = true,
    val rateLimit: Int = 10
)
