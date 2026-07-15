package com.pixelhub.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.data.local.ThemeMode

// AMOLED pure black color scheme
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1A1A2E), onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC), onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1A1A2E), onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8), onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1A1A2E), onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF000000), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF000000), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF0A0A0A), onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFF000000),
    outline = Color(0xFF2A2A2A), outlineVariant = Color(0xFF1A1A1A),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B), onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC), onSecondary = Color(0xFF332D41),
    background = Color(0xFF0E0E1A), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF0E0E1A), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF1E1E2E), onSurfaceVariant = Color(0xFFCAC4D0),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF), onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71), onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
)

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

    val colorScheme = when {
        // AMOLED mode takes priority when dark
        darkTheme && amoled -> AmoledColorScheme
        // Dynamic colors on Android 12+
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !amoled -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}
