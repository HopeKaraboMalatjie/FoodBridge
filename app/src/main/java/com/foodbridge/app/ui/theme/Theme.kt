package com.foodbridge.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FoodBridgeGreen = Color(0xFF2E7D32)
private val FoodBridgeGreenDark = Color(0xFF66BB6A)
private val FoodBridgeOrange = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = FoodBridgeGreen,
    secondary = FoodBridgeOrange
)

private val DarkColors = darkColorScheme(
    primary = FoodBridgeGreenDark,
    secondary = FoodBridgeOrange
)

@Composable
fun FoodBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
