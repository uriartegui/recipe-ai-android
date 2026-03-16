package com.guiuriarte.recipeai.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.guiuriarte.recipeai.BuildConfig
import com.guiuriarte.recipeai.data.api.AiService
import com.guiuriarte.recipeai.data.api.MealDbService
import com.guiuriarte.recipeai.data.api.UnsplashService
import com.guiuriarte.recipeai.data.api.model.ChatRequest
import com.guiuriarte.recipeai.data.api.model.MealDetail
import com.guiuriarte.recipeai.data.api.model.Message
import com.guiuriarte.recipeai.data.api.model.RecipeDto
import com.guiuriarte.recipeai.data.database.RecipeDao
import com.guiuriarte.recipeai.data.database.entity.RecipeEntity
import com.guiuriarte.recipeai.model.Recipe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val aiService: AiService,
    private val mealDbService: MealDbService,
    private val unsplashService: UnsplashService,
    private val recipeDao: RecipeDao
) {
    private val gson = Gson()

    // ── Fridge Suggestions ──────────────────────────────────────────────────

    suspend fun generateFridgeSuggestions(ingredients: List<String>): Result<List<Recipe>> {
        val mealDbResults = searchMealDb(ingredients)
        return if (mealDbResults.isNotEmpty()) {
            translateMealDbAndConvert(mealDbResults)
        } else {
            generateFridgeSuggestionsWithAI(ingredients)
        }
    }

    private suspend fun searchMealDb(ingredients: List<String>): List<MealDetail> {
        val ignoredIngredients = setOf("sal", "azeite", "alho", "cebola", "manteiga", "pimenta", "agua", "água")
        val mainIngredients = ingredients.filter { it.lowercase() !in ignoredIngredients }.take(3)

        val mealIds = mutableSetOf<String>()
        val meals = mutableListOf<MealDetail>()

        for (ingredient in mainIngredients) {
            try {
                val response = mealDbService.filterByIngredient(ingredient)
                response.meals?.take(2)?.forEach { mealIds.add(it.idMeal) }
            } catch (e: Exception) { continue }
            if (mealIds.size >= 3) break
        }

        for (id in mealIds.take(3)) {
            try {
                val detail = mealDbService.lookupById(id)
                detail.meals?.firstOrNull()?.let { meals.add(it) }
            } catch (e: Exception) { continue }
        }

        return meals
    }

    private suspend fun translateMealDbAndConvert(meals: List<MealDetail>): Result<List<Recipe>> {
        return try {
            val mealsJson = meals.joinToString("\n\n") { meal ->
                """
Nome: ${meal.strMeal}
Ingredientes: ${meal.ingredientsList().joinToString(", ")}
Instruções: ${meal.strInstructions?.take(800)}
                """.trimIndent()
            }

            val prompt = """
Traduza e adapte estas ${meals.size} receitas para o português brasileiro. Retorne como JSON array:
$mealsJson

Responda APENAS com um JSON array válido:
[
  {
    "name": "Nome traduzido",
    "description": "Descrição curta e apetitosa em português",
    "ingredients": ["quantidade + ingrediente em português"],
    "steps": ["Passo 1: ...", "Passo 2: ..."],
    "cookingTime": "X minutos",
    "servings": "X porções",
    "photoSearchTerm": "nome do prato em inglês para busca de foto"
  }
]
Regras:
- Traduza tudo para português
- Adapte medidas (cups → xícaras, oz → gramas aproximadas)
- Mínimo 3 passos por receita
- photoSearchTerm sempre em inglês
- Se não tiver informação de tempo de preparo, estime um tempo realista para a receita
""".trimIndent()

            val response = aiService.generateRecipe(
                ChatRequest(messages = listOf(Message(role = "user", content = prompt)))
            )

            val content = response.choices.first().message.content
            val json = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val dtos = gson.fromJson(json, Array<RecipeDto>::class.java).toList()

            val recipes = coroutineScope {
                dtos.mapIndexed { index, dto ->
                    async {
                        val recipe = dto.toDomain()
                        val imageUrl = meals.getOrNull(index)?.strMealThumb ?: fetchImage(dto.photoSearchTerm)
                        recipe.copy(imageUrl = imageUrl, source = "TheMealDB")
                    }
                }.awaitAll()
            }

            Result.success(recipes)
        } catch (e: Exception) {
            generateFridgeSuggestionsWithAI(meals.map { it.strMeal })
        }
    }

    private suspend fun generateFridgeSuggestionsWithAI(ingredients: List<String>): Result<List<Recipe>> {
        val ingredientList = ingredients.joinToString(", ")
        return try {
            val prompt = """
Tenho estes ingredientes disponíveis: $ingredientList

Crie 3 receitas usando APENAS ingredientes que fazem sentido juntos.

Regras obrigatórias:
- NUNCA misture proteínas diferentes na mesma receita
- NUNCA misture ingredientes doces com proteínas salgadas de forma estranha
- Cada receita usa apenas 2 a 4 dos ingredientes disponíveis
- Pode complementar com temperos básicos (sal, alho, azeite, cebola, manteiga)
- As 3 receitas devem ser diferentes entre si
- Prefira receitas simples, do dia a dia

Responda APENAS com um JSON array válido:
[
  {
    "name": "Nome da receita",
    "description": "Descrição curta e apetitosa",
    "ingredients": ["200g de arroz", "2 dentes de alho picados"],
    "steps": ["Passo 1: ...", "Passo 2: ..."],
    "cookingTime": "30 minutos",
    "servings": "2 porções",
    "photoSearchTerm": "nome do prato em inglês"
  }
]
""".trimIndent()

            val response = aiService.generateRecipe(
                ChatRequest(messages = listOf(Message(role = "user", content = prompt)))
            )

            val content = response.choices.first().message.content
            val json = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val dtos = gson.fromJson(json, Array<RecipeDto>::class.java).toList()

            val recipes = coroutineScope {
                dtos.map { dto ->
                    async {
                        val recipe = dto.toDomain()
                        val imageUrl = fetchImage(dto.photoSearchTerm.ifBlank { recipe.name })
                        recipe.copy(imageUrl = imageUrl)
                    }
                }.awaitAll()
            }

            Result.success(recipes)
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("A IA retornou um formato inesperado."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao gerar sugestões: ${e.message}"))
        }
    }

    // ── Main Recipe Generation ───────────────────────────────────────────────
    suspend fun generateRecipes(ingredients: String, count: Int = 4, servings: Int = 4, excludeNames: List<String> = emptyList()): Result<List<Recipe>> {
        return try {
            val excludeText = if (excludeNames.isNotEmpty())
                "\n- NÃO repita estas receitas já mostradas: ${excludeNames.joinToString(", ")}"
            else ""

            val prompt = """
Crie $count receitas diferentes e variadas usando: $ingredients

Responda APENAS com um JSON array válido:
[
  {
    "name": "Nome da receita",
    "description": "Descrição curta e apetitosa",
    "ingredients": ["200g de arroz", "2 dentes de alho picados"],
    "steps": ["Passo 1: ...", "Passo 2: ..."],
    "cookingTime": "30 minutos",
    "servings": "$servings pessoas",
    "photoSearchTerm": "nome do prato finalizado em inglês"
  }
]
Regras:
- Exatamente $count receitas DIFERENTES entre si$excludeText
- Cada receita deve ser para $servings pessoas, ajuste TODAS as quantidades dos ingredientes proporcionalmente
- O tempo de preparo deve ser realista para $servings pessoas (mais pessoas = mais tempo)
- Cada ingrediente com quantidade exata (ex: "300g de frango", "2 dentes de alho picados")
- Mínimo de 6 passos por receita
- Cada passo deve ser DETALHADO e profissional: inclua quantidades exatas no passo (ex: "adicione 10g de sal", "despeje 200ml de água fervente"), temperatura quando aplicável (ex: "forno pré-aquecido a 180°C") e tempo de cada etapa (ex: "refogue por 5 minutos até dourar levemente")
- Cada passo deve ter pelo menos 2 frases descritivas explicando o que fazer e por quê
- photoSearchTerm sempre em inglês
- Tempo de preparo realista (nunca 0 minutos)
""".trimIndent()

            val response = aiService.generateRecipe(
                ChatRequest(messages = listOf(Message(role = "user", content = prompt)))
            )

            val content = response.choices.first().message.content
            val json = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val dtos = gson.fromJson(json, Array<RecipeDto>::class.java).toList()

            val recipes = coroutineScope {
                dtos.map { dto ->
                    async {
                        val recipe = dto.toDomain()
                        val imageUrl = fetchImage(dto.photoSearchTerm.ifBlank { recipe.name })
                        recipe.copy(imageUrl = imageUrl)
                    }
                }.awaitAll()
            }

            Result.success(recipes)
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("A IA retornou um formato inesperado. Tente novamente."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao gerar receitas: ${e.message}"))
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun fetchImage(query: String): String? {
        return try {
            val result = unsplashService.searchPhoto(
                authorization = "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}",
                query = "$query food photography plated"
            )
            result.results.firstOrNull()?.urls?.regular
        } catch (e: Exception) { null }
    }

    suspend fun saveRecipe(recipe: Recipe) = recipeDao.insertRecipe(recipe.toEntity())

    fun getSavedRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes().map { list -> list.map { it.toDomain() } }

    fun getFavoriteRecipes(): Flow<List<Recipe>> =
        recipeDao.getFavorites().map { list -> list.map { it.toDomain() } }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) =
        recipeDao.toggleFavorite(id, isFavorite)

    suspend fun deleteRecipe(id: String) = recipeDao.deleteRecipe(id)

    suspend fun getById(id: String): Recipe? = recipeDao.getById(id)?.toDomain()

    private fun RecipeDto.toDomain() = Recipe(
        id = UUID.randomUUID().toString(),
        name = name,
        description = description,
        ingredients = ingredients,
        steps = steps,
        cookingTime = cookingTime,
        servings = servings
    )

    private fun Recipe.toEntity() = RecipeEntity(
        id = id, name = name, description = description,
        ingredients = gson.toJson(ingredients), steps = gson.toJson(steps),
        cookingTime = cookingTime, servings = servings,
        isFavorite = isFavorite, createdAt = createdAt, imageUrl = imageUrl
    )

    private fun RecipeEntity.toDomain() = Recipe(
        id = id, name = name, description = description,
        ingredients = gson.fromJson(ingredients, Array<String>::class.java).toList(),
        steps = gson.fromJson(steps, Array<String>::class.java).toList(),
        cookingTime = cookingTime, servings = servings,
        isFavorite = isFavorite, createdAt = createdAt, imageUrl = imageUrl
    )
}