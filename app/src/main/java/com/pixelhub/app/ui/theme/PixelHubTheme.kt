package com.pixelhub.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.data.local.ThemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.theme.darkColorScheme

@Composable
fun PixelHubTheme(
    dataStoreManager: DataStoreManager? = null,
    content: @Composable () -> Unit
) {
    val dsm = dataStoreManager
    val themeMode by dsm?.themeMode?.collectAsState(initial = ThemeMode.DARK)
        ?: remember { mutableStateOf(ThemeMode.DARK) }
    val amoled by dsm?.amoledMode?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Miuix color scheme
    val colors = when {
        // AMOLED mode takes priority when dark
        darkTheme && amoled -> darkColorScheme().copy(
            background = Color(0xFF000000),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF0A0A0A),
            onSurfaceVariant = Color(0xFFCAC4D0),
        )
        // Dynamic colors on Android 12+
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !amoled -> {
            val ctx = LocalContext.current
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MiuixTheme(colors = colors, content = content)
}
