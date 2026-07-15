package com.pixelhub.app.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelhub.app.data.local.ImageQuality
import com.pixelhub.app.data.local.ThemeMode
import com.pixelhub.app.ui.viewmodel.SettingsViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import top.yukonga.miuix.kmp.supercomponent.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 外观
            item { SectionHeader("外观") }
            item {
                SettingsItem(Icons.Default.Palette, "主题", when (state.themeMode) {
                    ThemeMode.LIGHT -> "浅色"; ThemeMode.DARK -> "深色"; ThemeMode.SYSTEM -> "跟随系统"
                }) { showThemeDialog = true }
            }
            item {
                SettingsSwitch(Icons.Default.DarkMode, "AMOLED 纯黑", "纯黑背景，省电护眼", state.amoledMode) { viewModel.setAmoledMode(it) }
            }

            // 效果
            item { SectionHeader("视觉效果") }
            item {
                SettingsSwitch(Icons.Default.BlurOn, "模糊效果", "顶栏和底栏的背景模糊", state.blurEnabled) {
                    viewModel.setBlurEnabled(it)
                    // If disabling blur, also disable glass
                    if (!it) viewModel.setGlassEnabled(false)
                }
            }
            item {
                SettingsSwitch(Icons.Default.Gradient, "液态玻璃", "折射、色散、振动效果", state.glassEnabled) {
                    viewModel.setGlassEnabled(it)
                }
            }

            // 图片
            item { SectionHeader("图片") }
            item {
                SettingsItem(Icons.Default.HighQuality, "图片质量", when (state.imageQuality) {
                    ImageQuality.THUMBNAIL -> "缩略图"; ImageQuality.MEDIUM -> "中等"; ImageQuality.ORIGINAL -> "原图"
                }) { showQualityDialog = true }
            }
            item {
                SettingsSwitch(Icons.Default.NetworkCheck, "自动切换质量", "移动网络自动降级", state.autoSwitchQuality) { viewModel.setAutoSwitchQuality(it) }
            }
            item {
                SettingsSwitch(Icons.Default.Warning, "NSFW 内容", "显示成人内容", state.nsfwEnabled) { viewModel.setNsfwEnabled(it) }
            }

            // 存储
            item { SectionHeader("存储") }
            item {
                SettingsItem(Icons.Default.CleaningServices, "清理缓存", "当前: ${state.cacheSize}") { viewModel.clearCache() }
            }

            // 调试
            item { SectionHeader("调试") }
            item {
                SettingsItem(Icons.Default.BugReport, "导出日志", "复制运行日志到剪贴板") { exportLogs(ctx) }
            }

            // 统计
            item { SectionHeader("使用统计") }
            item { SettingsItem(Icons.Default.Visibility, "已浏览", "${state.statsBrowsed} 张") {} }
            item { SettingsItem(Icons.Default.Favorite, "已收藏", "${state.statsFavorited} 张") {} }
            item { SettingsItem(Icons.Default.Download, "已下载", "${state.statsDownloaded} 张") {} }
            item { SettingsItem(Icons.Default.Search, "已搜索", "${state.statsSearched} 次") {} }

            // 关于
            item { SectionHeader("关于") }
            item { SettingsItem(Icons.Default.Info, "像素集 PixelHub", "v2.1.1 · MIT License") {} }
        }
    }

    if (showThemeDialog) {
        SingleChoiceDialog("主题", listOf(ThemeMode.LIGHT to "浅色", ThemeMode.DARK to "深色", ThemeMode.SYSTEM to "跟随系统"), state.themeMode, { showThemeDialog = false }) { viewModel.setThemeMode(it); showThemeDialog = false }
    }
    if (showQualityDialog) {
        SingleChoiceDialog("图片质量", listOf(ImageQuality.THUMBNAIL to "缩略图", ImageQuality.MEDIUM to "中等", ImageQuality.ORIGINAL to "原图"), state.imageQuality, { showQualityDialog = false }) { viewModel.setImageQuality(it); showQualityDialog = false }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp))
}

@Composable
private fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) }, modifier = Modifier.clickable(onClick = onClick))
}

@Composable
private fun SettingsSwitch(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) }, trailingContent = { Switch(checked, onCheckedChange) })
}

@Composable
private fun <T> SingleChoiceDialog(title: String, options: List<Pair<T, String>>, selected: T, onDismiss: () -> Unit, onSelect: (T) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column { options.forEach { (v, l) -> Row(Modifier.fillMaxWidth().clickable { onSelect(v) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(v == selected, { onSelect(v) }); Spacer(Modifier.width(8.dp)); Text(l) } } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

private fun exportLogs(context: Context) {
    try {
        val log = StringBuilder()
        log.appendLine("=== PixelHub Log Export ===")
        log.appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        val logDir = File(context.filesDir, "logs")
        if (logDir.exists()) {
            logDir.listFiles()?.sortedByDescending { it.lastModified() }?.take(5)?.forEach { file ->
                log.appendLine("--- ${file.name} ---")
                log.appendLine(file.readText().takeLast(5000))
            }
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("PixelHub Log", log.toString()))
        Toast.makeText(context, "日志已复制到剪贴板", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
