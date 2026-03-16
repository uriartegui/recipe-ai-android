package com.guiuriarte.recipeai.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.guiuriarte.recipeai.model.Recipe
import com.guiuriarte.recipeai.ui.theme.BadgeGreen
import com.guiuriarte.recipeai.ui.theme.BrandOrange
import com.guiuriarte.recipeai.ui.theme.SurfaceGray
import com.guiuriarte.recipeai.ui.theme.TextMedium
import com.guiuriarte.recipeai.viewmodel.HomeViewModel
import com.guiuriarte.recipeai.viewmodel.SavedRecipesViewModel

@Composable
fun SavedRecipesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: SavedRecipesViewModel = hiltViewModel()
) {
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    val displayedRecipes = if (showFavoritesOnly) favoriteRecipes else savedRecipes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Minhas Receitas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showFavoritesOnly,
                    onClick = { showFavoritesOnly = false },
                    label = { Text("Todas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandOrange,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { showFavoritesOnly = true },
                    label = { Text("Favoritas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (displayedRecipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (showFavoritesOnly) "Nenhuma favorita ainda." else "Nenhuma receita salva ainda.",
                    color = TextMedium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onNavigateToDetail(recipe.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(recipe.id, recipe.isFavorite) },
                        onDelete = { viewModel.deleteRecipe(recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val category = HomeViewModel.getCategory(recipe.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Imagem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (recipe.imageUrl != null) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍽", style = MaterialTheme.typography.headlineLarge)
                    }
                }

                // Badge categoria
                if (category != "Outras") {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = BadgeGreen
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botão deletar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱ ${recipe.cookingTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onToggleFavorite() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (recipe.isFavorite) BrandOrange else Color(0xFFCCCCCC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (recipe.source != null) {
                    Text(
                        text = "🌐 ${recipe.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }
            }
        }
    }
}
