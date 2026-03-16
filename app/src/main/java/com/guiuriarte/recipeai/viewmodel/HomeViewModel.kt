package com.guiuriarte.recipeai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiuriarte.recipeai.data.repository.FridgeRepository
import com.guiuriarte.recipeai.data.repository.RecipeRepository
import com.guiuriarte.recipeai.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val recipes: List<Recipe>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class FridgeSuggestionsState {
    object Idle : FridgeSuggestionsState()
    object Loading : FridgeSuggestionsState()
    data class Success(val recipes: List<Recipe>) : FridgeSuggestionsState()
    object Empty : FridgeSuggestionsState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val fridgeRepository: FridgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _fridgeSuggestions = MutableStateFlow<FridgeSuggestionsState>(FridgeSuggestionsState.Idle)
    val fridgeSuggestions: StateFlow<FridgeSuggestionsState> = _fridgeSuggestions.asStateFlow()

    private val _chips = MutableStateFlow<List<String>>(emptyList())
    val chips: StateFlow<List<String>> = _chips.asStateFlow()

    init {
        viewModelScope.launch {
            fridgeRepository.ingredients
                .collect { loadFridgeSuggestions() }
        }
    }

    fun addChip(item: String) {
        if (item.isNotBlank() && !_chips.value.contains(item))
            _chips.value = _chips.value + item
    }

    fun removeChip(item: String) {
        _chips.value = _chips.value - item
    }

    fun loadFridgeSuggestions() {
        val ingredients = fridgeRepository.ingredients.value
        if (ingredients.size < 2) {
            _fridgeSuggestions.value = FridgeSuggestionsState.Empty
            return
        }
        viewModelScope.launch {
            _fridgeSuggestions.value = FridgeSuggestionsState.Loading
            val result = repository.generateFridgeSuggestions(ingredients.shuffled().take(6))
            _fridgeSuggestions.value = result.fold(
                onSuccess = { FridgeSuggestionsState.Success(it) },
                onFailure = { FridgeSuggestionsState.Empty }
            )
        }
    }

    fun onQueryChange(value: String) { _query.value = value }

    private val _shownRecipeNames = mutableListOf<String>()

    fun generateRecipes(servings: Int = 4) {
        val q = _query.value.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = repository.generateRecipes(q, servings = servings, excludeNames = _shownRecipeNames.toList())
            _uiState.value = result.fold(
                onSuccess = { recipes ->
                    _shownRecipeNames.addAll(recipes.map { it.name })
                    HomeUiState.Success(recipes)
                },
                onFailure = { HomeUiState.Error(it.message ?: "Erro desconhecido") }
            )
        }
    }

    fun clearAll() {
        _uiState.value = HomeUiState.Idle
        _query.value = ""
        _shownRecipeNames.clear()
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch { repository.saveRecipe(recipe) }
    }

    fun setIngredients(list: List<String>) {
        _query.value = list.joinToString(", ")
    }

    companion object {
        val categories = listOf("Massas", "Saladas", "Sobremesas", "Peixes", "Frango", "Mexicana", "Outras")

        fun getCategory(name: String): String {
            val lower = name.lowercase()
            return when {
                lower.containsAny("macarrão", "pasta", "espaguete", "lasanha", "nhoque", "penne") -> "Massas"
                lower.containsAny("salada", "bowl") -> "Saladas"
                lower.containsAny("bolo", "torta", "pudim", "mousse", "sorvete", "brigadeiro", "brownie") -> "Sobremesas"
                lower.containsAny("peixe", "salmão", "atum", "tilápia", "bacalhau", "camarão") -> "Peixes"
                lower.containsAny("frango", "galinha", "chester") -> "Frango"
                lower.containsAny("taco", "burrito", "guacamole", "nachos") -> "Mexicana"
                else -> "Outras"
            }
        }

        private fun String.containsAny(vararg keywords: String) =
            keywords.any { this.contains(it) }
    }
}
