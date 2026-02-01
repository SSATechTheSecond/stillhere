package com.stillhere.domain.model

/**
 * Chat message between user and Echo.
 */
data class Message(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

/**
 * Ollama model configurations.
 */
enum class OllamaModel(
    val displayName: String,
    val sizeGb: String,
    val description: String
) {
    WIZARD_VICUNA_7B(
        displayName = "Wizard-Vicuna 7B",
        sizeGb = "~7GB",
        description = "Fast, good for casual chat"
    ),
    MYTHOMAX_13B(
        displayName = "MythoMax 13B",
        sizeGb = "~13GB",
        description = "Deep, thoughtful responses"
    )
}

/**
 * Settings for Ollama connection.
 */
data class OllamaSettings(
    val host: String = "http://localhost:11434",
    val fastModel: OllamaModel = OllamaModel.WIZARD_VICUNA_7B,
    val deepModel: OllamaModel = OllamaModel.MYTHOMAX_13B,
    val numThread: Int = 4,
    val ctxSize: Int = 2048
)
