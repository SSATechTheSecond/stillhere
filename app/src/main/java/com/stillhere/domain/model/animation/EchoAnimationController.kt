package com.stillhere.domain.model.animation

import android.view.MotionEvent
import com.stillhere.domain.model.Echo
import com.stillhere.domain.model.EchoAnimationState
import com.stillhere.domain.model.EchoMood
import com.stillhere.domain.model.TouchReaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main controller for Echo's animations and behaviors.
 * Integrates CameraController and BehaviorEngine into a unified system.
 */
class EchoAnimationController {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Components
    private val cameraController = CameraController()
    private val behaviorEngine = BehaviorEngine(cameraController)
    
    // State
    private val _echoState = MutableStateFlow(EchoState())
    val echoState: StateFlow<EchoState> = _echoState.asStateFlow()
    
    private val _isEyeFollowing = MutableStateFlow(false)
    val isEyeFollowing: StateFlow<Boolean> = _isEyeFollowing.asStateFlow()
    
    private val _idleBehaviorState = MutableStateFlow(IdleBehaviorState.Idle)
    val idleBehaviorState: StateFlow<IdleBehaviorState> = _idleBehaviorState.asStateFlow()
    
    // Listeners
    private var onTouchReaction: ((TouchReaction) -> Unit)? = null
    
    init {
        // Set up behavior engine callbacks
        behaviorEngine.onMoodChange { mood ->
            updateEchoState()
        }
        
        behaviorEngine.onAnimationStateChange { animationState ->
            _echoState.value = _echoState.value.copy(animationState = animationState)
        }
        
        // Collect behavior states
        scope.launch {
            behaviorEngine.behaviorState.collect { state ->
                _idleBehaviorState.value = state
                updateEchoState()
            }
        }
        
        scope.launch {
            behaviorEngine.eyeFollowState.collect { following ->
                _isEyeFollowing.value = following
                updateEchoState()
            }
        }
    }

    /**
     * Start the animation system.
     */
    fun start() {
        behaviorEngine.start()
        _echoState.value = _echoState.value.copy(isAlive = true)
    }

    /**
     * Stop the animation system.
     */
    fun stop() {
        behaviorEngine.stop()
        scope.cancel()
        _echoState.value = _echoState.value.copy(isAlive = false)
    }

    /**
     * Update loop - call this every frame.
     */
    fun update(deltaTimeMs: Long = 16L) {
        // Update camera
        cameraController.update(deltaTimeMs)
        
        // Update echo state from camera
        _echoState.value = _echoState.value.copy(
            cameraState = cameraController.state
        )
    }

    /**
     * Handle new message from user.
     */
    fun onMessage(content: String) {
        behaviorEngine.onNewMessage(content)
        updateEchoState()
    }

    /**
     * Handle user typing.
     */
    fun onTyping(isTyping: Boolean) {
        behaviorEngine.onUserTyping(isTyping)
    }

    /**
     * Handle touch/click on Echo.
     */
    fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                behaviorEngine.onTouch(TouchReaction.POKE)
                onTouchReaction?.invoke(TouchReaction.POKE)
            }
            MotionEvent.ACTION_UP -> {
                // Could be tap or release
            }
        }
        updateEchoState()
    }

    /**
     * Handle long press.
     */
    fun onLongPress() {
        behaviorEngine.onTouch(TouchReaction.PET)
        onTouchReaction?.invoke(TouchReaction.PET)
        updateEchoState()
    }

    /**
     * Handle specific interaction types.
     */
    fun onInteraction(type: TouchReaction) {
        behaviorEngine.onTouch(type)
        onTouchReaction?.invoke(type)
        updateEchoState()
    }

    /**
     * Handle cursor position (for eye following).
     */
    fun onCursorPosition(x: Float, y: Float) {
        if (_isEyeFollowing.value) {
            behaviorEngine.startEyeFollowing(x, y)
        }
    }

    /**
     * Handle user away.
     */
    fun onUserAway() {
        behaviorEngine.onUserAway()
    }

    /**
     * Handle user returned.
     */
    fun onUserReturned() {
        behaviorEngine.onUserReturned()
    }

    /**
     * Set touch reaction callback.
     */
    fun setOnTouchReaction(callback: (TouchReaction) -> Unit) {
        onTouchReaction = callback
    }

    /**
     * Set Echo's mood directly.
     */
    fun setMood(mood: EchoMood) {
        _echoState.value = _echoState.value.copy(mood = mood)
        cameraController.applyMood(mood)
    }

    /**
     * Get current camera state for rendering.
     */
    fun getCameraState(): CameraState = cameraController.state

    private fun updateEchoState() {
        _echoState.value = _echoState.value.copy(
            cameraState = cameraController.state,
            mood = _echoState.value.mood,
            animationState = _echoState.value.animationState,
            isEyeFollowing = _isEyeFollowing.value,
            idleBehavior = _idleBehaviorState.value
        )
    }

    /**
     * Clean up resources.
     */
    fun destroy() {
        stop()
    }
}

/**
 * Complete state of Echo for rendering.
 */
data class EchoState(
    val mood: EchoMood = EchoMood.HAPPY,
    val animationState: EchoAnimationState = EchoAnimationState.IDLE,
    val cameraState: CameraState = CameraState(),
    val isAlive: Boolean = false,
    val isEyeFollowing: Boolean = false,
    val idleBehavior: IdleBehaviorState = IdleBehaviorState.Idle
) {
    // Derived properties for rendering
    val zoom: Float get() = cameraState.zoom
    val panX: Float get() = cameraState.panX
    val panY: Float get() = cameraState.panY
    val tilt: Float get() = cameraState.tilt
    val headRotationX: Float get() = cameraState.headRotationX
    val headRotationY: Float get() = cameraState.headRotationY
    val eyeX: Float get() = cameraState.eyeX
    val eyeY: Float get() = cameraState.eyeY
}

/**
 * Extension to get focus level from echo state.
 */
val EchoState.focusLevel: FocusLevel
    get() {
        val intensity = when {
            zoom <= 1.1f -> 0.1f
            zoom <= 1.3f -> 0.4f
            zoom <= 1.7f -> 0.6f
            else -> 0.9f
        }
        return intensity.toFocusLevel()
    }
