package com.guiuriarte.recipeai.data.api.model

data class RecipeDto(
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val cookingTime: String,
    val servings: String,
    val photoSearchTerm: String = ""
)
