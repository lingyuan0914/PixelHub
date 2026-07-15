package com.pixelhub.app.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelhub.app.ui.components.ImageDetailSheet
import com.pixelhub.app.ui.components.ImageWaterfallGrid
import com.pixelhub.app.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("收藏") },
                actions = {
                    if (uiState.isBatchMode) {
                        IconButton(onClick = { viewModel.selectAll() }) { Icon(Icons.Default.SelectAll, "全选") }
                        IconButton(onClick = { viewModel.deselectAll() }) { Icon(Icons.Default.Deselect, "取消全选") }
                        IconButton(onClick = { viewModel.showMoveToGroupDialog() }) { Icon(Icons.Default.FolderOpen, "移动") }
                        IconButton(onClick = { viewModel.batchRemove() }) { Icon(Icons.Default.Delete, "删除") }
                        IconButton(onClick = { viewModel.toggleBatchMode() }) { Icon(Icons.Default.Close, "退出") }
                    } else {
                        IconButton(onClick = { viewModel.toggleBatchMode() }) { Icon(Icons.Default.Checklist, "批量") }
                        IconButton(onClick = { viewModel.showCreateGroupDialog() }) { Icon(Icons.Default.CreateNewFolder, "新建分组") }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Group tabs
            ScrollableTabRow(
                selectedTabIndex = getTabIndex(uiState.selectedGroupId),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                Tab(uiState.selectedGroupId == null, { viewModel.selectGroup(null) }, text = { Text("全部") })
                Tab(uiState.selectedGroupId == "ungrouped", { viewModel.selectGroup("ungrouped") }, text = { Text("未分组") })
                uiState.groups.forEach { g ->
                    Tab(uiState.selectedGroupId == g.id, { viewModel.selectGroup(g.id) }, text = { Text(g.name) })
                }
            }

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.favorites.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("还没有收藏", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("去首页收藏喜欢的图片吧", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    ImageWaterfallGrid(
                        images = uiState.favorites,
                        favoriteIds = uiState.favorites.map { it.id }.toSet(),
                        onImageClick = { viewModel.selectImage(it) },
                        onFavoriteClick = { viewModel.removeFavorite(it) }
                    )
                }
            }
        }
    }

    // Detail sheet
    uiState.selectedImage?.let { image ->
        ImageDetailSheet(image, true, { viewModel.selectImage(null) }, { viewModel.removeFavorite(image) }, {}, {}, {})
    }

    // Create group dialog
    if (uiState.showCreateGroupDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateGroupDialog() },
            title = { Text("创建分组") },
            text = { OutlinedTextField(name, { name = it }, label = { Text("分组名称") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { viewModel.createGroup(name) }, enabled = name.isNotBlank()) { Text("创建") } },
            dismissButton = { TextButton(onClick = { viewModel.hideCreateGroupDialog() }) { Text("取消") } }
        )
    }

    // Move to group dialog
    if (uiState.showMoveToGroupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideMoveToGroupDialog() },
            title = { Text("移动到分组") },
            text = {
                Column {
                    TextButton(onClick = { viewModel.batchMoveToGroup(null) }, modifier = Modifier.fillMaxWidth()) { Text("移出分组") }
                    uiState.groups.forEach { g ->
                        TextButton(onClick = { viewModel.batchMoveToGroup(g.id) }, modifier = Modifier.fillMaxWidth()) { Text(g.name) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { viewModel.hideMoveToGroupDialog() }) { Text("取消") } }
        )
    }
}

private fun getTabIndex(selectedGroupId: String?) = when (selectedGroupId) { null -> 0; "ungrouped" -> 1; else -> 2 }
