package com.guiuriarte.recipeai.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.guiuriarte.recipeai.BuildConfig
import com.guiuriarte.recipeai.data.api.AiService
import com.guiuriarte.recipeai.data.api.UnsplashService
import com.guiuriarte.recipeai.data.api.model.ChatRequest
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
    private val unsplashService: UnsplashService,
    private val recipeDao: RecipeDao
) {
    private val gson = Gson()

    suspend fun generateFridgeSuggestions(ingredients: List<String>): Result<List<Recipe>> {
        val ingredientList = ingredients.joinToString(", ")
        return try {
            val prompt = """
Tenho estes ingredientes disponíveis na minha geladeira: $ingredientList

Sugira 3 receitas práticas e deliciosas que eu possa fazer principalmente com esses ingredientes.
Regras importantes:
- Cada receita deve usar a maioria dos ingredientes listados
- Não combine proteínas diferentes (frango, carne, peixe) na mesma receita
- As receitas devem ser coerentes e saborosas, não combinações estranhas
- Pode adicionar temperos básicos (sal, azeite, alho, cebola) que qualquer pessoa tem em casa
- Prefira receitas simples e rápidas

Responda APENAS com um JSON array válido, sem texto adicional:
[
  {
    "name": "Nome da receita",
    "description": "Descrição curta e apetitosa",
    "ingredients": ["200g de arroz", "2 dentes de alho picados"],
    "steps": ["Passo 1: Descrição detalhada...", "Passo 2: ..."],
    "cookingTime": "30 minutos",
    "servings": "2 porções",
    "photoSearchTerm": "nome do prato final em inglês para busca de foto (ex: 'chicken stir fry', 'pasta carbonara', 'egg fried rice')"
  }
]
Regras de formato:
- Exatamente 3 receitas
- Cada ingrediente com quantidade exata
- Mínimo de 3 passos por receita
- photoSearchTerm sempre em inglês, só o nome do prato finalizado
""".trimIndent()

            val response = aiService.generateRecipe(
                ChatRequest(messages = listOf(Message(role = "user", content = prompt)))
            )

            val content = response.choices.first().message.content
            val json = content
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val dtos = gson.fromJson(json, Array<RecipeDto>::class.java).toList()

            val recipes = coroutineScope {
                dtos.map { dto ->
                    async {
                        val recipe = dto.toDomain()
                        val searchTerm = dto.photoSearchTerm.ifBlank { recipe.name }
                        val imageUrl = fetchImage(searchTerm)
                        recipe.copy(imageUrl = imageUrl)
                    }
                }.awaitAll()
            }

            Result.success(recipes)
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("A IA retornou um formato inesperado. Tente novamente."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao gerar sugestões: ${e.message}"))
        }
    }

    suspend fun generateRecipes(ingredients: String, count: Int = 4): Result<List<Recipe>> {
        return try {
            val prompt = """
Crie $count receitas diferentes usando os seguintes ingredientes: $ingredients
Responda APENAS com um JSON array válido, sem texto adicional:
[
  {
    "name": "Nome da receita",
    "description": "Descrição curta e apetitosa",
    "ingredients": ["200g de arroz", "2 dentes de alho picados"],
    "steps": ["Passo 1: Descrição detalhada...", "Passo 2: ..."],
    "cookingTime": "30 minutos",
    "servings": "4 porções",
    "photoSearchTerm": "completed plated dish for food photography in English - must be the FINAL DISH, never just an ingredient (ex: 'spinach quiche slice', 'chicken lasagna baked', 'banana bread loaf sliced')"
  }
]
Regras:
- Exatamente $count receitas diferentes entre si
- Cada ingrediente com quantidade exata
- Mínimo de 4 passos por receita
- photoSearchTerm sempre em inglês, simples, só o nome do prato
""".trimIndent()

            val response = aiService.generateRecipe(
                ChatRequest(messages = listOf(Message(role = "user", content = prompt)))
            )

            val content = response.choices.first().message.content
            val json = content
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val dtos = gson.fromJson(json, Array<RecipeDto>::class.java).toList()

            // Busca fotos em paralelo para todas as receitas
            val recipes = coroutineScope {
                dtos.map { dto ->
                    async {
                        val recipe = dto.toDomain()
                        val searchTerm = dto.photoSearchTerm.ifBlank { recipe.name }
                        val imageUrl = fetchImage(searchTerm)
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

    private suspend fun fetchImage(query: String): String? {
        return try {
            val result = unsplashService.searchPhoto(
                authorization = "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}",
                query = "$query food photography plated"
            )
            result.results.firstOrNull()?.urls?.regular
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe.toEntity())
    }

    fun getSavedRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes().map { list -> list.map { it.toDomain() } }

    fun getFavoriteRecipes(): Flow<List<Recipe>> =
        recipeDao.getFavorites().map { list -> list.map { it.toDomain() } }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) =
        recipeDao.toggleFavorite(id, isFavorite)

    suspend fun deleteRecipe(id: String) =
        recipeDao.deleteRecipe(id)

    suspend fun getById(id: String): Recipe? =
        recipeDao.getById(id)?.toDomain()

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
        id = id,
        name = name,
        description = description,
        ingredients = gson.toJson(ingredients),
        steps = gson.toJson(steps),
        cookingTime = cookingTime,
        servings = servings,
        isFavorite = isFavorite,
        createdAt = createdAt,
        imageUrl = imageUrl
    )

    private fun RecipeEntity.toDomain() = Recipe(
        id = id,
        name = name,
        description = description,
        ingredients = gson.fromJson(ingredients, Array<String>::class.java).toList(),
        steps = gson.fromJson(steps, Array<String>::class.java).toList(),
        cookingTime = cookingTime,
        servings = servings,
        isFavorite = isFavorite,
        createdAt = createdAt,
        imageUrl = imageUrl
    )
}
