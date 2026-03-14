package com.guiuriarte.recipeai.data.api

import com.guiuriarte.recipeai.data.api.model.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnsplashService {
    @GET("search/photos")
    suspend fun searchPhoto(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 5,
        @Query("orientation") orientation: String = "landscape"
    ): UnsplashResponse
}
