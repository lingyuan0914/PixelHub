package com.pixelhub.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.data.repository.*
import com.pixelhub.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ExploreUiState(
    val images: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentSource: ImageSource? = null,
    val availableSources: List<ImageSource> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedImage: ImageItem? = null,
    val showAddApiDialog: Boolean = false,
    val hasMore: Boolean = true
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repo: ExploreRepository,
    private val favRepo: FavoritesRepository,
    private val dsm: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        loadSources()
        loadImages(refresh = true)
        observeFavorites()
    }

    private fun loadSources() {
        viewModelScope.launch {
            repo.getAvailableSources().collect { sources ->
                val currentSource = repo.getActiveSource()
                _state.update { it.copy(availableSources = sources, currentSource = currentSource) }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favRepo.getAllFavorites().collect { f -> _state.update { it.copy(favoriteIds = f.map { i -> i.id }.toSet()) } }
        }
    }

    fun loadImages(refresh: Boolean = false) {
        if (_state.value.isLoading || _state.value.isLoadingMore) return
        if (!refresh && !_state.value.hasMore) return

        viewModelScope.launch {
            _state.update { if (refresh) it.copy(isLoading = true, error = null, hasMore = true) else it.copy(isLoadingMore = true) }
            val source = _state.value.currentSource ?: repo.getActiveSource()
            Timber.i("Loading images from: ${source.name}, refresh=$refresh")

            try {
                val newImages = repo.loadImages(_state.value.searchQuery.takeIf { q -> q.isNotBlank() })
                Timber.i("Loaded ${newImages.size} images from ${source.name}")

                _state.update {
                    it.copy(
                        images = if (refresh) newImages else it.images + newImages,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = newImages.isNotEmpty()
                    )
                }
                dsm.incrementStat("browsed")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load images")
                _state.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun loadMore() {
        loadImages(refresh = false)
    }

    fun search(q: String) {
        _state.update { it.copy(searchQuery = q) }
        viewModelScope.launch { dsm.incrementStat("searched") }
        loadImages(refresh = true)
    }

    fun switchSource(s: ImageSource) {
        Timber.i("Switching source to: ${s.name}")
        viewModelScope.launch {
            dsm.setActiveSourceId(s.id)
            _state.update { it.copy(currentSource = s) }
            loadImages(refresh = true)
        }
    }

    fun toggleFavorite(img: ImageItem) {
        viewModelScope.launch { favRepo.toggleFavorite(img); dsm.incrementStat("favorited") }
    }

    fun selectImage(img: ImageItem?) { _state.update { it.copy(selectedImage = img) } }

    fun showAddApiDialog() { _state.update { it.copy(showAddApiDialog = true) } }
    fun hideAddApiDialog() { _state.update { it.copy(showAddApiDialog = false) } }

    fun addCustomApi(name: String, url: String, type: ApiType) {
        viewModelScope.launch {
            repo.addCustomSource(name, url, type)
            _state.update { it.copy(showAddApiDialog = false) }
            loadSources()
        }
    }

    fun removeCustomApi(id: String) {
        viewModelScope.launch {
            repo.removeCustomSource(id)
            loadSources()
        }
    }
}
