package com.stillhere.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stillhere.data.repository.OllamaRepository
import com.stillhere.domain.model.Echo
import com.stillhere.domain.model.EchoAnimationState
import com.stillhere.domain.model.EchoMood
import com.stillhere.domain.model.Message
import com.stillhere.domain.model.OllamaModel
import com.stillhere.domain.model.OllamaSettings
import com.stillhere.domain.model.TouchReaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State for the main chat screen.
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val ollamaSettings: OllamaSettings = OllamaSettings(),
    val selectedModel: OllamaModel = OllamaModel.WIZARD_VICUNA_7B,
    val echo: Echo = Echo(
        id = "echo_1",
        name = "Echo",
        mood = EchoMood.HAPPY,
        animationState = EchoAnimationState.IDLE
    ),
    val error: String? = null
)

/**
 * ViewModel for the chat screen.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val ollamaRepository: OllamaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    /**
     * Send a message to Echo.
     */
    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isEmpty()) return

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            content = message,
            isFromUser = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentMessage = "",
                isLoading = true,
                isStreaming = true,
                echo = state.echo.copy(
                    animationState = EchoAnimationState.THINKING
                )
            )
        }

        streamingJob = viewModelScope.launch {
            try {
                ollamaRepository.sendMessage(
                    message = message,
                    history = _uiState.value.messages,
                    settings = _uiState.value.ollamaSettings,
                    model = _uiState.value.selectedModel
                ).collect { chunk ->
                    _uiState.update { state ->
                        val lastMessage = state.messages.lastOrNull()
                        val updatedMessages = if (lastMessage != null && !lastMessage.isFromUser) {
                            state.messages.dropLast(1) + lastMessage.copy(
                                content = lastMessage.content + chunk,
                                isStreaming = true
                            )
                        } else {
                            state.messages + Message(
                                id = UUID.randomUUID().toString(),
                                content = chunk,
                                isFromUser = false,
                                isStreaming = true
                            )
                        }
                        state.copy(
                            messages = updatedMessages,
                            echo = state.echo.copy(
                                animationState = EchoAnimationState.TALKING
                            )
                        )
                    }
                }

                // Streaming complete
                _uiState.update { state ->
                    val completedMessages = state.messages.map { msg ->
                        if (msg.isStreaming) msg.copy(isStreaming = false) else msg
                    }
                    state.copy(
                        messages = completedMessages,
                        isStreaming = false,
                        isLoading = false,
                        echo = state.echo.copy(
                            animationState = EchoAnimationState.IDLE,
                            mood = determineMood(completedMessages.lastOrNull()?.content ?: "")
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false,
                        isStreaming = false,
                        echo = state.echo.copy(
                            animationState = EchoAnimationState.IDLE,
                            mood = EchoMood.SAD
                        )
                    )
                }
            }
        }
    }

    /**
     * Update the current message input.
     */
    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    /**
     * Handle touch interaction with Echo.
     */
    fun onTouchEcho(reaction: TouchReaction) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    echo = state.echo.copy(
                        animationState = EchoAnimationState.REACTING,
                        mood = when (reaction) {
                            TouchReaction.PET, TouchReaction.HEADPAT -> EchoMood.HAPPY
                            TouchReaction.POKE -> EchoMood.SURPRISED
                            TouchReaction.TICKLE -> EchoMood.PLAYFUL
                            TouchReaction.HUG -> EchoMood.LOVING
                        }
                    )
                )
            }
            
            // Return to idle after reaction
            delay(1000)
            _uiState.update { state ->
                state.copy(
                    echo = state.echo.copy(
                        animationState = EchoAnimationState.IDLE
                    )
                )
            }
        }
    }

    /**
     * Update Ollama settings.
     */
    fun updateSettings(settings: OllamaSettings) {
        _uiState.update { it.copy(ollamaSettings = settings) }
    }

    /**
     * Switch between models.
     */
    fun switchModel(model: OllamaModel) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun determineMood(response: String): EchoMood {
        return when {
            response.contains("happy", ignoreCase = true) -> EchoMood.HAPPY
            response.contains("sad", ignoreCase = true) -> EchoMood.SAD
            response.contains("love", ignoreCase = true) -> EchoMood.LOVING
            response.contains("surprised", ignoreCase = true) -> EchoMood.SURPRISED
            response.contains("excited", ignoreCase = true) -> EchoMood.EXCITED
            else -> EchoMood.HAPPY
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
