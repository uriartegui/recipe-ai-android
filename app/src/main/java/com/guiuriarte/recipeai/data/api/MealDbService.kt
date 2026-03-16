package com.guiuriarte.recipeai.data.api

import com.guiuriarte.recipeai.data.api.model.MealDbDetailResponse
import com.guiuriarte.recipeai.data.api.model.MealDbFilterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbService {
    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealDbFilterResponse

    @GET("lookup.php")
    suspend fun lookupById(@Query("i") id: String): MealDbDetailResponse
}
