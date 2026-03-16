package com.guiuriarte.recipeai.model

data class RecipeCategory(
    val emoji: String,
    val name: String,
    val subcategories: List<String> = emptyList()
)

val ALL_CATEGORIES = listOf(
    RecipeCategory("🍳", "Café da Manhã", listOf("Ovos", "Panquecas", "Tapioca", "Vitaminas", "Pão", "Frutas")),
    RecipeCategory("🍝", "Massas", listOf("Macarrão", "Lasanha", "Nhoque", "Espaguete", "Ravioli", "Penne")),
    RecipeCategory("🥩", "Carnes", listOf("Bife", "Carne Moída", "Costela", "Churrasco", "Hambúrguer", "Assado")),
    RecipeCategory("🍗", "Frango", listOf("Grelhado", "Assado", "Refogado", "Ensopado", "Frito", "Recheado")),
    RecipeCategory("🐟", "Peixes", listOf("Salmão", "Atum", "Tilápia", "Bacalhau", "Camarão", "Lula")),
    RecipeCategory("🥗", "Saladas", listOf("Simples", "Com Proteína", "Bowl", "Caesar", "Tropical")),
    RecipeCategory("🥪", "Lanches", listOf("Sanduíche", "Hambúrguer", "Wrap", "Torrada", "Hot Dog")),
    RecipeCategory("🍲", "Sopas", listOf("Caldo", "Creme", "Sopa Fria", "Feijão", "Lentilha")),
    RecipeCategory("🍚", "Arroz", listOf("Arroz Branco", "Risoto", "Arroz Colorido", "Carreteiro")),
    RecipeCategory("🍕", "Pizzas", listOf("Tradicional", "Integral", "Especial", "Pizza Doce")),
    RecipeCategory("🍰", "Sobremesas", listOf("Bolo", "Torta", "Mousse", "Sorvete", "Brigadeiro", "Brownie")),
    RecipeCategory("🌎", "Cozinhas do Mundo", listOf("Brasileira", "Italiana", "Japonesa", "Mexicana", "Chinesa", "Indiana")),
    RecipeCategory("🥦", "Vegetariano", listOf("Proteína Vegetal", "Legumes", "Tofu", "Vegano")),
    RecipeCategory("🎲", "Aleatório", emptyList())
)

val ALL_TAGS = listOf(
    "⚡ Rápido", "🔥 Air Fryer", "🥑 Saudável", "🌱 Vegano",
    "💪 Proteico", "💰 Econômico", "🍽️ Fácil", "👨‍🍳 Gourmet"
)
