package com.stillhere.domain.model.animation

import com.stillhere.domain.model.Echo
import com.stillhere.domain.model.EchoAnimationState
import com.stillhere.domain.model.EchoMood
import com.stillhere.domain.model.TouchReaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * Main behavior engine that orchestrates Echo's autonomous behaviors.
 */
class BehaviorEngine(
    private val cameraController: CameraController
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var behaviorJob: Job? = null
    
    // State
    private var currentMood by MutableStateFlow(EchoMood.HAPPY)
    private var currentFocusLevel by MutableStateFlow(FocusLevel.CASUAL)
    private var isUserTyping by MutableStateFlow(false)
    private var lastUserActivity by MutableStateFlow(System.currentTimeMillis())
    private var idleBehaviorState by MutableStateFlow(IdleBehaviorState.Idle)
    
    // Eye following state
    private var eyeFollowCooldown by MutableStateFlow(0L)
    private var isEyeFollowing by MutableStateFlow(false)
    private var eyeFollowTarget: Pair<Float, Float>? = null
    
    // Listeners
    private var onMoodChange: ((EchoMood) -> Unit)? = null
    private var onAnimationStateChange: ((EchoAnimationState) -> Unit)? = null
    
    // Emotional keywords for focus detection
    private val emotionalWords = setOf(
        "love", "hate", "happy", "sad", "angry", "excited",
        "scared", "worried", "hope", "miss", "feel", "heart",
        "beautiful", "wonderful", "terrible", "awesome"
    )

    val behaviorState: StateFlow<IdleBehaviorState> = idleBehaviorState.asStateFlow()
    val eyeFollowState: StateFlow<Boolean> = isEyeFollowing.asStateFlow()

    /**
     * Start the behavior engine.
     */
    fun start() {
        behaviorJob = scope.launch {
            while (isActive) {
                update()
                delay(FRAME_TIME_MS)
            }
        }
        startIdleBehaviorLoop()
    }

    /**
     * Stop the behavior engine.
     */
    fun stop() {
        behaviorJob?.cancel()
        behaviorJob = null
    }

    /**
     * Main update loop.
     */
    private fun update() {
        // Update camera
        cameraController.update(FRAME_TIME_MS)
        
        // Update eye following
        updateEyeFollowing()
        
        // Check for long silence
        val silenceDuration = System.currentTimeMillis() - lastUserActivity
        if (silenceDuration > 30000L && idleBehaviorState.value is IdleBehaviorState.Idle) {
            onLongSilence()
        }
    }

    /**
     * Handle user typing.
     */
    fun onUserTyping(isTyping: Boolean) {
        isUserTyping.value = isTyping
        if (isTyping) {
            lastUserActivity = System.currentTimeMillis()
            // Occasionally watch the user type (30% chance)
            if (Random.nextFloat() < 0.3f) {
                startEyeFollowing(0.5f, 0.3f)  // Look at center-bottom area
            }
        }
    }

    /**
     * Handle new message.
     */
    fun onNewMessage(content: String) {
        lastUserActivity = System.currentTimeMillis()
        
        // Calculate message intensity
        val isEmotional = emotionalWords.any { content.contains(it, ignoreCase = true) }
        val intensity = calculateMessageIntensity(content.length, isEmotional)
        
        // React to message
        reactToMessage(content, intensity)
        
        // Update focus level
        updateFocusLevel(intensity)
    }

    /**
     * Handle touch interaction.
     */
    fun onTouch(reaction: TouchReaction) {
        lastUserActivity = System.currentTimeMillis()
        
        when (reaction) {
            TouchReaction.PET, TouchReaction.HEADPAT -> {
                currentMood = EchoMood.HAPPY
                cameraController.setTarget(zoom = 1.1f, durationMs = 300L)
                startEyeFollowing(0.5f, 0.5f)
            }
            TouchReaction.POKE -> {
                currentMood = EchoMood.SURPRISED
                cameraController.shake(intensity = 10f, durationMs = 200L)
                delay(200)
                cameraController.lookAt(0.5f, 0.5f, durationMs = 100L)
            }
            TouchReaction.TICKLE -> {
                currentMood = EchoMood.PLAYFUL
                cameraController.setTarget(zoom = 1.15f, durationMs = 150L)
            }
            TouchReaction.HUG -> {
                currentMood = EchoMood.LOVING
                cameraController.setTarget(zoom = 1.2f, eyeX = 0f, eyeY = 0f, durationMs = 400L)
            }
        }
        
        onMoodChange?.invoke(currentMood)
        onAnimationStateChange?.invoke(EchoAnimationState.REACTING)
        
        // Return to normal after reaction
        scope.launch {
            delay(1500)
            onAnimationStateChange?.invoke(EchoAnimationState.IDLE)
            cameraController.applyMood(currentMood)
        }
    }

    /**
     * Handle user away/returned.
     */
    fun onUserAway() {
        scope.launch {
            delay(5000)  // Wait 5 seconds
            if (isUserTyping.value.not()) {
                currentMood = EchoMood.BORED
                cameraController.reset(1000L)
                onMoodChange?.invoke(currentMood)
            }
        }
    }

    fun onUserReturned() {
        lastUserActivity = System.currentTimeMillis()
        currentMood = EchoMood.HAPPY
        cameraController.centerLook(500L)
        onMoodChange?.invoke(currentMood)
    }

    /**
     * Update focus level based on conversation.
     */
    private fun updateFocusLevel(intensity: Float) {
        val newLevel = intensity.toFocusLevel()
        if (newLevel != currentFocusLevel) {
            currentFocusLevel = newLevel
            cameraController.setFocusLevel(intensity, FocusConfig.TRANSITION_DURATION_MS)
        }
    }

    /**
     * Calculate message intensity (0-1).
     */
    private fun calculateMessageIntensity(length: Int, isEmotional: Boolean): Float {
        var intensity = 0f
        
        // Length contribution (cap at 50 chars)
        intensity += (length.coerceAtMost(50) / 50f) * 0.5f
        
        // Emotional contribution
        if (isEmotional) intensity += 0.3f
        
        // Question mark contribution
        intensity += 0.1f
        
        return intensity.coerceIn(0f, 1f)
    }

    /**
     * React to a new message.
     */
    private fun reactToMessage(content: String, intensity: Float) {
        // Notice reaction
        cameraController.shake(intensity = 5f, durationMs = 100L)
        
        // Look toward chat area (typically center-top for input)
        cameraController.panToward(0.5f, 0.3f, 300L)
        
        // Brief eye widen for surprise
        scope.launch {
            // Eye widen would be handled by expression system
            delay(500)
            cameraController.centerLook(600L)
        }
    }

    /**
     * Handle long silence.
     */
    private fun onLongSilence() {
        // Echo checks if you're still there
        cameraController.lookRandomly(800L)
        scope.launch {
            delay(2000)
            cameraController.centerLook(600L)
        }
    }

    /**
     * Start the idle behavior loop.
     */
    private fun startIdleBehaviorLoop() {
        scope.launch {
            while (isActive) {
                val delayTime = Random.nextLong(
                    IdleBehaviorConfig.MIN_INTERVAL,
                    IdleBehaviorConfig.MAX_INTERVAL
                )
                delay(delayTime)
                
                // Check if we should run idle behavior
                if (shouldRunIdleBehavior() && !isUserTyping.value) {
                    executeRandomIdleBehavior()
                }
            }
        }
    }

    /**
     * Check if idle behavior should run.
     */
    private fun shouldRunIdleBehavior(): Boolean {
        return Random.nextFloat() < IdleBehaviorConfig.BEHAVIOR_CHANCE &&
               idleBehaviorState.value is IdleBehaviorState.Idle &&
               !isEyeFollowing.value &&
               currentFocusLevel == FocusLevel.CASUAL
    }

    /**
     * Execute a random idle behavior.
     */
    private fun executeRandomIdleBehavior() {
        val behaviors = IdleBehaviorType.entries.toTypedArray()
        val behavior = behaviors[Random.nextInt(behaviors.size)]
        
        idleBehaviorState.value = IdleBehaviorState.Preparing(behavior)
        
        scope.launch {
            delay(500)  // Brief pause before behavior
            
            idleBehaviorState.value = IdleBehaviorState.Executing(behavior, 0f)
            
            // Execute the behavior
            executeIdleBehavior(behavior)
            
            idleBehaviorState.value = IdleBehaviorState.Cooldown
            delay(1000)  // Brief cooldown
            
            idleBehaviorState.value = IdleBehaviorState.Idle
        }
    }

    /**
     * Execute a specific idle behavior.
     */
    private suspend fun executeIdleBehavior(behavior: IdleBehaviorType) {
        when (behavior) {
            IdleBehaviorType.LOOK_AROUND -> {
                // Look left
                cameraController.lookAt(0.1f, 0.5f, 800L)
                delay(1500)
                // Look right
                cameraController.lookAt(0.9f, 0.5f, 1000L)
                delay(1500)
                // Center
                cameraController.centerLook(600L)
            }
            IdleBehaviorType.DAYDREAM -> {
                // Look up and away
                cameraController.lookAt(0.7f, 0.8f, 1000L)
                delay(3000)
                cameraController.centerLook(800L)
            }
            IdleBehaviorType.BLINK -> {
                // Blink animation (handled in rendering)
                delay(500)
            }
            IdleBehaviorType.CHECK_NOTIFICATIONS -> {
                // Look toward notification area (top-right)
                cameraController.lookAt(0.85f, 0.15f, 600L)
                delay(1500)
                cameraController.centerLook(500L)
            }
            IdleBehaviorType.SHIFT_WEIGHT -> {
                // Slight camera shift
                cameraController.setTarget(panX = 0.05f, durationMs = 500L)
                delay(800)
                cameraController.setTarget(panX = 0f, durationMs = 500L)
            }
            IdleBehaviorType.PLAY_WITH_HAIR -> {
                cameraController.lookAt(0.3f, 0.4f, 800L)
                delay(2000)
                cameraController.centerLook(600L)
            }
            else -> delay(1000)
        }
    }

    /**
     * Update eye following state.
     */
    private fun updateEyeFollowing() {
        if (eyeFollowCooldown > 0) {
            eyeFollowCooldown -= FRAME_TIME_MS
            return
        }
        
        if (isEyeFollowing.value && eyeFollowTarget != null) {
            val (x, y) = eyeFollowTarget!!
            cameraController.lookAt(x, y, 100L)
            
            // End eye following after duration
            val followEndTime = eyeFollowCooldown + FRAME_TIME_MS
            if (followEndTime <= -EyeFollowingConfig.LOOK_DURATION_MIN) {
                stopEyeFollowing()
            }
        }
    }

    /**
     * Start following cursor position.
     */
    fun startEyeFollowing(targetX: Float, targetY: Float) {
        if (Random.nextFloat() >= EyeFollowingConfig.LOOK_PROBABILITY) return
        
        isEyeFollowing.value = true
        eyeFollowTarget = targetX to targetY
        eyeFollowCooldown = -Random.nextLong(
            EyeFollowingConfig.LOOK_DURATION_MIN,
            EyeFollowingConfig.LOOK_DURATION_MAX
        )
    }

    /**
     * Stop eye following.
     */
    private fun stopEyeFollowing() {
        isEyeFollowing.value = false
        eyeFollowTarget = null
        eyeFollowCooldown = EyeFollowingConfig.COOLDOWN
        cameraController.centerLook(400L)
    }

    /**
     * Set mood callback.
     */
    fun onMoodChange(callback: (EchoMood) -> Unit) {
        onMoodChange = callback
    }

    /**
     * Set animation state callback.
     */
    fun onAnimationStateChange(callback: (EchoAnimationState) -> Unit) {
        onAnimationStateChange = callback
    }

    companion object {
        private const val FRAME_TIME_MS = 16L  // ~60fps
    }
}
