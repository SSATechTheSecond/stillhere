package com.stillhere.domain.model.animation

import com.stillhere.domain.model.EchoMood

/**
 * Configuration for eye following behavior.
 */
object EyeFollowingConfig {
    const val LOOK_PROBABILITY = 0.3f  // 30% chance to look at cursor
    const val LOOK_DURATION_MIN = 2000L  // 2 seconds min
    const val LOOK_DURATION_MAX = 5000L  // 5 seconds max
    const val COOLDOWN = 10000L  // 10 seconds between looks
    const val HEAD_FOLLOW_RATIO = 0.3f  // Head follows 30% of eye movement
}

/**
 * Configuration for idle behaviors.
 */
object IdleBehaviorConfig {
    const val MIN_INTERVAL = 5000L   // 5 seconds between behaviors
    const val MAX_INTERVAL = 15000L  // 15 seconds max
    const val BEHAVIOR_CHANCE = 0.4f  // 40% chance each interval
}

/**
 * Configuration for focus mode.
 */
object FocusConfig {
    const val CASUAL_THRESHOLD = 0.25f
    const val ENGAGED_THRESHOLD = 0.5f
    const val FOCUSED_THRESHOLD = 0.75f
    
    const val ZOOM_CASUAL = 1.0f
    const val ZOOM_ENGAGED = 1.2f
    const val ZOOM_FOCUSED = 1.5f
    const val ZOOM_INTENSE = 2.0f
    
    const val TRANSITION_DURATION_MS = 800L
}

/**
 * Focus level based on conversation intensity.
 */
enum class FocusLevel(val threshold: Float, val zoom: Float) {
    CASUAL(FocusConfig.CASUAL_THRESHOLD, FocusConfig.ZOOM_CASUAL),
    ENGAGED(FocusConfig.ENGAGED_THRESHOLD, FocusConfig.ZOOM_ENGAGED),
    FOCUSED(FocusConfig.FOCUSED_THRESHOLD, FocusConfig.ZOOM_FOCUSED),
    INTENSE(1.0f, FocusConfig.ZOOM_INTENSE)
}

/**
 * Types of idle behaviors Echo can perform.
 */
enum class IdleBehaviorType {
    LOOK_AROUND,
    DAYDREAM,
    STRETCH,
    CHECK_NOTIFICATIONS,
    BLINK,
    SHIFT_WEIGHT,
    PLAY_WITH_HAIR,
    SIGH,
    HUM,
    LOOK_UP_IDLE
}

/**
 * State of an idle behavior.
 */
sealed class IdleBehaviorState {
    data object Idle : IdleBehaviorState()
    data class Preparing(val behavior: IdleBehaviorType) : IdleBehaviorState()
    data class Executing(val behavior: IdleBehaviorType, val progress: Float) : IdleBehaviorState()
    data object Cooldown : IdleBehaviorState()
}

/**
 * Triggers that affect Echo's behavior.
 */
sealed class BehaviorTrigger {
    data class NewMessage(val length: Int, val isEmotional: Boolean) : BehaviorTrigger()
    data class UserTyping(val isTyping: Boolean) : BehaviorTrigger()
    data object UserAway : BehaviorTrigger()
    data object UserReturned : BehaviorTrigger()
    data class TouchInteraction(val type: String) : BehaviorTrigger()
    data class NotificationReceived(val source: String) : BehaviorTrigger()
    data object LongSilence : BehaviorTrigger()
    data class MoodChanged(val newMood: EchoMood) : BehaviorTrigger()
}

/**
 * Eye state for rendering.
 */
data class EyeState(
    val openness: Float = 1f,    // 0 = closed, 1 = open
    val x: Float = 0f,           // -1 to 1
    val y: Float = 0f,           // -1 to 1
    val blinkProgress: Float = 0f  // 0 = normal, 0.5 = fully closed, 1 = opening
)

/**
 * Head state for rendering.
 */
data class HeadState(
    val rotationX: Float = 0f,  // Up/down tilt in degrees
    val rotationY: Float = 0f,  // Left/right turn in degrees
    val leanX: Float = 0f,      // Body lean left/right
    val leanY: Float = 0f       // Body lean forward/back
)

/**
 * Complete character pose for rendering.
 */
data class CharacterPose(
    val mood: EchoMood,
    val eyeState: EyeState,
    val headState: HeadState,
    val cameraState: CameraState,
    val mouthShape: MouthShape = MouthShape.NEUTRAL,
    val bodyAnimation: BodyAnimation = BodyAnimation.IDLE
)

/**
 * Mouth shapes for different expressions.
 */
enum class MouthShape {
    NEUTRAL,
    SMILE,
    BIG_SMILE,
    FROWN,
    O_SHAPE,
    SMIRK,
    TALKING_A,
    TALKING_O
}

/**
 * Body animations.
 */
enum class BodyAnimation {
    IDLE,
    BREATHING,
    BOUNCING,
    FLINCH,
    STRETCH,
    SHAKE_HEAD,
    NOD,
    TILT_HEAD,
    SHRUG
}

/**
 * Helper extension to get FocusLevel from intensity.
 */
fun Float.toFocusLevel(): FocusLevel {
    return when {
        this < FocusConfig.CASUAL_THRESHOLD -> FocusLevel.CASUAL
        this < FocusConfig.ENGAGED_THRESHOLD -> FocusLevel.ENGAGED
        this < FocusConfig.FOCUSED_THRESHOLD -> FocusLevel.FOCUSED
        else -> FocusLevel.INTENSE
    }
}
