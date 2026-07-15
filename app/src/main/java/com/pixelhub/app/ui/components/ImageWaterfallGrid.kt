package com.pixelhub.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.pixelhub.app.domain.model.ImageItem

@Composable
fun ImageWaterfallGrid(
    images: List<ImageItem>,
    favoriteIds: Set<String>,
    onImageClick: (ImageItem) -> Unit,
    onFavoriteClick: (ImageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columns = when {
        screenWidth < 400.dp -> 2
        screenWidth < 600.dp -> 3
        else -> 4
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(
            items = images,
            key = { it.id }
        ) { image ->
            ImageCard(
                image = image,
                isFavorite = favoriteIds.contains(image.id),
                onImageClick = { onImageClick(image) },
                onFavoriteClick = { onFavoriteClick(image) }
            )
        }
    }
}
