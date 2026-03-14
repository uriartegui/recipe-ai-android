package com.guiuriarte.recipeai.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.guiuriarte.recipeai.ui.theme.BrandOrange
import com.guiuriarte.recipeai.ui.theme.SurfaceGray
import com.guiuriarte.recipeai.ui.theme.TextMedium
import com.guiuriarte.recipeai.viewmodel.SavedRecipesViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateToCooking: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SavedRecipesViewModel = hiltViewModel()
) {
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val recipe = savedRecipes.find { it.id == recipeId }

    if (recipe == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandOrange)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Hero image
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                if (recipe.imageUrl != null) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍽", style = MaterialTheme.typography.displayLarge)
                    }
                }
                // Gradiente escuro no topo e embaixo
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.35f),
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                // Título sobre a imagem (embaixo)
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "⏱ ${recipe.cookingTime}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "🍽 ${recipe.servings}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Conteúdo
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Descrição
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMedium
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Ingredientes
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandOrange
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recipe.ingredients.forEach { ingredient ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BrandOrange, CircleShape)
                            )
                            Text(ingredient, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Modo de preparo
                Text(
                    text = "Modo de preparo",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandOrange
                )
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    recipe.steps.forEachIndexed { index, step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(BrandOrange, CircleShape)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f).padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onNavigateToCooking(recipe.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text(
                        "Iniciar Modo Cozinha 👨‍🍳",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Botão voltar (overlay)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
        }

        // Botão favoritar (overlay)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { viewModel.toggleFavorite(recipe.id, recipe.isFavorite) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (recipe.isFavorite) BrandOrange else Color.White
                )
            }
        }
    }
}
