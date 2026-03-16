package com.guiuriarte.recipeai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.guiuriarte.recipeai.ui.navigation.NavGraph
import com.guiuriarte.recipeai.ui.navigation.Screen
import com.guiuriarte.recipeai.ui.theme.RecipeAITheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Favorite

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeAITheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("Início", Icons.Default.Home, Screen.Home),
        BottomNavItem("Geladeira", Icons.Default.Kitchen, Screen.Fridge),
        BottomNavItem("Salvas", Icons.Default.Favorite, Screen.SavedRecipes),
    )

    val showBottomBar = currentRoute?.startsWith("home") == true ||
            currentRoute == Screen.Fridge.route ||
            currentRoute == Screen.SavedRecipes.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = when (item.screen) {
                                Screen.Home -> currentRoute?.startsWith("home") == true
                                else -> currentRoute == item.screen.route
                            },
                            onClick = {
                                val route = if (item.screen == Screen.Home) Screen.Home.createRoute() else item.screen.route
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}
