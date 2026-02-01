package com.stillhere.data.repository

import com.stillhere.data.remote.api.OllamaApi
import com.stillhere.data.remote.dto.ChatRequest
import com.stillhere.data.remote.dto.MessageDto
import com.stillhere.data.remote.dto.OptionsDto
import com.stillhere.domain.model.Message
import com.stillhere.domain.model.OllamaModel
import com.stillhere.domain.model.OllamaSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for interacting with Ollama AI.
 */
@Singleton
class OllamaRepository @Inject constructor(
    private val ollamaApi: OllamaApi
) {
    /**
     * Send a message and get streaming response.
     */
    fun sendMessage(
        message: String,
        history: List<Message>,
        settings: OllamaSettings,
        model: OllamaModel
    ): Flow<String> = flow {
        val messages = buildMessageHistory(history, message)
        
        val request = ChatRequest(
            model = getModelName(model),
            messages = messages,
            stream = true,
            options = OptionsDto(
                numThread = settings.numThread,
                ctxSize = settings.ctxSize
            )
        )

        val response = ollamaApi.chatStream(request)
        
        if (response.isSuccessful) {
            response.body()?.byteStream()?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    // Parse SSE format
                    if (line.startsWith("data:")) {
                        val json = line.substring(5).trim()
                        if (json.isNotEmpty()) {
                            // Extract content from chunk (simplified parsing)
                            val content = extractContentFromChunk(json)
                            if (content.isNotEmpty()) {
                                emit(content)
                            }
                        }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Check if Ollama is running.
     */
    suspend fun isOllamaRunning(): Boolean {
        return try {
            val response = ollamaApi.getVersion()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun buildMessageHistory(history: List<Message>, newMessage: String): List<MessageDto> {
        return history.map { msg ->
            MessageDto(
                role = if (msg.isFromUser) "user" else "assistant",
                content = msg.content
            )
        } + MessageDto(role = "user", content = newMessage)
    }

    private fun getModelName(model: OllamaModel): String {
        return when (model) {
            OllamaModel.WIZARD_VICUNA_7B -> "wizard-vicuna-uncensored:7b"
            OllamaModel.MYTHOMAX_13B -> "mythomax-l2-13b"
        }
    }

    private fun extractContentFromChunk(chunk: String): String {
        // Simplified extraction - looks for "response" field
        val responsePattern = "\"response\":\"([^\"]*)\"".toRegex()
        val match = responsePattern.find(chunk)
        return match?.groupValues?.get(1)?.replace("\\n", "\n") ?: ""
    }
}
