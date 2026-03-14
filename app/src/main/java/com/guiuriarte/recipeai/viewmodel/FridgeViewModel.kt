package com.guiuriarte.recipeai.viewmodel

import androidx.lifecycle.ViewModel
import com.guiuriarte.recipeai.data.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepository
) : ViewModel() {
    val ingredients: StateFlow<List<String>> = fridgeRepository.ingredients

    fun addIngredient(item: String) {
        val updated = fridgeRepository.ingredients.value + item
        fridgeRepository.setIngredients(updated)
    }

    fun removeIngredient(index: Int) {
        val updated = fridgeRepository.ingredients.value.toMutableList().also { it.removeAt(index) }
        fridgeRepository.setIngredients(updated)
    }

    fun setIngredients(ingredients: List<String>) {
        fridgeRepository.setIngredients(ingredients)
    }
}
