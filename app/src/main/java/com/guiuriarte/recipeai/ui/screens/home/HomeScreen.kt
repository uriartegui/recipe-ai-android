package com.guiuriarte.recipeai.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guiuriarte.recipeai.ui.screens.saved.RecipeCard
import com.guiuriarte.recipeai.ui.theme.BrandOrange
import com.guiuriarte.recipeai.ui.theme.BrandOrangeLight
import com.guiuriarte.recipeai.ui.theme.SurfaceGray
import com.guiuriarte.recipeai.ui.theme.TextMedium
import com.guiuriarte.recipeai.viewmodel.FridgeSuggestionsState
import com.guiuriarte.recipeai.viewmodel.HomeUiState
import com.guiuriarte.recipeai.viewmodel.HomeViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigateToRecipeDetail: (String) -> Unit,
    initialIngredients: List<String> = emptyList(),
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val fridgeSuggestions by viewModel.fridgeSuggestions.collectAsState()

    LaunchedEffect(initialIngredients) {
        if (initialIngredients.isNotEmpty()) viewModel.setIngredients(initialIngredients)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            bottom = 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Olá, Chef! 👋",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMedium
                )
                Text(
                    text = "O que vamos cozinhar hoje?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Input + Botão
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Ex: leite, ovo, macarrão...", color = TextMedium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 100.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = SurfaceGray
                    )
                )

                Button(
                    onClick = viewModel::generateRecipes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = query.isNotBlank() && uiState !is HomeUiState.Loading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOrange,
                        disabledContainerColor = BrandOrange.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        "Gerar Receitas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Estados da busca manual
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = BrandOrange, strokeWidth = 3.dp)
                            Text(
                                "Buscando receitas e fotos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMedium
                            )
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEEEE))
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is HomeUiState.Success -> {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.recipes.size} receitas encontradas",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMedium
                        )
                        TextButton(onClick = viewModel::clearAll) {
                            Text("Limpar", color = BrandOrange, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                items(state.recipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = {
                            viewModel.saveRecipe(recipe)
                            onNavigateToRecipeDetail(recipe.id)
                        },
                        onToggleFavorite = {},
                        onDelete = {}
                    )
                }
            }

            else -> {}
        }

        // Seção da Geladeira — sempre visível
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Da sua geladeira 🧊",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (fridgeSuggestions is FridgeSuggestionsState.Success) {
                    TextButton(onClick = viewModel::loadFridgeSuggestions) {
                        Text("Gerar novas", color = BrandOrange, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        when (val fs = fridgeSuggestions) {
            is FridgeSuggestionsState.Loading -> {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BrandOrange,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Buscando sugestões...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMedium
                            )
                        }
                    }
                }
            }

            is FridgeSuggestionsState.Success -> {
                item(span = { GridItemSpan(2) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(fs.recipes, key = { "fridge_${it.id}" }) { recipe ->
                            Card(
                                onClick = {
                                    viewModel.saveRecipe(recipe)
                                    onNavigateToRecipeDetail(recipe.id)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(130.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceGray)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = recipe.imageUrl,
                                        contentDescription = recipe.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            recipe.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "⏱ ${recipe.cookingTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandOrangeLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧊", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                "Sua geladeira está vazia",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Preencha com os ingredientes que você tem em casa para ver receitas sugeridas aqui",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
