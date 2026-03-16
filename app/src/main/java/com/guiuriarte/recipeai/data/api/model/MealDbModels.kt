package com.guiuriarte.recipeai.data.api.model

data class MealDbFilterResponse(val meals: List<MealSummary>?)
data class MealSummary(val strMeal: String, val strMealThumb: String, val idMeal: String)
data class MealDbDetailResponse(val meals: List<MealDetail>?)

data class MealDetail(
    val idMeal: String,
    val strMeal: String,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strIngredient1: String?, val strMeasure1: String?,
    val strIngredient2: String?, val strMeasure2: String?,
    val strIngredient3: String?, val strMeasure3: String?,
    val strIngredient4: String?, val strMeasure4: String?,
    val strIngredient5: String?, val strMeasure5: String?,
    val strIngredient6: String?, val strMeasure6: String?,
    val strIngredient7: String?, val strMeasure7: String?,
    val strIngredient8: String?, val strMeasure8: String?,
    val strIngredient9: String?, val strMeasure9: String?,
    val strIngredient10: String?, val strMeasure10: String?,
) {
    fun ingredientsList(): List<String> = listOfNotNull(
        if (!strIngredient1.isNullOrBlank()) "${strMeasure1?.trim()} ${strIngredient1.trim()}".trim() else null,
        if (!strIngredient2.isNullOrBlank()) "${strMeasure2?.trim()} ${strIngredient2.trim()}".trim() else null,
        if (!strIngredient3.isNullOrBlank()) "${strMeasure3?.trim()} ${strIngredient3.trim()}".trim() else null,
        if (!strIngredient4.isNullOrBlank()) "${strMeasure4?.trim()} ${strIngredient4.trim()}".trim() else null,
        if (!strIngredient5.isNullOrBlank()) "${strMeasure5?.trim()} ${strIngredient5.trim()}".trim() else null,
        if (!strIngredient6.isNullOrBlank()) "${strMeasure6?.trim()} ${strIngredient6.trim()}".trim() else null,
        if (!strIngredient7.isNullOrBlank()) "${strMeasure7?.trim()} ${strIngredient7.trim()}".trim() else null,
        if (!strIngredient8.isNullOrBlank()) "${strMeasure8?.trim()} ${strIngredient8.trim()}".trim() else null,
        if (!strIngredient9.isNullOrBlank()) "${strMeasure9?.trim()} ${strIngredient9.trim()}".trim() else null,
        if (!strIngredient10.isNullOrBlank()) "${strMeasure10?.trim()} ${strIngredient10.trim()}".trim() else null,
    )
}
