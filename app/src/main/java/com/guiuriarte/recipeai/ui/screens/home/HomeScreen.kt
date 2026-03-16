package com.guiuriarte.recipeai.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.foundation.border
import com.guiuriarte.recipeai.model.ALL_CATEGORIES
import com.guiuriarte.recipeai.model.ALL_TAGS

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToRecipeDetail: (String) -> Unit,
    onNavigateToFridge: () -> Unit,
    initialIngredients: List<String> = emptyList(),
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val fridgeSuggestions by viewModel.fridgeSuggestions.collectAsState()

    var input by remember { mutableStateOf("") }
    val chips by viewModel.chips.collectAsState()
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var showNoIngredientsDialog by remember { mutableStateOf(false) }
    var showServingsDialog by remember { mutableStateOf(false) }
    var servingsInput by remember { mutableStateOf("") }
    var selectedSubcategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTags by rememberSaveable(
        stateSaver = listSaver(
            save = { it.toList() },
            restore = { it.toSet() }
        )
    ) { mutableStateOf(emptySet<String>()) }

    fun addChip() {
        val item = input.trim()
        if (item.isNotEmpty() && !chips.contains(item)) {
            viewModel.addChip(item)
            input = ""
        }
    }

    LaunchedEffect(initialIngredients) {
        if (initialIngredients.isNotEmpty()) {
            initialIngredients.forEach { viewModel.addChip(it) }
        }
    }

    if (showNoIngredientsDialog) {
        Dialog(onDismissRequest = { showNoIngredientsDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                // Botão X fechar
                IconButton(
                    onClick = { showNoIngredientsDialog = false },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.Gray)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Ícone laranja
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFE0CC), Color(0xFFFFF3EC))
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF3366))
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Text("!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Nenhum ingrediente\nadicionado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        buildAnnotatedString {
                            append("Escreva os ingredientes, clique no ")
                            withStyle(SpanStyle(color = BrandOrange, fontWeight = FontWeight.Bold)) { append("+") }
                            append(" e depois em ")
                            withStyle(SpanStyle(color = BrandOrange, fontWeight = FontWeight.Bold)) { append("Gerar Receitas.") }
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    // Card bege
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3EC), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("Caso queira continuar sem ingredientes, clique em ")
                                withStyle(SpanStyle(color = BrandOrange, fontWeight = FontWeight.Bold)) { append("\"Gerar sem ingrediente\".") }
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Botão Gerar sem ingrediente
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF6B35), Color(0xFFFF3366))
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                showNoIngredientsDialog = false
                                val query = buildString {
                                    if (selectedCategory != null) {
                                        append(selectedCategory)
                                        if (selectedSubcategory != null) append(": $selectedSubcategory")
                                    } else {
                                        append("receitas variadas")
                                    }
                                    if (selectedTags.isNotEmpty()) {
                                        append(" - estilo: ${selectedTags.joinToString(", ")}")
                                    }
                                }
                                viewModel.onQueryChange(query)
                                viewModel.generateRecipes()
                            }
                    ) {
                        Text("Gerar sem ingrediente", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Botão Voltar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(50))
                            .clickable { showNoIngredientsDialog = false }
                    ) {
                        Text("Voltar", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
    if (showServingsDialog) {
        Dialog(onDismissRequest = { showServingsDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = { showServingsDialog = false },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.Gray)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Para quantas pessoas? 👨‍👩‍👧‍👦",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "A IA vai ajustar as quantidades dos ingredientes para o número certo de pessoas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium,
                        textAlign = TextAlign.Center
                    )
                    OutlinedTextField(
                        value = servingsInput,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) servingsInput = it },
                        placeholder = {
                            Text(
                                "Quantidade de pessoas para a receita",
                                color = TextMedium.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val servings = servingsInput.toIntOrNull()?.coerceIn(1, 100) ?: 4
                            showServingsDialog = false
                            if (chips.isEmpty() && selectedCategory == null && selectedTags.isEmpty()) {
                                showNoIngredientsDialog = true
                            } else {
                                val query = buildString {
                                    if (chips.isNotEmpty()) append(chips.joinToString(", "))
                                    if (selectedCategory != null) {
                                        if (isNotEmpty()) append(" - ")
                                        append(selectedCategory)
                                        if (selectedSubcategory != null) append(": $selectedSubcategory")
                                    }
                                    if (selectedTags.isNotEmpty()) {
                                        if (isNotEmpty()) append(" - ")
                                        append("estilo: ${selectedTags.joinToString(", ")}")
                                    }
                                }
                                viewModel.onQueryChange(query)
                                viewModel.generateRecipes(servings)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(listOf(BrandOrange, Color(0xFFFF4F7B))),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("Gerar Receitas", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
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
                    text = "Olá, Chef! 🍳",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )
                Text(
                    text = "O que vamos cozinhar hoje?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )
            }
        }

        // Seção da Geladeira — no topo
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
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
                                        if (recipe.source != null) {
                                            Text(
                                                "🌐 ${recipe.source}",
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
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🧊", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Sua geladeira está vazia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                "Preencha com os ingredientes que você tem em casa para ver receitas sugeridas aqui",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium,
                                textAlign = TextAlign.Center
                            )
                            TextButton(onClick = onNavigateToFridge) {
                                Text(
                                    "Ir para geladeira  >",
                                    color = BrandOrange,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Divisor
        item(span = { GridItemSpan(2) }) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFFE0E0E0)
            )
        }

        // Categorias
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(ALL_CATEGORIES) { cat ->
                    val isSelected = selectedCategory == cat.name
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) BrandOrange else SurfaceGray,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                if (isSelected) {
                                    selectedCategory = null
                                    selectedSubcategory = null
                                } else {
                                    selectedCategory = cat.name
                                    selectedSubcategory = null
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${cat.emoji} ${cat.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

// Subcategorias — aparece inline quando categoria tem subcategorias
        val currentCategory = ALL_CATEGORIES.find { it.name == selectedCategory }
        if (currentCategory != null && currentCategory.subcategories.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(currentCategory.subcategories) { sub ->
                        val isSubSelected = selectedSubcategory == sub
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent, RoundedCornerShape(50))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSubSelected) BrandOrange else Color(0xFFDDDDDD),
                                    shape = RoundedCornerShape(50)
                                )
                                .background(
                                    color = if (isSubSelected) BrandOrange.copy(alpha = 0.12f) else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    selectedSubcategory = if (isSubSelected) null else sub
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                sub,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSubSelected) BrandOrange else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

// Tags — aparecem só após selecionar subcategoria
        if (selectedSubcategory != null) item(span = { GridItemSpan(2) }) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_TAGS.forEach { tag ->
                    val isTagSelected = tag in selectedTags
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isTagSelected) BrandOrange else SurfaceGray,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                selectedTags = if (isTagSelected) selectedTags - tag else selectedTags + tag
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTagSelected) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Input + Botão
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ex: leite, ovo, macarrão...", color = TextMedium) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOrange,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = SurfaceGray
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(BrandOrange, Color(0xFFFF4F7B))),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { addChip() }, enabled = input.isNotBlank()) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color.White)
                        }
                    }
                }

                // Chips dos ingredientes
                if (chips.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chips.forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(listOf(BrandOrange, Color(0xFFFF4F7B))),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(chip, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remover",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.removeChip(chip) }
                                    )
                                }
                            }
                        }
                    }
                }

                val canGenerate = uiState !is HomeUiState.Loading
                Button(
                    onClick = {
                        servingsInput = ""
                        showServingsDialog = true
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = canGenerate,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (canGenerate)
                                    Brush.linearGradient(listOf(BrandOrange, Color(0xFFFF4F7B)))
                                else
                                    Brush.linearGradient(listOf(BrandOrange.copy(alpha = 0.4f), Color(0xFFFF4F7B).copy(alpha = 0.4f))),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Gerar Receitas", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
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
                item(span = { GridItemSpan(2) }) {
                    OutlinedButton(
                        onClick = {
                            servingsInput = ""
                            showServingsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrange)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gerar outras", color = BrandOrange)
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

    }
}
