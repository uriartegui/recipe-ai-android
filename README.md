# 🍳 RecipeAI
### Gerador Inteligente de Receitas com IA
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-orange)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Version](https://img.shields.io/badge/version-1.0.0--alpha-informational)
![MinSDK](https://img.shields.io/badge/minSdk-26-yellow)

> Diga o que você tem na geladeira. A IA cria as receitas.

RecipeAI é um app Android que usa Inteligência Artificial para gerar receitas personalizadas com base nos ingredientes que você tem em casa, na categoria que você quer cozinhar ou em filtros como "Rápido", "Saudável" e "Gourmet". As receitas são geradas com passos detalhados e profissionais, fotos reais e ajuste automático de quantidades para qualquer número de pessoas.

---

## 💡 O que o RecipeAI resolve

Quem cozinha em casa enfrenta todo dia os mesmos problemas:

- Não sabe o que cozinhar com o que tem disponível.
- Busca receitas genéricas que não consideram os ingredientes reais na geladeira.
- Receitas com passos vagos e sem quantidades precisas.
- Dificuldade de adaptar receitas para mais ou menos pessoas.

RecipeAI resolve isso gerando receitas sob medida, com ingredientes que você realmente tem, para o número de pessoas que precisa, com instruções detalhadas passo a passo.

---

## 🎯 Funcionalidades

- 🧠 **Geração de receitas com IA** — envia ingredientes, categoria e filtros; a IA gera receitas completas e detalhadas.
- 🧊 **Geladeira inteligente** — cadastre os ingredientes que você tem em casa e receba sugestões automáticas toda vez que abrir o app.
- 🗂️ **Sistema de 3 níveis de categoria** — Categoria → Subcategoria → Tags para filtrar exatamente o que quer cozinhar.
- 👨‍👩‍👧 **Ajuste de porções** — informe para quantas pessoas e a IA ajusta todos os ingredientes automaticamente.
- 📋 **Passos detalhados e profissionais** — cada passo inclui quantidades exatas, temperatura e tempo de preparo.
- 📸 **Fotos reais das receitas** — imagens buscadas automaticamente via Unsplash para cada receita gerada.
- 💾 **Salvar e favoritar** — salve receitas geradas e marque suas favoritas para acessar depois.
- 🌐 **Integração com TheMealDB** — quando disponível, busca receitas reais do banco de dados e as traduz para português.
- 🔄 **Gerar outras** — clique em "Gerar outras" para receber novas sugestões sem repetir as já mostradas.
- 🍽️ **Modo de preparo guiado** — tela dedicada para seguir o passo a passo durante o cozimento.

---

## 🗂️ Sistema de Categorias

O app organiza receitas em 3 níveis progressivos:

**1. Categoria principal** (sempre visível)
> 🍳 Café da Manhã · 🍝 Massas · 🥩 Carnes · 🍗 Frango · 🐟 Peixes · 🥗 Saladas · 🥪 Lanches · 🍲 Sopas · 🍚 Arroz · 🍕 Pizzas · 🍰 Sobremesas · 🌎 Cozinhas do Mundo · 🥦 Vegetariano · 🎲 Aleatório

**2. Subcategoria** (aparece ao clicar na categoria)
> Ex: Massas → Macarrão, Lasanha, Nhoque, Espaguete, Ravioli, Penne

**3. Tags / Filtros** (aparecem ao selecionar subcategoria)
> ⚡ Rápido · 🔥 Air Fryer · 🥑 Saudável · 🌱 Vegano · 💪 Proteico · 💰 Econômico · 🍽️ Fácil · 👨‍🍳 Gourmet

---

## 🛠️ Stack Técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Injeção de Dependência | Hilt |
| Banco de dados local | Room |
| Navegação | Navigation Compose |
| Requisições HTTP | Retrofit + OkHttp |
| Imagens | Coil |
| Async | Coroutines + StateFlow |
| IA | Groq API (compatível OpenAI) |
| Fotos | Unsplash API |
| Receitas base | TheMealDB API |

---

## 🏗️ Arquitetura

O projeto segue **MVVM + Repository Pattern** com separação clara de responsabilidades:

```
app/
├── data/
│   ├── api/                  # Retrofit services (AI, Unsplash, MealDB)
│   │   └── model/            # DTOs de resposta das APIs
│   ├── database/             # Room DAO, Entity, AppDatabase
│   └── repository/
│       ├── RecipeRepository  # Lógica central: IA, MealDB, imagens, CRUD
│       └── FridgeRepository  # Gerenciamento de ingredientes da geladeira
├── di/
│   ├── AppModule             # Providers: Retrofit, OkHttp, SharedPreferences
│   └── DatabaseModule        # Provider: Room database
├── model/
│   ├── Recipe                # Modelo de domínio
│   └── RecipeFilter          # Categorias, subcategorias e tags
├── ui/
│   ├── navigation/           # NavGraph + Screen routes
│   ├── screens/
│   │   ├── home/             # HomeScreen (geração principal)
│   │   ├── fridge/           # FridgeModeScreen
│   │   ├── saved/            # SavedRecipesScreen
│   │   └── recipe/           # RecipeDetailScreen + CookingModeScreen
│   └── theme/                # Colors, Typography, Theme
└── viewmodel/
    ├── HomeViewModel         # Estado da home, geração, filtros
    ├── SavedRecipesViewModel # Receitas salvas e favoritas
    └── FridgeViewModel       # Ingredientes da geladeira
```

---

## 🔄 Fluxo de geração de receitas

```
Usuário informa ingredientes / categoria / filtros
        ↓
Tem ingredientes da geladeira?
    ├── Sim → Busca no TheMealDB → encontrou?
    │           ├── Sim → Traduz com IA → exibe com badge "TheMealDB"
    │           └── Não → Gera com IA
    └── Não → Gera com IA diretamente
        ↓
Para cada receita gerada:
    → Busca foto no Unsplash (photoSearchTerm em inglês)
        ↓
Exibe cards com nome, tempo, foto e badge de categoria
```

---

## 🔑 Configuração das APIs

Crie o arquivo `local.properties` na raiz do projeto com as chaves:

```properties
OPENAI_API_KEY=sua_chave_groq_aqui
UNSPLASH_ACCESS_KEY=sua_chave_unsplash_aqui
```

> **Groq API:** https://console.groq.com
> **Unsplash:** https://unsplash.com/developers
> **TheMealDB:** gratuita, sem chave necessária

---

## 🚀 Como executar

**Pré-requisitos:**
- Android Studio Hedgehog ou superior
- JDK 21
- Android SDK 35

```bash
# Clone o repositório
git clone https://github.com/guiuriarte/recipe-ai-android

# Abra no Android Studio
# Configure o local.properties com as API keys
# Execute em um dispositivo ou emulador (API 26+)
```

---

## 📱 Telas do app

| Tela | Descrição |
|---|---|
| **Home** | Ingredientes, categorias 3 níveis, geração de receitas |
| **Geladeira** | Cadastro de ingredientes disponíveis em casa |
| **Detalhe da Receita** | Ingredientes, passos detalhados, favoritar, modo preparo |
| **Modo Preparo** | Passo a passo guiado durante o cozimento |
| **Minhas Receitas** | Todas as receitas salvas com filtro de favoritas |

---

## 👨‍💻 Desenvolvedor

- **Guilherme Uriarte** — Product & Mobile Development

📌 Status: 🟠 Em desenvolvimento ativo (Alpha)
