package com.guiuriarte.recipeai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiuriarte.recipeai.ui.theme.RecipeAITheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.guiuriarte.recipeai.ChatRequest
import com.guiuriarte.recipeai.Message
import com.guiuriarte.recipeai.ApiClient
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecipeAITheme {
                RecipeScreen()
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun RecipeScreen() {

    val scope = rememberCoroutineScope()

    var ingredients by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val commonIngredients = listOf(
        "Arroz",
        "Frango",
        "Ovo",
        "Tomate",
        "Queijo",
        "Batata",
        "Carne",
        "Cebola"
    )

    Scaffold(

        topBar = {

            TopAppBar(
                title = { Text("Recipe AI") }
            )

        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("Digite os ingredientes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingredientes rápidos",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonIngredients.forEach { ingredient ->

                    AssistChip(
                        onClick = {
                            ingredients =
                                if (ingredients.isEmpty())
                                    ingredient
                                else
                                    "$ingredients, $ingredient"
                        },
                        label = { Text(ingredient) }
                    )
                }
            }

        Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    loading = true

                    scope.launch {

                        try {

                            val response = ApiClient.api.generateRecipe(
                                ChatRequest(
                                    messages = listOf(
                                        Message(
                                            role = "user",
                                            content = "Crie uma receita usando: $ingredients"
                                        )
                                    )
                                )
                            )

                            recipe = response.choices.first().message.content

                        } catch (e: Exception) {

                            recipe = "Erro ao gerar receita"

                        }

                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ingredients.isNotBlank()
            ) {
                Text("Gerar Receita")
            }

        Spacer(modifier = Modifier.height(30.dp))

        if (loading) {
            CircularProgressIndicator()
        }

            if (recipe.isNotEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Receita gerada",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(recipe)

                    }
                }
            }

        Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    ingredients = ""
                    recipe = ""
                    loading = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpar")
            }
        }
    }
}