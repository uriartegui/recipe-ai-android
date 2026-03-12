package com.guiuriarte.recipeai

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AiService {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer SUA_KEY"
    )
    @POST("v1/chat/completions")
    suspend fun generateRecipe(
        @Body request: ChatRequest
    ): ChatResponse
}