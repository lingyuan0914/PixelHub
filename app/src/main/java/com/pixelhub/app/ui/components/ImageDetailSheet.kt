package com.pixelhub.app.ui.components

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pixelhub.app.domain.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import top.yukonga.miuix.kmp.supercomponent.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailSheet(
    image: ImageItem,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onSetWallpaper: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val imageLoader = remember { Coil.imageLoader(context) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(image.id) { scale = 1f; offset = Offset.Zero; loadedBitmap = null }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        contentColor = Color.White,
        dragHandle = null
    ) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭", tint = Color.White) }
                Text(image.author, style = MaterialTheme.typography.titleMedium, color = Color.White)
                IconButton(onClick = onFavoriteToggle) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (isFavorite) "取消收藏" else "收藏", tint = if (isFavorite) Color.Red else Color.White)
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offset = if (scale <= 1f) Offset.Zero else offset + pan
                }
            }, contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(image.url).crossfade(true)
                        .listener(onSuccess = { _, result ->
                            // Capture the actual bitmap from the drawable
                            val drawable = result.drawable
                            if (drawable is BitmapDrawable) {
                                loadedBitmap = drawable.bitmap
                            }
                        })
                        .build(),
                    contentDescription = image.author, contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }
                )
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), Arrangement.SpaceEvenly) {
                ActionButton(Icons.Default.Favorite, "收藏", if (isFavorite) Color.Red else Color.White) { onFavoriteToggle() }
                ActionButton(Icons.Default.Download, "下载") {
                    scope.launch {
                        Toast.makeText(context, "正在下载...", Toast.LENGTH_SHORT).show()
                        val success = saveBitmapToGallery(context, loadedBitmap, image.url)
                        Toast.makeText(context, if (success) "已保存到相册" else "下载失败", Toast.LENGTH_SHORT).show()
                    }
                }
                ActionButton(Icons.Default.Share, "分享") {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${image.author} - ${image.url}")
                    }, "分享图片"))
                }
                ActionButton(Icons.Default.Wallpaper, "壁纸") {
                    scope.launch {
                        Toast.makeText(context, "正在设置壁纸...", Toast.LENGTH_SHORT).show()
                        val success = setWallpaper(context, loadedBitmap, image.url)
                        Toast.makeText(context, if (success) "壁纸设置成功" else "壁纸设置失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, label, tint = tint, modifier = Modifier.size(28.dp)) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

private suspend fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap?, url: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val bmp = bitmap ?: downloadBitmap(url) ?: return@withContext false
            val filename = "PixelHub_${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PixelHub")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let {
                    context.contentResolver.openOutputStream(it)?.use { os -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, os) }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PixelHub")
                dir.mkdirs()
                FileOutputStream(File(dir, filename)).use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            }
            true
        } catch (e: Exception) { Timber.e(e, "Save failed"); false }
    }
}

private suspend fun setWallpaper(context: android.content.Context, bitmap: Bitmap?, url: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val bmp = bitmap ?: downloadBitmap(url) ?: return@withContext false
            android.app.WallpaperManager.getInstance(context).setBitmap(bmp)
            true
        } catch (e: Exception) { Timber.e(e, "Wallpaper failed"); false }
    }
}

private fun downloadBitmap(url: String): Bitmap? {
    return try {
        val conn = URL(url).openConnection().apply { connectTimeout = 15000; readTimeout = 15000 }
        android.graphics.BitmapFactory.decodeStream(conn.getInputStream())
    } catch (e: Exception) { Timber.e(e, "Download failed"); null }
}
