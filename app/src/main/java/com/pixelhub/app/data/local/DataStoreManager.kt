package com.pixelhub.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixelhub_prefs")

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class ImageQuality { THUMBNAIL, MEDIUM, ORIGINAL }

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Theme
    private val THEME_KEY = stringPreferencesKey("theme_mode")
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name)
    }
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.name }
    }

    // NSFW
    private val NSFW_KEY = booleanPreferencesKey("nsfw_enabled")
    val nsfwEnabled: Flow<Boolean> = dataStore.data.map { it[NSFW_KEY] ?: false }
    suspend fun setNsfwEnabled(enabled: Boolean) {
        dataStore.edit { it[NSFW_KEY] = enabled }
    }

    // Image Quality
    private val QUALITY_KEY = stringPreferencesKey("image_quality")
    val imageQuality: Flow<ImageQuality> = dataStore.data.map { prefs ->
        ImageQuality.valueOf(prefs[QUALITY_KEY] ?: ImageQuality.MEDIUM.name)
    }
    suspend fun setImageQuality(quality: ImageQuality) {
        dataStore.edit { it[QUALITY_KEY] = quality.name }
    }

    // Auto switch quality on mobile network
    private val AUTO_SWITCH_KEY = booleanPreferencesKey("auto_switch_quality")
    val autoSwitchQuality: Flow<Boolean> = dataStore.data.map { it[AUTO_SWITCH_KEY] ?: true }
    suspend fun setAutoSwitchQuality(enabled: Boolean) {
        dataStore.edit { it[AUTO_SWITCH_KEY] = enabled }
    }

    // Current active source ID
    private val ACTIVE_SOURCE_KEY = stringPreferencesKey("active_source_id")
    val activeSourceId: Flow<String> = dataStore.data.map { it[ACTIVE_SOURCE_KEY] ?: "lolicon" }
    suspend fun setActiveSourceId(id: String) {
        dataStore.edit { it[ACTIVE_SOURCE_KEY] = id }
    }

    // Custom API sources (JSON serialized)
    private val CUSTOM_SOURCES_KEY = stringPreferencesKey("custom_sources")
    val customSourcesJson: Flow<String> = dataStore.data.map { it[CUSTOM_SOURCES_KEY] ?: "[]" }
    suspend fun setCustomSourcesJson(json: String) {
        dataStore.edit { it[CUSTOM_SOURCES_KEY] = json }
    }

    // WebDAV settings
    private val WEBDAV_URL_KEY = stringPreferencesKey("webdav_url")
    private val WEBDAV_USER_KEY = stringPreferencesKey("webdav_user")
    private val WEBDAV_PASS_KEY = stringPreferencesKey("webdav_pass")

    val webdavUrl: Flow<String> = dataStore.data.map { it[WEBDAV_URL_KEY] ?: "" }
    val webdavUser: Flow<String> = dataStore.data.map { it[WEBDAV_USER_KEY] ?: "" }
    val webdavPass: Flow<String> = dataStore.data.map { it[WEBDAV_PASS_KEY] ?: "" }

    suspend fun setWebdavConfig(url: String, user: String, pass: String) {
        dataStore.edit {
            it[WEBDAV_URL_KEY] = url
            it[WEBDAV_USER_KEY] = user
            it[WEBDAV_PASS_KEY] = pass
        }
    }

    // AMOLED pure black mode
    private val AMOLED_KEY = booleanPreferencesKey("amoled_mode")
    val amoledMode: Flow<Boolean> = dataStore.data.map { it[AMOLED_KEY] ?: false }
    suspend fun setAmoledMode(enabled: Boolean) {
        dataStore.edit { it[AMOLED_KEY] = enabled }
    }

    // Blur effect toggle
    private val BLUR_KEY = booleanPreferencesKey("blur_enabled")
    val blurEnabled: Flow<Boolean> = dataStore.data.map { it[BLUR_KEY] ?: true }
    suspend fun setBlurEnabled(enabled: Boolean) {
        dataStore.edit { it[BLUR_KEY] = enabled }
    }

    // Liquid Glass effect toggle
    private val GLASS_KEY = booleanPreferencesKey("glass_enabled")
    val glassEnabled: Flow<Boolean> = dataStore.data.map { it[GLASS_KEY] ?: true }
    suspend fun setGlassEnabled(enabled: Boolean) {
        dataStore.edit { it[GLASS_KEY] = enabled }
        // When glass is enabled, force blur on
        if (enabled) {
            dataStore.edit { it[BLUR_KEY] = true }
        }
    }

    // Stats
    private val STATS_BROWSED_KEY = intPreferencesKey("stats_browsed")
    private val STATS_FAVORITED_KEY = intPreferencesKey("stats_favorited")
    private val STATS_DOWNLOADED_KEY = intPreferencesKey("stats_downloaded")
    private val STATS_SEARCHED_KEY = intPreferencesKey("stats_searched")

    val statsBrowsed: Flow<Int> = dataStore.data.map { it[STATS_BROWSED_KEY] ?: 0 }
    val statsFavorited: Flow<Int> = dataStore.data.map { it[STATS_FAVORITED_KEY] ?: 0 }
    val statsDownloaded: Flow<Int> = dataStore.data.map { it[STATS_DOWNLOADED_KEY] ?: 0 }
    val statsSearched: Flow<Int> = dataStore.data.map { it[STATS_SEARCHED_KEY] ?: 0 }

    suspend fun incrementStat(key: String) {
        dataStore.edit { prefs ->
            val intKey = when (key) {
                "browsed" -> STATS_BROWSED_KEY
                "favorited" -> STATS_FAVORITED_KEY
                "downloaded" -> STATS_DOWNLOADED_KEY
                "searched" -> STATS_SEARCHED_KEY
                else -> return@edit
            }
            prefs[intKey] = (prefs[intKey] ?: 0) + 1
        }
    }
}
