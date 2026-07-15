package com.pixelhub.app.ui.screens.explore

import androidx.compose.foundation.layout.*
import top.yukonga.miuix.kmp.supercomponent.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelhub.app.domain.model.ApiType
import com.pixelhub.app.ui.components.ImageCard
import com.pixelhub.app.ui.components.ImageDetailSheet
import com.pixelhub.app.ui.viewmodel.ExploreViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(viewModel: ExploreViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyStaggeredGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showSourceMenu by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }

    val recommendedTags = listOf(
        "原神", "崩坏星穹铁道", "明日方舟", "蔚蓝档案", "碧蓝航线",
        "初音未来", "VOCALOID", "Fate", "东方Project", "赛马娘",
        "少女", "长发", "兽耳", "和服", "制服",
        "风景", "夜景", "星空", "樱花", "猫耳"
    )

    // Infinite scroll
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
            last != null && last.index >= gridState.layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoading && !uiState.isLoadingMore && uiState.hasMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("像素集") },
                actions = {
                    Box {
                        TextButton(onClick = { showSourceMenu = true }) {
                            Text(uiState.currentSource?.name ?: "选择源")
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(showSourceMenu, { showSourceMenu = false }) {
                            uiState.availableSources.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = { viewModel.switchSource(s); showSourceMenu = false },
                                    trailingIcon = {
                                        if (!s.isBuiltIn) {
                                            IconButton(onClick = { viewModel.removeCustomApi(s.id); showSourceMenu = false }, Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, "删除", Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("添加自定义 API") },
                                onClick = { viewModel.showAddApiDialog(); showSourceMenu = false },
                                leadingIcon = { Icon(Icons.Default.Add, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalItemSpacing = 6.dp
        ) {
            // Search bar + Recommend button
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = { showTagSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, "推荐标签")
                    }
                }
            }

            // Loading
            if (uiState.isLoading && uiState.images.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
            }

            // Error
            if (uiState.error != null && uiState.images.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error ?: "未知错误")
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadImages(refresh = true) }) { Text("重试") }
                        }
                    }
                }
            }

            // Empty
            if (!uiState.isLoading && uiState.error == null && uiState.images.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("暂无图片", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Images
            items(items = uiState.images, key = { it.id }) { image ->
                ImageCard(
                    image = image,
                    isFavorite = uiState.favoriteIds.contains(image.id),
                    onImageClick = { viewModel.selectImage(image) },
                    onFavoriteClick = { viewModel.toggleFavorite(image) }
                )
            }

            // Loading more
            if (uiState.isLoadingMore) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }

    // Tag recommendation sheet
    if (showTagSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTagSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("推荐标签", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recommendedTags.forEach { tag ->
                        FilterChip(
                            selected = uiState.searchQuery.contains(tag),
                            onClick = {
                                viewModel.search(tag)
                                showTagSheet = false
                            },
                            label = { Text(tag) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Detail sheet
    uiState.selectedImage?.let { image ->
        ImageDetailSheet(
            image = image,
            isFavorite = uiState.favoriteIds.contains(image.id),
            onDismiss = { viewModel.selectImage(null) },
            onFavoriteToggle = { viewModel.toggleFavorite(image) },
            onDownload = {},
            onShare = {},
            onSetWallpaper = {}
        )
    }

    // Add API dialog
    if (uiState.showAddApiDialog) {
        AddApiDialog(
            onDismiss = { viewModel.hideAddApiDialog() },
            onAdd = { name, url, type -> viewModel.addCustomApi(name, url, type) }
        )
    }
}

@Composable
private fun AddApiDialog(onDismiss: () -> Unit, onAdd: (String, String, ApiType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ApiType.DIRECT_IMAGE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加自定义 API") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(url, { url = it }, label = { Text("API 地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("类型", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ApiType.DIRECT_IMAGE to "直连", ApiType.LOLICON to "Lolicon", ApiType.YPPP to "YPPP", ApiType.AUTO_DETECT to "自动").forEach { (type, label) ->
                        FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(label) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, url, selectedType) }, enabled = name.isNotBlank() && url.isNotBlank()) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
