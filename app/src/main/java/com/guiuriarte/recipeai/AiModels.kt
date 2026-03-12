package com.guiuriarte.recipeai

data class Message(
    val role: String,
    val content: String
)

data class ChatRequest(
    val model: String = "gpt-4.1-mini",
    val messages: List<Message>
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)