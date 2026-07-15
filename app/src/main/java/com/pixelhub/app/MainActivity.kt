package com.pixelhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.pixelhub.app.data.local.DataStoreManager
import com.pixelhub.app.ui.screens.explore.ExploreScreen
import com.pixelhub.app.ui.screens.favorites.FavoritesScreen
import com.pixelhub.app.ui.screens.settings.SettingsScreen
import com.pixelhub.app.ui.theme.PixelHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import top.yukonga.miuix.kmp.supercomponent.Scaffold
import top.yukonga.miuix.kmp.supercomponent.TopAppBar
import top.yukonga.miuix.kmp.supercomponent.NavigationBar
import top.yukonga.miuix.kmp.supercomponent.NavigationBarItem
import top.yukonga.miuix.kmp.supercomponent.FloatingActionButton
import top.yukonga.miuix.kmp.supercomponent.Icon
import top.yukonga.miuix.kmp.supercomponent.Text
import top.yukonga.miuix.kmp.supercomponent.Surface
import top.yukonga.miuix.kmp.supercomponent.Box
import top.yukonga.miuix.kmp.supercomponent.Row
import top.yukonga.miuix.kmp.supercomponent.Column
import top.yukonga.miuix.kmp.supercomponent.Modifier
import top.yukonga.miuix.kmp.supercomponent.MaterialTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelHubTheme(dataStoreManager = dataStoreManager) {
                PixelHubMainContent(dataStoreManager)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Explore : Screen("explore", "首页", Icons.Outlined.Home, Icons.Filled.Home)
    data object Favorites : Screen("favorites", "收藏", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite)
    data object Settings : Screen("settings", "设置", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun PixelHubMainContent(dsm: DataStoreManager) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Explore, Screen.Favorites, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Read effect settings
    val blurEnabled by dsm.blurEnabled.collectAsState(initial = true)
    val glassEnabled by dsm.glassEnabled.collectAsState(initial = true)

    val bgColor = MaterialTheme.colorScheme.background

    val backdrop = rememberLayerBackdrop {
        drawContent()
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController, Screen.Explore.route,
            Modifier.fillMaxSize().layerBackdrop(backdrop)
        ) {
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }

        // Bottom bar with liquid glass / blur / solid
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (glassEnabled && blurEnabled) {
                // Liquid Glass mode
                Box(
                    Modifier.fillMaxWidth().height(56.dp).drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(28.dp) },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(20f.dp.toPx(), 40f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.2f)) }
                    )
                ) { BottomBarContent(screens, currentRoute, navController) }
            } else if (blurEnabled) {
                // Blur only (no glass)
                Box(
                    Modifier.fillMaxWidth().height(56.dp).drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(28.dp) },
                        effects = { blur(12f.dp.toPx()) },
                        onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.3f)) }
                    )
                ) { BottomBarContent(screens, currentRoute, navController) }
            } else {
                // Solid color fallback
                Surface(
                    Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shadowElevation = 8.dp
                ) { BottomBarContent(screens, currentRoute, navController) }
            }
        }

        // Floating refresh button with liquid glass
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 72.dp)) {
            if (glassEnabled && blurEnabled) {
                Box(
                    Modifier.size(48.dp).drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = { vibrancy(); blur(6f.dp.toPx()); lens(16f.dp.toPx(), 32f.dp.toPx()) },
                        onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.2f)) }
                    ).clickable { /* TODO: trigger refresh via shared state */ }
                ) {
                    Icon(Icons.Default.Refresh, "刷新", tint = Color.White, modifier = Modifier.align(Alignment.Center).size(24.dp))
                }
            } else if (blurEnabled) {
                Box(
                    Modifier.size(48.dp).drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = { blur(8f.dp.toPx()) },
                        onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.4f)) }
                    ).clickable { /* TODO */ }
                ) {
                    Icon(Icons.Default.Refresh, "刷新", tint = Color.White, modifier = Modifier.align(Alignment.Center).size(24.dp))
                }
            } else {
                FloatingActionButton(onClick = { /* TODO */ }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Refresh, "刷新")
                }
            }
        }
    }
}

@Composable
private fun BottomBarContent(
    screens: List<Screen>,
    currentRoute: String?,
    navController: androidx.navigation.NavHostController
) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        screens.forEach { screen ->
            val selected = currentRoute == screen.route
            IconButton(onClick = {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selected) screen.selectedIcon else screen.icon,
                        screen.title,
                        modifier = Modifier.size(22.dp),
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
