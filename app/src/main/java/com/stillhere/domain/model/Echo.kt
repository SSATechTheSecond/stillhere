package com.stillhere.domain.model

/**
 * Represents an "Echo" - the animated companion character.
 */
data class Echo(
    val id: String,
    val name: String,
    val mood: EchoMood = EchoMood.HAPPY,
    val animationState: EchoAnimationState = EchoAnimationState.IDLE,
    val isTalking: Boolean = false,
    val affectionLevel: Int = 50  // 0-100
)

/**
 * Mood states for Echo character.
 */
enum class EchoMood {
    HAPPY, SAD, ANGRY, SURPRISED, EXCITED,
    RELAXED, SLEEPY, PLAYFUL, EMBARRASSED,
    LOVING, CURIOUS, BORED, CARING, CONCERNED,
    THINKING, IDLE
}

/**
 * Animation states for Echo.
 */
enum class EchoAnimationState {
    IDLE, LISTENING, THINKING, TALKING,
    CELEBRATING, SAD, SURPRISED, SLEEPING, REACTING
}

/**
 * Touch interactions with Echo.
 */
enum class TouchReaction {
    PET, POKE, TICKLE, HUG, HEADPAT
}

/**
 * User's interaction with Echo.
 */
data class UserInteraction(
    val id: Long = 0,
    val type: TouchReaction,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0
)
