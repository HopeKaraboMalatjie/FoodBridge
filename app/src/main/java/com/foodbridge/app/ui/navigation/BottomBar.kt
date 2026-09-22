package com.foodbridge.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    BottomTab(Routes.HOME_FEED, "Feed", Icons.Filled.Home),
    BottomTab(Routes.MAP_VIEW, "Map", Icons.Filled.Map),
    BottomTab(Routes.POST_LISTING, "Post", Icons.Filled.AddCircle),
    BottomTab(Routes.IMPACT_PROFILE, "Impact", Icons.Filled.Person)
)

@Composable
fun FoodBridgeBottomBar(currentRoute: String?, onTabSelected: (String) -> Unit) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}
