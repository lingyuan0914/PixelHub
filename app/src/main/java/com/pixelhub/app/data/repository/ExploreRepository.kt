package com.pixelhub.app.data.repository

import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.data.remote.ApiAdapter
import com.pixelhub.app.data.remote.model.ApiSourceConfig
import com.pixelhub.app.domain.model.ApiType
import com.pixelhub.app.domain.model.BuiltInApis
import com.pixelhub.app.domain.model.ImageItem
import com.pixelhub.app.domain.model.ImageSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val apiAdapter: ApiAdapter,
    private val dataStoreManager: DataStoreManager,
    private val gson: Gson
) {
    // Server base URL - change this to your server IP
    private val serverUrl = "http://160.202.248.48:8899"

    fun getAvailableSources(): Flow<List<ImageSource>> = flow {
        val customSources = loadCustomSources()
        emit(BuiltInApis.sources + customSources.map { it.toDomain() })
    }

    suspend fun getActiveSource(): ImageSource {
        val sourceId = dataStoreManager.activeSourceId.first()
        val allSources = BuiltInApis.sources + loadCustomSources().map { it.toDomain() }
        return allSources.find { it.id == sourceId } ?: BuiltInApis.sources.first()
    }

    suspend fun loadImages(tag: String? = null): List<ImageItem> {
        val source = getActiveSource()
        val r18 = dataStoreManager.nsfwEnabled.first()

        return try {
            // Try server first
            val serverImages = apiAdapter.fetchFromServer(serverUrl, source, tag)
            if (serverImages.isNotEmpty()) {
                Timber.d("Loaded ${serverImages.size} images from server")
                serverImages
            } else {
                // Fallback to direct API
                Timber.d("Server empty, falling back to direct API")
                apiAdapter.fetchImages(source, tag, r18)
            }
        } catch (e: Exception) {
            Timber.e(e, "Server failed, falling back to direct API")
            apiAdapter.fetchImages(source, tag, r18)
        }
    }

    suspend fun toggleFavorite(image: ImageItem) {
        try {
            apiAdapter.toggleFavoriteOnServer(serverUrl, image)
        } catch (e: Exception) {
            Timber.e(e, "Server favorite toggle failed")
        }
    }

    suspend fun addCustomSource(name: String, url: String, type: ApiType) {
        val customList = loadCustomSources().toMutableList()
        customList.add(ApiSourceConfig("custom_${System.currentTimeMillis()}", name, url, type.name))
        dataStoreManager.setCustomSourcesJson(gson.toJson(customList))
    }

    suspend fun removeCustomSource(id: String) {
        val customList = loadCustomSources().toMutableList()
        customList.removeAll { it.id == id }
        dataStoreManager.setCustomSourcesJson(gson.toJson(customList))
    }

    private suspend fun loadCustomSources(): List<ApiSourceConfig> {
        val json = dataStoreManager.customSourcesJson.first()
        return try {
            gson.fromJson(json, object : TypeToken<List<ApiSourceConfig>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    private fun ApiSourceConfig.toDomain() = ImageSource(id, name, url, ApiType.valueOf(type), enabled, rateLimit)
}
