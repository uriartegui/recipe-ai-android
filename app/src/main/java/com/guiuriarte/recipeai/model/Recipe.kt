package com.guiuriarte.recipeai.model

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val cookingTime: String,
    val servings: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val source: String? = null
)

