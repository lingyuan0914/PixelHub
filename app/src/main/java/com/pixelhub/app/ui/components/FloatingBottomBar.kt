package com.pixelhub.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

data class FloatingBarItem(
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String,
    val route: String
)

@Composable
fun FloatingBottomBar(
    items: List<FloatingBarItem>,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        if (backdrop != null) {
            // Liquid Glass effect
            Box(
                Modifier
                    .fillMaxWidth()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(50) },
                        effects = {
                            vibrancy()
                            blur(4f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        onDrawSurface = {
                            drawRect(Color.Black.copy(alpha = 0.35f))
                        }
                    )
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        BarItem(
                            icon = if (currentRoute == item.route) item.selectedIcon else item.icon,
                            label = item.label,
                            selected = currentRoute == item.route,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item.route)
                            }
                        )
                    }
                }
            }
        } else {
            // Fallback: frosted glass
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                color = Color(0xFF1A1A2E).copy(alpha = 0.75f),
                shadowElevation = 16.dp
            ) {
                Box(
                    Modifier.fillMaxWidth().background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                        )
                    ).padding(vertical = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            BarItem(
                                icon = if (currentRoute == item.route) item.selectedIcon else item.icon,
                                label = item.label,
                                selected = currentRoute == item.route,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onItemSelected(item.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "s")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale).clickable(interaction, null, onClick = onClick).padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Icon(icon, label, tint = if (selected) Color(0xFFD0BCFF) else Color(0xFF5A5A7A), modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, color = if (selected) Color(0xFFD0BCFF) else Color(0xFF5A5A7A))
    }
}
