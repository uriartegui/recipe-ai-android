package com.guiuriarte.recipeai.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeRepository @Inject constructor(
    private val prefs: SharedPreferences,
    private val gson: Gson
) {
    private val KEY = "fridge_ingredients"

    private val _ingredients = MutableStateFlow(load())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()

    private fun load(): List<String> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try { gson.fromJson(json, Array<String>::class.java).toList() } catch (e: Exception) { emptyList() }
    }

    fun setIngredients(ingredients: List<String>) {
        _ingredients.value = ingredients
        prefs.edit().putString(KEY, gson.toJson(ingredients)).commit()
    }
}
