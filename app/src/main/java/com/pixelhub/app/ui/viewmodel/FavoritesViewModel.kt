package com.pixelhub.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelhub.app.data.repository.FavoritesRepository
import com.pixelhub.app.data.local.entity.GroupEntity
import com.pixelhub.app.domain.model.ImageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<ImageItem> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val selectedGroupId: String? = null,
    val isBatchMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val selectedImage: ImageItem? = null,
    val showCreateGroupDialog: Boolean = false,
    val showMoveToGroupDialog: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var favoritesJob: Job? = null

    init {
        loadGroups()
        loadFavorites()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            favoritesRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    private fun loadFavorites() {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            val groupId = _uiState.value.selectedGroupId
            val flow = when {
                groupId == null -> favoritesRepository.getAllFavorites()
                groupId == "ungrouped" -> favoritesRepository.getUngroupedFavorites()
                else -> favoritesRepository.getFavoritesByGroup(groupId)
            }
            flow.collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    fun selectGroup(groupId: String?) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
        loadFavorites()
    }

    fun toggleBatchMode() {
        _uiState.update { it.copy(isBatchMode = !it.isBatchMode, selectedIds = emptySet()) }
    }

    fun toggleSelection(id: String) {
        _uiState.update { s -> s.copy(selectedIds = if (id in s.selectedIds) s.selectedIds - id else s.selectedIds + id) }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedIds = it.favorites.map { f -> f.id }.toSet()) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun batchRemove() {
        viewModelScope.launch {
            favoritesRepository.batchRemoveFromFavorites(_uiState.value.selectedIds.toList())
            _uiState.update { it.copy(isBatchMode = false, selectedIds = emptySet()) }
        }
    }

    fun batchMoveToGroup(groupId: String?) {
        viewModelScope.launch {
            favoritesRepository.batchMoveToGroup(_uiState.value.selectedIds.toList(), groupId)
            _uiState.update { it.copy(isBatchMode = false, selectedIds = emptySet(), showMoveToGroupDialog = false) }
        }
    }

    fun showCreateGroupDialog() { _uiState.update { it.copy(showCreateGroupDialog = true) } }
    fun hideCreateGroupDialog() { _uiState.update { it.copy(showCreateGroupDialog = false) } }
    fun createGroup(name: String) {
        viewModelScope.launch {
            favoritesRepository.createGroup(name)
            _uiState.update { it.copy(showCreateGroupDialog = false) }
        }
    }
    fun showMoveToGroupDialog() { _uiState.update { it.copy(showMoveToGroupDialog = true) } }
    fun hideMoveToGroupDialog() { _uiState.update { it.copy(showMoveToGroupDialog = false) } }

    fun removeFavorite(image: ImageItem) {
        viewModelScope.launch { favoritesRepository.removeFromFavorites(image.id) }
    }

    fun selectImage(image: ImageItem?) { _uiState.update { it.copy(selectedImage = image) } }
}
