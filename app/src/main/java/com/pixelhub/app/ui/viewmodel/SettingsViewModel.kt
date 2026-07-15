package com.pixelhub.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.data.local.ImageQuality
import com.pixelhub.app.data.local.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val amoledMode: Boolean = false,
    val blurEnabled: Boolean = true,
    val glassEnabled: Boolean = true,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM,
    val autoSwitchQuality: Boolean = true,
    val nsfwEnabled: Boolean = false,
    val statsBrowsed: Int = 0,
    val statsFavorited: Int = 0,
    val statsDownloaded: Int = 0,
    val statsSearched: Int = 0,
    val cacheSize: String = "计算中..."
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dsm: DataStoreManager,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
        observeAmoled()
        observeBlur()
        observeGlass()
        observeImage()
        observeNsfw()
        observeStats()
        calcCache()
    }

    private fun observeTheme() {
        viewModelScope.launch { dsm.themeMode.collect { _uiState.update { s -> s.copy(themeMode = it) } } }
    }

    private fun observeAmoled() {
        viewModelScope.launch { dsm.amoledMode.collect { _uiState.update { s -> s.copy(amoledMode = it) } } }
    }

    private fun observeBlur() {
        viewModelScope.launch { dsm.blurEnabled.collect { _uiState.update { s -> s.copy(blurEnabled = it) } } }
    }

    private fun observeGlass() {
        viewModelScope.launch { dsm.glassEnabled.collect { _uiState.update { s -> s.copy(glassEnabled = it) } } }
    }

    private fun observeImage() {
        viewModelScope.launch {
            combine(dsm.imageQuality, dsm.autoSwitchQuality) { q, a -> q to a }.collect { (q, a) ->
                _uiState.update { s -> s.copy(imageQuality = q, autoSwitchQuality = a) }
            }
        }
    }

    private fun observeNsfw() {
        viewModelScope.launch { dsm.nsfwEnabled.collect { _uiState.update { s -> s.copy(nsfwEnabled = it) } } }
    }

    private fun observeStats() {
        viewModelScope.launch {
            combine(dsm.statsBrowsed, dsm.statsFavorited, dsm.statsDownloaded, dsm.statsSearched) { v ->
                @Suppress("UNCHECKED_CAST")
                arrayOf(v[0], v[1], v[2], v[3])
            }.collect { v ->
                _uiState.update {
                    it.copy(statsBrowsed = v[0] as Int, statsFavorited = v[1] as Int, statsDownloaded = v[2] as Int, statsSearched = v[3] as Int)
                }
            }
        }
    }

    private fun calcCache() {
        viewModelScope.launch {
            try {
                val bytes = ctx.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                _uiState.update { it.copy(cacheSize = formatSize(bytes)) }
            } catch (_: Exception) { _uiState.update { it.copy(cacheSize = "未知") } }
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1048576 -> "${bytes / 1024}KB"
        bytes < 1073741824 -> "${"%.1f".format(bytes / 1048576.0)}MB"
        else -> "${"%.2f".format(bytes / 1073741824.0)}GB"
    }

    fun setThemeMode(m: ThemeMode) { viewModelScope.launch { dsm.setThemeMode(m) } }
    fun setAmoledMode(enabled: Boolean) { viewModelScope.launch { dsm.setAmoledMode(enabled) } }
    fun setBlurEnabled(enabled: Boolean) { viewModelScope.launch { dsm.setBlurEnabled(enabled) } }
    fun setGlassEnabled(enabled: Boolean) { viewModelScope.launch { dsm.setGlassEnabled(enabled) } }
    fun setImageQuality(q: ImageQuality) { viewModelScope.launch { dsm.setImageQuality(q) } }
    fun setAutoSwitchQuality(e: Boolean) { viewModelScope.launch { dsm.setAutoSwitchQuality(e) } }
    fun setNsfwEnabled(e: Boolean) { viewModelScope.launch { dsm.setNsfwEnabled(e) } }
    fun clearCache() { viewModelScope.launch { ctx.cacheDir.deleteRecursively(); calcCache() } }
}
