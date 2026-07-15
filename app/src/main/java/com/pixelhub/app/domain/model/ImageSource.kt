package com.pixelhub.app.domain.model

data class ImageSource(
    val id: String, val name: String, val url: String,
    val type: ApiType, val enabled: Boolean = true,
    val rateLimit: Int = 10, val isBuiltIn: Boolean = false
)

enum class ApiType { LOLICON, DIRECT_IMAGE, YPPP, AUTO_DETECT }

object BuiltInApis {
    val sources = listOf(
        ImageSource("lolicon", "Lolicon", "https://api.lolicon.app/setu/v2", ApiType.LOLICON, isBuiltIn = true),
        ImageSource("elaina", "Elaina Cat", "https://api.elaina.cat/random/", ApiType.DIRECT_IMAGE, isBuiltIn = true),
        ImageSource("czl", "CZL 随机图", "https://random-api.czl.net/pic/all", ApiType.DIRECT_IMAGE, isBuiltIn = true),
        ImageSource("picsum", "Picsum", "https://picsum.photos/600/800", ApiType.DIRECT_IMAGE, isBuiltIn = true),
    )
}
