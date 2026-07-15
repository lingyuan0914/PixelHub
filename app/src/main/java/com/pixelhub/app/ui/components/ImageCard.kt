package com.pixelhub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pixelhub.app.domain.model.ImageItem
import timber.log.Timber
import top.yukonga.miuix.kmp.supercomponent.*

@Composable
fun ImageCard(
    image: ImageItem,
    isFavorite: Boolean,
    onImageClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember { Coil.imageLoader(context) }

    val aspectRatio = if (image.width > 0 && image.height > 0) {
        image.width.toFloat() / image.height.toFloat()
    } else {
        0.75f
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onImageClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            var isLoading by remember { mutableStateOf(true) }
            var hasError by remember { mutableStateOf(false) }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(image.thumbnailUrl)
                    .crossfade(true)
                    .listener(
                        onStart = { Timber.d("Coil START: ${image.thumbnailUrl.take(100)}") },
                        onSuccess = { _, result ->
                            Timber.d("Coil OK: ${image.thumbnailUrl.take(100)} (${result.dataSource})")
                            isLoading = false
                        },
                        onError = { _, error ->
                            Timber.e(error.throwable, "Coil FAIL: ${image.thumbnailUrl.take(100)}")
                            isLoading = false
                            hasError = true
                        }
                    )
                    .build(),
                imageLoader = imageLoader,
                contentDescription = image.author,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio).clip(RoundedCornerShape(12.dp))
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                )
            }

            if (hasError) {
                Text("加载失败", style = MaterialTheme.typography.labelSmall, color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center))
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(36.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
