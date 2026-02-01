package com.stillhere.data.remote.api

import com.stillhere.data.remote.dto.ChatRequest
import com.stillhere.data.remote.dto.ChatResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Ollama.
 */
interface OllamaApi {
    
    /**
     * Send a chat message to Ollama.
     */
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
    
    /**
     * Stream chat responses from Ollama.
     * Uses Server-Sent Events (SSE).
     */
    @POST("api/chat")
    suspend fun chatStream(@Body request: ChatRequest): Response<ResponseBody>
    
    /**
     * Check if Ollama is running and get version.
     */
    @POST("api/version")
    suspend fun getVersion(): Response<ResponseBody>
}
