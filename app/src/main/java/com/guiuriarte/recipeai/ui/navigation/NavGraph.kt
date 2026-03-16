package com.guiuriarte.recipeai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.guiuriarte.recipeai.ui.screens.fridge.FridgeModeScreen
import com.guiuriarte.recipeai.ui.screens.home.HomeScreen
import com.guiuriarte.recipeai.ui.screens.recipe.CookingModeScreen
import com.guiuriarte.recipeai.ui.screens.recipe.RecipeDetailScreen
import com.guiuriarte.recipeai.ui.screens.saved.SavedRecipesScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            arguments = listOf(navArgument("ingredients") {
                type = NavType.StringType; defaultValue = ""
            })
        ) { backStackEntry ->
            val ingredients = backStackEntry.arguments?.getString("ingredients")
                ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            HomeScreen(
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onNavigateToFridge = {
                    navController.navigate(Screen.Fridge.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                initialIngredients = ingredients
            )
        }

        composable(Screen.Fridge.route) {
            FridgeModeScreen(
                onNavigateToHome = { ingredients ->
                    val ingredientsArg = ingredients.joinToString(",")
                    navController.navigate(Screen.Home.createRoute(ingredientsArg)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SavedRecipes.route) {
            SavedRecipesScreen(
                onNavigateToDetail = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateToCooking = { id ->
                    navController.navigate(Screen.CookingMode.createRoute(id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CookingMode.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            CookingModeScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
