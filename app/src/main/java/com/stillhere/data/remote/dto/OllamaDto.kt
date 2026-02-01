package com.stillhere.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for Ollama chat completion.
 */
data class ChatRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<MessageDto>,
    @SerializedName("stream")
    val stream: Boolean = true,
    @SerializedName("options")
    val options: OptionsDto? = null
)

/**
 * Message format for Ollama API.
 */
data class MessageDto(
    @SerializedName("role")
    val role: String,  // "user", "assistant", "system"
    @SerializedName("content")
    val content: String
)

/**
 * Model options for fine-tuning generation.
 */
data class OptionsDto(
    @SerializedName("num_thread")
    val numThread: Int? = null,
    @SerializedName("num_ctx")
    val ctxSize: Int? = null
)

/**
 * Response from Ollama API.
 */
data class ChatResponse(
    @SerializedName("model")
    val model: String,
    @SerializedName("message")
    val message: MessageDto?,
    @SerializedName("response")
    val response: String?,
    @SerializedName("done")
    val done: Boolean
)

/**
 * Streaming chunk from Ollama.
 */
data class StreamChunk(
    @SerializedName("model")
    val model: String?,
    @SerializedName("message")
    val message: MessageDto?,
    @SerializedName("response")
    val response: String?,
    @SerializedName("done")
    val done: Boolean?
)
