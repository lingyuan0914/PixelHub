package com.pixelhub.app.data.remote

import com.google.gson.Gson
import com.pixelhub.app.data.remote.model.LoliconResponse
import com.pixelhub.app.data.remote.model.YpppResponse
import com.pixelhub.app.domain.model.ApiType
import com.pixelhub.app.domain.model.ImageItem
import com.pixelhub.app.domain.model.ImageSource
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAdapter @Inject constructor(
    private val api: PixelHubApi,
    private val gson: Gson
) {
    // Server API methods
    suspend fun fetchFromServer(serverUrl: String, source: ImageSource, tag: String?): List<ImageItem> {
        return try {
            val sourceParam = if (source.isBuiltIn) source.id else "all"
            val tagParam = tag?.let { "&tag=$it" } ?: ""
            val url = "$serverUrl/api/images?source=$sourceParam$tagParam"
            
            val response = api.fetchFromUrl(url)
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: return emptyList()
                val json = gson.fromJson(body, ServerResponse::class.java)
                json.images.map { it.toDomain() }
            } else emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Server fetch failed")
            emptyList()
        }
    }

    suspend fun toggleFavoriteOnServer(serverUrl: String, image: ImageItem) {
        try {
            val body = gson.toJson(mapOf(
                "id" to image.id,
                "url" to image.url,
                "thumbnailUrl" to image.thumbnailUrl,
                "width" to image.width,
                "height" to image.height,
                "author" to image.author,
                "tags" to image.tags,
                "source" to image.source,
                "sourceUrl" to image.sourceUrl,
                "rating" to image.rating
            ))
            // This would need a POST request - simplified for now
            Timber.d("Toggle favorite on server: ${image.id}")
        } catch (e: Exception) {
            Timber.e(e, "Server favorite toggle failed")
        }
    }

    suspend fun fetchImages(source: ImageSource, tag: String? = null, r18: Boolean = false): List<ImageItem> {
        return try {
            when (source.type) {
                ApiType.LOLICON -> fetchLolicon(source, tag, r18)
                ApiType.DIRECT_IMAGE -> fetchDirectImage(source)
                ApiType.YPPP -> fetchYppp(source)
                ApiType.AUTO_DETECT -> fetchAutoDetect(source, tag, r18)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch from ${source.name}")
            emptyList()
        }
    }

    private suspend fun fetchLolicon(source: ImageSource, tag: String?, r18: Boolean): List<ImageItem> {
        // Parse tag: support "tag1,tag2" as AND, "tag1|tag2" as OR within a group
        // Also support "-tag" as notTag
        val (positiveTags, negativeTags) = parseTags(tag)

        val tagParam = if (positiveTags.isNotEmpty()) {
            // Each comma-separated group becomes an AND match
            // Items within a group separated by | are OR
            positiveTags.joinToString(",") { it }
        } else null

        val notTagParam = if (negativeTags.isNotEmpty()) {
            negativeTags.joinToString(",")
        } else null

        Timber.d("Lolicon fetch: tag=$tagParam, notTag=$notTagParam, r18=${if (r18) 1 else 0}")

        val response = api.getLoliconImages(
            r18 = if (r18) 1 else 0,
            num = 20,
            tag = tagParam,
            proxy = "i.pixiv.cat"
        )

        // Filter by notTag on client side (API may not support notTag directly)
        val filtered = if (negativeTags.isNotEmpty()) {
            response.data.filter { data ->
                negativeTags.none { notTag ->
                    data.tags.any { it.contains(notTag, ignoreCase = true) }
                }
            }
        } else response.data

        return filtered.mapNotNull { data ->
            val url = data.urls?.original ?: data.urls?.regular ?: data.urls?.small ?: data.urls?.thumb
            val thumbUrl = data.urls?.regular ?: data.urls?.small ?: data.urls?.thumb ?: url
            if (url.isNullOrBlank()) {
                Timber.w("Lolicon item ${data.pid} has null/empty URL, skipping")
                return@mapNotNull null
            }
            ImageItem(
                id = "lolicon_${data.pid}",
                url = url,
                thumbnailUrl = thumbUrl ?: url,
                width = data.width,
                height = data.height,
                author = data.author ?: "未知",
                tags = data.tags ?: emptyList(),
                source = source.name,
                sourceUrl = "https://www.pixiv.net/artworks/${data.pid}",
                rating = if (data.r18) "explicit" else "safe"
            )
        }
    }

    /**
     * Parse search query into positive and negative tags.
     * "原神,甘雨" → AND match: ["原神", "甘雨"]
     * "原神,甘雨,-黑白" → AND: ["原神", "甘雨"], NOT: ["黑白"]
     * "原神|崩坏,少女" → OR group: ["原神|崩坏"], AND: ["少女"]
     */
    private fun parseTags(tag: String?): Pair<List<String>, List<String>> {
        if (tag.isNullOrBlank()) return emptyList<String>() to emptyList()

        val positive = mutableListOf<String>()
        val negative = mutableListOf<String>()

        tag.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { part ->
            if (part.startsWith("-")) {
                negative.add(part.removePrefix("-").trim())
            } else {
                positive.add(part)
            }
        }

        return positive to negative
    }

    private suspend fun fetchDirectImage(source: ImageSource): List<ImageItem> {
        Timber.d("fetchDirectImage: ${source.name} url=${source.url}")
        val baseTs = System.currentTimeMillis()
        return (1..20).map { i ->
            val sep = if (source.url.contains("?")) "&" else "?"
            val uniqueUrl = "${source.url}${sep}_t=${baseTs}_${i}_${UUID.randomUUID().toString().take(8)}"
            ImageItem(
                id = "${source.id}_${UUID.randomUUID()}", url = uniqueUrl, thumbnailUrl = uniqueUrl,
                width = 0, height = 0, author = "未知", tags = emptyList(),
                source = source.name, sourceUrl = source.url, rating = "safe"
            )
        }
    }

    private suspend fun fetchYppp(source: ImageSource): List<ImageItem> {
        val response = api.fetchFromUrl(source.url)
        return if (response.isSuccessful) {
            val body = response.body()?.string() ?: return emptyList()
            try {
                val yppp = gson.fromJson(body, YpppResponse::class.java)
                if (yppp.acgUrl.isNotBlank()) {
                    listOf(ImageItem("yppp_${UUID.randomUUID()}", yppp.acgUrl, yppp.acgUrl, yppp.width, yppp.height, "未知", emptyList(), source.name, yppp.acgUrl, "safe"))
                } else emptyList()
            } catch (e: Exception) {
                if (body.trim().startsWith("http"))
                    listOf(ImageItem("yppp_${UUID.randomUUID()}", body.trim(), body.trim(), 0, 0, "未知", emptyList(), source.name, body.trim(), "safe"))
                else emptyList()
            }
        } else emptyList()
    }

    private suspend fun fetchAutoDetect(source: ImageSource, tag: String?, r18: Boolean): List<ImageItem> {
        val response = api.fetchFromUrl(source.url)
        if (!response.isSuccessful) return emptyList()

        val contentType = response.headers()["Content-Type"] ?: ""
        val body = response.body()

        return when {
            contentType.contains("image/") -> {
                listOf(ImageItem("auto_${UUID.randomUUID()}", response.raw().request.url.toString(), response.raw().request.url.toString(), 0, 0, "未知", emptyList(), source.name, source.url, "safe"))
            }
            contentType.contains("application/json") -> {
                val jsonStr = body?.string() ?: return emptyList()
                try {
                    val lolicon = gson.fromJson(jsonStr, LoliconResponse::class.java)
                    if (lolicon.data.isNotEmpty()) {
                        return lolicon.data.mapNotNull { d ->
                            val url = d.urls?.original ?: d.urls?.regular ?: d.urls?.small
                            if (url.isNullOrBlank()) return@mapNotNull null
                            ImageItem("lolicon_${d.pid}", url, d.urls?.small ?: url, d.width, d.height, d.author ?: "未知", d.tags ?: emptyList(), source.name, "https://www.pixiv.net/artworks/${d.pid}", if (d.r18) "explicit" else "safe")
                        }
                    }
                } catch (_: Exception) {}
                try {
                    val yppp = gson.fromJson(jsonStr, YpppResponse::class.java)
                    if (yppp.acgUrl.isNotBlank()) {
                        return listOf(ImageItem("yppp_${UUID.randomUUID()}", yppp.acgUrl, yppp.acgUrl, yppp.width, yppp.height, "未知", emptyList(), source.name, yppp.acgUrl, "safe"))
                    }
                } catch (_: Exception) {}
                emptyList()
            }
            else -> {
                val text = body?.string()?.trim() ?: return emptyList()
                if (text.startsWith("http")) listOf(ImageItem("text_${UUID.randomUUID()}", text, text, 0, 0, "未知", emptyList(), source.name, text, "safe"))
                else emptyList()
            }
        }
    }
}
